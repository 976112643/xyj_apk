package com.mikuwxc.autoreply.wxmoment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.receiver.MomentReceiver;
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
     * http://szmmsns.qpic.cn/mmsns/IiaPEQGicElLUaQJyk0nCT0S2EfKbxahnc0HUIRMpzW06W3ZMGDaQWCSWtMTNJLfkpEicVOWrJzuicQ/0     图文
     * 处理朋友圈数据的上传
     **/
    public static void handleDatas2() {

        //打开数据库
        final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);

        //sd卡文件夹每次判断不存在则创建处理 防止应用崩溃
        File picFile = new File(Config.EXT_DIR + "/pic");
        File videoFile = new File(Config.EXT_DIR + "/video");
        if (!picFile.exists()) {
            picFile.mkdirs();
        }
        if (!videoFile.exists()) {
            videoFile.mkdirs();
        }


        //需要拿到当前账户登录的 微信id
        final String wxid = MyFileUtil.readFromFile(AppConfig.APP_USERNAME).split(",")[1];
        //查询moment表中 当前微信号登录的用户  且uploadsuccess=false(未上传到服务器) 的所有数据
        Cursor cursor = database.rawQuery("select * from moment where uploadsuccess=? and authorId=?", new String[]{"false",wxid});
        //如果查询出来的数量大于0
        if (cursor.getCount() > 0) {
            //构建json数据的集合  最终需要将List->json上传
            final List<MomentUploadBean> uploadDatas = new ArrayList<>();
            //遍历查询到的结果集
            while (cursor.moveToNext()) {
                String mediaListJson = cursor.getString(cursor.getColumnIndex("mediaList"));//媒体集合(图片链接或者视频链接)
                final String momentsId = cursor.getString(cursor.getColumnIndex("snsId"));//朋友圈id 唯一
                final String authorId = cursor.getString(cursor.getColumnIndex("authorId"));//微信id

//                wxid = authorId;
                final String content = cursor.getString(cursor.getColumnIndex("content"));//内容
                final String operateTime = cursor.getString(cursor.getColumnIndex("timestamp"));//朋友圈时间戳

                //链接集合
                List<String> mediaList = JSON.parseArray(mediaListJson, String.class);
                //判断链接集合的数量以及链接的所属类型来区分是图片、视频、链接(这种做法不精确,但是暂时未找到微信区分type的方法,待改进)  0图文 1视频 2文本 3链接 99未知
                if (mediaList.size() > 0 && mediaList.get(0).contains("qpic.cn")) {
                    //为图片类型
                    uploadDatas.add(new MomentUploadBean(content, "0", "", "", operateTime, "", momentsId));
                } else if(mediaList.size() > 0&&mediaList.get(0).contains("video")){
                    //为视频类型
                    uploadDatas.add(new MomentUploadBean(content, "1", "", "", operateTime, "", momentsId));
                }else if(mediaList.size()==0){
                    uploadDatas.add(new MomentUploadBean(content, "0", "", "", operateTime, "", momentsId));
                }

            }

            //最终需要上传的普通json  (不包媒体链接,因为需要下载并上传到云平台后才能知道媒体链接)
            String json = JSON.toJSONString(uploadDatas);
            OkGo.<String>post(AppConfig.OUT_NETWORK + NetApi.upload_moments_synchronous + wxid)
                    .tag(new Object())
                    .upJson(json)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {
                            //上传成功后需要将  上传成功的数据库数据改为成功状态
//                            {"msg":"该微信号不存在","code":"4004","success":false}
                            SuccessBean successBean = new Gson().fromJson(s, SuccessBean.class);
                            if("200".equals(successBean.getCode())){
                                SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                                for (int i = 0; i < uploadDatas.size(); i++) {
                                    String momentsId = uploadDatas.get(i).getMomentsId();
                                    database.execSQL("update moment set uploadsuccess= ? where snsId=?", new String[]{"true",momentsId});
                                }
                                database.close();
                            }

                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            super.onError(call, response, e);

                        }
                    });

        }


        //================================查询当前微信号(账户)图片或者视频 下载 上传 并写入数据库处理(多的这张表)==================================
        Cursor c = database.rawQuery("select me.id,me.address,me.yunaddress,me.uploadsuccess,me.snsId ,mo.authorId " +
                "from media as me left join moment as mo on me.snsId=mo.snsId where me.uploadsuccess=? and mo.authorId=?", new String[]{"false",wxid});
        int count = c.getCount();
        while (c.moveToNext()) {
            final String address = c.getString(c.getColumnIndex("address"));
            final String yunaddress = c.getString(c.getColumnIndex("yunaddress"));
            String uploadsuccess = c.getString(c.getColumnIndex("uploadsuccess"));
            final String snsId = c.getString(c.getColumnIndex("snsId"));
            final String authorId = c.getString(c.getColumnIndex("authorId"));
            //需要剔除 如果下载过的图片 则不需要再次下载  下载完成后 根据链接tag更新yunaddress的值(yunaddress默认为null)
            if (address!=null&&address.contains("qpic.cn") && yunaddress == null) {
                //为图片
                downloadPic(address);
            } else if(address.contains("video")){
                downloadVideo(address);
            }
        }

        //延迟上传图片到云平台
        MomentReceiver.runHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                // 查询出未上传到云平台的图片或视频
                Cursor uploadCursor = database.rawQuery("select * from media where yunaddress!=? and uploadsuccess=?", new String[]{"null", "false"});
                while (uploadCursor.moveToNext()) {
                    String yunaddress = uploadCursor.getString(uploadCursor.getColumnIndex("yunaddress"));
                    //判断云地址来区别是图片链接还是视频链接  http://upyun.ijucaimao.cn/moment/pic/78478-788-4748-48974-4987-.jpg
                    if(yunaddress.contains(".jpg")){
                        uploadPic(Config.EXT_DIR + "/pic/" + yunaddress.substring(yunaddress.lastIndexOf("/") + 1), yunaddress.substring(yunaddress.lastIndexOf("/") + 1));
                    }else if(yunaddress.contains(".video")){
                        uploadVideo(Config.EXT_DIR + "/video/" + yunaddress.substring(yunaddress.lastIndexOf("/") + 1), yunaddress.substring(yunaddress.lastIndexOf("/") + 1));
                    }
                }
                uploadCursor.close();
                database.close();

            }
        }, 10 * 1000);//延迟10s上传图片到云平台


        //延迟调用上传图片接口
        MomentReceiver.runHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                //上传图片的数据集
                List<MomentUploadBean> uploadPicDatas = new ArrayList<>();
                final SQLiteDatabase picDatabase = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                final Cursor picCursor = picDatabase.rawQuery("select * from media where uploadsuccess=? and tomcatuploadsuccess=?", new String[]{"true", "false"});
                while (picCursor.moveToNext()) {
                    String yunaddress = picCursor.getString(picCursor.getColumnIndex("yunaddress"));
                    String snsId = picCursor.getString(picCursor.getColumnIndex("snsId"));
                    uploadPicDatas.add(new MomentUploadBean(null, null, null, null, null, yunaddress, snsId));
                }
                String json = JSON.toJSONString(uploadPicDatas);
                if (uploadPicDatas.size() > 0) {
                    OkGo.<String>put(AppConfig.OUT_NETWORK + NetApi.upload_moments_updateFodderUrl)
                            .tag(new Object())
                            .upJson(json)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(String s, Call call, Response response) {
                                    SuccessBean successBean = new Gson().fromJson(s, SuccessBean.class);
                                    if("200".equals(successBean.getCode())){
                                        picDatabase.execSQL("update media set tomcatuploadsuccess=? where uploadsuccess=?", new String[]{"true", "true"});
                                        picCursor.close();
                                        picDatabase.close();
                                    }

                                }

                                @Override
                                public void onError(Call call, Response response, Exception e) {
                                    super.onError(call, response, e);
                                    picCursor.close();
                                    picDatabase.close();
                                }
                            });
                }
            }
        }, 30 * 1000);


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
                .tag(linkAddress)//以图片链接为标识  下载成功后需要更改本地数据库media表中yunaddress状态为新地址
                .execute(new FileCallback(Config.EXT_DIR + "/pic", fileName) {
                    @Override
                    public void onSuccess(File file, Call call, Response response) {
                        String tag = (String) call.request().tag();
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                        ContentValues c = new ContentValues();
                        c.put("yunaddress", AppConfig.YOUPAIYUN + "/moment/pic/" + fileName);
                        //根据tag来更新
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



    class SuccessBean{

        /**
         * msg : 该微信号不存在
         * code : 4004
         * success : false
         */

        private String msg;
        private String code;
        private boolean success;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }




}
