package com.mikuwxc.autoreply.wxmoment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.receiver.MomentReceiver;
import com.mikuwxc.autoreply.wxmoment.model.SnsInfo;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Response;


/**
 * 朋友圈图片 视频 上传
 **/
public class MomentPicUpload {


    /**
     * 朋友圈数据已经写入sd卡,开始处理
     **/
    public static void handleDatas() {
        File picFile = new File(Config.EXT_DIR + "/pic");
        File videoFile = new File(Config.EXT_DIR + "/video");
        if (!picFile.exists()) {
            picFile.mkdirs();
        }
        if (!videoFile.exists()) {
            videoFile.mkdirs();
        }
        List<SnsInfo> snsInfos = null;
        String json = getFileFromSD(Config.EXT_DIR + "/all_sns.json");//所有的数据
        if ("".equals(json)) {
            snsInfos = JSON.parseArray("[]", SnsInfo.class);//解析所有的数据

        } else {
            snsInfos = JSON.parseArray(json, SnsInfo.class);//解析所有的数据

            for (int i = 0; i < snsInfos.size(); i++) {

                SnsInfo snsInfo = snsInfos.get(i);//得到当前朋友圈
                ArrayList<String> mediaList = snsInfo.mediaList;//获取图片或者小视频
                if (mediaList.size() > 0 && mediaList.get(0).contains("http://szmmsns.qpic.cn/mmsns")) {
                    //图片或者链接
                    for (int j = 0; j < mediaList.size(); j++) {
                        String linkAddress = mediaList.get(j);
                        downloadPic(linkAddress);
                        //延时单独上传图片到云平台及调用接口
                        MomentReceiver.runHandle.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                            }
                        }, 1000 * 60);
                    }
                } else if (mediaList.size() == 1 && mediaList.get(0).contains("video.qq.com")) {
                    //视频
                    String videoAddress = mediaList.get(0);
                    downloadVideo(videoAddress);
                }

            }

            ToastUtil.showLongToast("朋友圈下载结束了");
        }


    }

    /**
     * http://szmmsns.qpic.cn/mmsns/IiaPEQGicElLUaQJyk0nCT0S2EfKbxahnc0HUIRMpzW06W3ZMGDaQWCSWtMTNJLfkpEicVOWrJzuicQ/0     图文
     **/
    public static void handleDatas2() {
        final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
        File picFile = new File(Config.EXT_DIR + "/pic");
        File videoFile = new File(Config.EXT_DIR + "/video");
        if (!picFile.exists()) {
            picFile.mkdirs();
        }
        if (!videoFile.exists()) {
            videoFile.mkdirs();
        }
        List<SnsInfo> snsInfos = null;

        Cursor cursor = database.rawQuery("select * from moment where uploadsuccess=?", new String[]{"false"});//需要上传的朋友圈数据 拼接数据并组装上传服务器
        String wxid = "";
        if (cursor.getCount() > 0) {
            List<MomentUploadBean> uploadDatas = new ArrayList<>();
            while (cursor.moveToNext()) {
                String mediaListJson = cursor.getString(cursor.getColumnIndex("mediaList"));
                final String momentsId = cursor.getString(cursor.getColumnIndex("snsId"));
                final String authorId = cursor.getString(cursor.getColumnIndex("authorId"));
                //这个写死的 需要改  需要改成通过hook拿到wxid
                wxid = authorId;
                final String content = cursor.getString(cursor.getColumnIndex("content"));
                final String operateTime = cursor.getString(cursor.getColumnIndex("timestamp"));
                StringBuilder fodderUrl = new StringBuilder();

                List<String> mediaList = JSON.parseArray(mediaListJson, String.class);
                String type = "";
                if (mediaList.size() > 0 && mediaList.get(0).contains("qpic.cn")) {

                    uploadDatas.add(new MomentUploadBean(content, "", "", "", operateTime, "", momentsId));

                } else {//视频
                    uploadDatas.add(new MomentUploadBean(content, "", "", "", operateTime, "", momentsId));
                }

            }

            //最终需要上传的普通json
            String json = JSON.toJSONString(uploadDatas);
            OkGo.<String>post(AppConfig.OUT_NETWORK + NetApi.upload_moments_synchronous + wxid)
                    .tag(new Object())
                    .upJson(json)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {
                            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                            database.execSQL("update moment set uploadsuccess= ? where uploadsuccess=?",new String[]{"true","false"});
                            database.close();
                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            super.onError(call, response, e);
                        }
                    });

        }


        //================================查询图片或者视频下载上传并写入数据库处理(多的这张表)==================================
        Cursor c = database.rawQuery("select me.id,me.address,me.yunaddress,me.uploadsuccess,me.snsId ,mo.authorId " +
                "from media as me left join moment as mo on me.snsId=mo.snsId where me.uploadsuccess=? ", new String[]{"false"});
        int count = c.getCount();
        while (c.moveToNext()) {
            final String address = c.getString(c.getColumnIndex("address"));
            final String yunaddress = c.getString(c.getColumnIndex("yunaddress"));
            String uploadsuccess = c.getString(c.getColumnIndex("uploadsuccess"));
            final String snsId = c.getString(c.getColumnIndex("snsId"));
            final String authorId = c.getString(c.getColumnIndex("authorId"));
            if (address.contains("qpic.cn") && yunaddress == null) {
                //为图片
                downloadPic(address);
            }else{
                downloadVideo(address);
            }
        }

        //延迟上传图片到云平台
        MomentReceiver.runHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                // http://456363673.jpg
                Cursor uploadCursor = database.rawQuery("select * from media where yunaddress!=? and uploadsuccess=?", new String[]{"null", "false"});
                while (uploadCursor.moveToNext()) {
                    String yunaddress = uploadCursor.getString(uploadCursor.getColumnIndex("yunaddress"));
                    uploadPic(Config.EXT_DIR + "/pic/" + yunaddress.substring(yunaddress.lastIndexOf("/") + 1), yunaddress.substring(yunaddress.lastIndexOf("/") + 1));
                    uploadVideo(Config.EXT_DIR + "/video/" + yunaddress.substring(yunaddress.lastIndexOf("/") + 1), yunaddress.substring(yunaddress.lastIndexOf("/") + 1));
                }
                uploadCursor.close();
                database.close();

            }
        }, 10 * 1000);//延迟10s上传图片到云平台


        //延迟调用上传图片接口
        List<MomentUploadBean> uploadPicDatas = new ArrayList<>();
        final SQLiteDatabase picDatabase = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
        final Cursor picCursor = picDatabase.rawQuery("select * from media where uploadsuccess=? and tomcatuploadsuccess=?", new String[]{"true", "false"});
        while (picCursor.moveToNext()) {
            String yunaddress = picCursor.getString(picCursor.getColumnIndex("yunaddress"));
            String snsId = picCursor.getString(picCursor.getColumnIndex("snsId"));
            uploadPicDatas.add(new MomentUploadBean("", "", "", "", "", yunaddress, snsId));
        }
        String json = JSON.toJSONString(uploadPicDatas);
        if (uploadPicDatas.size()>0) {
            OkGo.<String>post(AppConfig.OUT_NETWORK + NetApi.upload_moments_synchronous + wxid)
                    .tag(new Object())
                    .upJson(json)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {

                            picDatabase.execSQL("update media set tomcatuploadsuccess=? where uploadsuccess=?",new String[]{"true","true"});
                            picCursor.close();
                            picDatabase.close();
                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            super.onError(call, response, e);
                            picCursor.close();
                            picDatabase.close();
                        }
                    });
        }


        cursor.close();
        database.close();
    }

    private static void downloadVideo(String linkAddress) {
        final String fileName = UUID.randomUUID().toString() + ".avi".replaceAll("-", "");
        OkGo.<File>get(linkAddress)
                .tag(linkAddress)
                .execute(new FileCallback(Config.EXT_DIR + "/video", fileName) {
                    @Override
                    public void onSuccess(File file, Call call, Response response) {
                        String tag = (String) call.request().tag();
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                        ContentValues c = new ContentValues();
                        c.put("yunaddress", AppConfig.YOUPAIYUN + "/moment/video/" + fileName);
                        database.update("media", c, "address=?", new String[]{tag});
                        database.close();

                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                    }
                });
    }

    /**
     * @param linkAddress 下载链接
     **/
    private static void downloadPic(String linkAddress) {
        final String fileName = UUID.randomUUID().toString() + ".jpg".replaceAll("-", "");
        OkGo.<File>get(linkAddress)
                .tag(linkAddress)
                .execute(new FileCallback(Config.EXT_DIR + "/pic", fileName) {
                    @Override
                    public void onSuccess(File file, Call call, Response response) {
                        String tag = (String) call.request().tag();
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                        ContentValues c = new ContentValues();
                        c.put("yunaddress", AppConfig.YOUPAIYUN + "/moment/pic/" + fileName);
                        database.update("media", c, "address=?", new String[]{tag});
                        database.close();

                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                    }
                });
    }


    /**
     * 朋友圈上传图片
     *
     * @param picPath 图片sd卡路径
     * @param name    文件名字(自定义一个)
     *                *
     **/
    public static String uploadPic(String picPath, String name) {
        File temp = new File(picPath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");
        //又拍云的保存路径，任选其中一个
        String savePath = "/moment/" + "/pic/" + name;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis() + 15);
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
            }
        };
        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {

                if (isSuccess) {
                    try {
                        // /moment/pic/94bfb4f3-53d1-49ca-a73e-46a56a4d4abd.jpg
                        String url = AppConfig.YOUPAIYUN + new JSONObject(result).optString("url");
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                        database.execSQL("update media set uploadsuccess=? where yunaddress=?", new String[]{"true", url});
                        database.close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp, paramsMap, "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);
        return "";
    }


    /**
     * 朋友圈上传视频
     **/
    //上传自己发送的视频
    public static String uploadVideo(String picPath, String name) {
        File temp = new File(picPath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");
        //又拍云的保存路径，任选其中一个
        String savePath = "/moment/" + "/video/" + name;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis() + 15);
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
            }
        };
        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                if (isSuccess) {
                    try {
                        // /moment/video/94bfb4f3-53d1-49ca-a73e-46a56a4d4abd.avi
                        String url = AppConfig.YOUPAIYUN + new JSONObject(result).optString("url");
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                        database.execSQL("update media set uploadsuccess=? where yunaddress=?", new String[]{"true", url});
                        database.close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp, paramsMap, "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);

        return "";
    }


    /**
     * 从sd卡获取json
     **/
    private static String getFileFromSD(String path) {
        String result = "";

        try {
            FileInputStream f = new FileInputStream(path);
            BufferedReader bis = new BufferedReader(new InputStreamReader(f));
            String line = "";
            while ((line = bis.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }


    public static class MomentUploadBean {

        /**
         * content : 朋友圈内容
         * type : 0
         * location : address
         * comment : 评论
         * operateTime : 1984416541654156
         * fodderUrl : http://grwberbr.gwgbh,http://rbvrbrebn.com,http://fvrb.com
         * momentsId : CEVEBVE
         */

        private String content;
        private String type;
        private String location;
        private String comment;
        private String operateTime;
        private String fodderUrl;
        private String momentsId;

        public MomentUploadBean() {
        }

        public MomentUploadBean(String content, String type, String location, String comment, String operateTime, String fodderUrl, String momentsId) {
            this.content = content;
            this.type = type;
            this.location = location;
            this.comment = comment;
            this.operateTime = operateTime;
            this.fodderUrl = fodderUrl;
            this.momentsId = momentsId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getOperateTime() {
            return operateTime;
        }

        public void setOperateTime(String operateTime) {
            this.operateTime = operateTime;
        }

        public String getFodderUrl() {
            return fodderUrl;
        }

        public void setFodderUrl(String fodderUrl) {
            this.fodderUrl = fodderUrl;
        }

        public String getMomentsId() {
            return momentsId;
        }

        public void setMomentsId(String momentsId) {
            this.momentsId = momentsId;
        }
    }

}
