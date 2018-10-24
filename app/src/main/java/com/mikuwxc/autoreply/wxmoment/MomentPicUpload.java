package com.mikuwxc.autoreply.wxmoment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import okhttp3.Call;
import okhttp3.Response;


/**
 * 朋友圈图片 视频 上传
 * **/
public class MomentPicUpload {


    /**
     * 朋友圈数据已经写入sd卡,开始处理
     * **/
    public static void handleDatas() {
        File picFile=new File(Config.EXT_DIR+"/pic");
        File videoFile=new File(Config.EXT_DIR+"/video");
        if(!picFile.exists()){
            picFile.mkdirs();
        }
        if(!videoFile.exists()){
            videoFile.mkdirs();
        }
        List<SnsInfo> snsInfos=null;
        String json = getFileFromSD(Config.EXT_DIR+"/all_sns.json");//所有的数据
        if("".equals(json)){
            snsInfos = JSON.parseArray("[]", SnsInfo.class);//解析所有的数据

        }else{
            snsInfos = JSON.parseArray(json, SnsInfo.class);//解析所有的数据

            for (int i = 0; i < snsInfos.size(); i++) {

                SnsInfo snsInfo = snsInfos.get(i);//得到当前朋友圈
                ArrayList<String> mediaList = snsInfo.mediaList;//获取图片或者小视频
                if(mediaList.size()>0&&mediaList.get(0).contains("http://szmmsns.qpic.cn/mmsns")){
                    //图片或者链接
                    for (int j = 0; j < mediaList.size(); j++) {
                        String linkAddress = mediaList.get(j);
                        downloadPic(linkAddress,"");
                        //延时单独上传图片到云平台及调用接口
                        MomentReceiver.runHandle.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                            }
                        },1000*60);
                    }
                }else if(mediaList.size()==1&&mediaList.get(0).contains("video.qq.com")){
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
     *
     * **/
    public static void handleDatas2() {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
        File picFile=new File(Config.EXT_DIR+"/pic");
        File videoFile=new File(Config.EXT_DIR+"/video");
        if(!picFile.exists()){
            picFile.mkdirs();
        }
        if(!videoFile.exists()){
            videoFile.mkdirs();
        }
        List<SnsInfo> snsInfos=null;

        Cursor cursor = database.rawQuery("select * from moment where uploadsuccess=?",new String[]{"false"});//需要上传的朋友圈数据 拼接数据并组装上传服务器
        String wxid="";
        if(cursor.getCount()>0) {
            List<MomentUploadBean> uploadDatas=new ArrayList<>();
            while (cursor.moveToNext()){
                String mediaListJson = cursor.getString(cursor.getColumnIndex("mediaList"));
                final String momentsId = cursor.getString(cursor.getColumnIndex("snsId"));
                final String authorId = cursor.getString(cursor.getColumnIndex("authorId"));
                //这个写死的 需要改  需要改成通过hook拿到wxid
                wxid=authorId;
                final String content = cursor.getString(cursor.getColumnIndex("content"));
                final String operateTime = cursor.getString(cursor.getColumnIndex("timestamp"));
                StringBuilder fodderUrl=new StringBuilder();

                List<String> mediaList = JSON.parseArray(mediaListJson, String.class);
                String type="";
                if(mediaList.size()>0&&mediaList.get(0).startsWith("http://szmmsns.qpic.cn")){//图片

                    for (String link : mediaList) {
                        // 源地址 http://szmmsns.qpic.cn/mmsns/IiaPEQGicElLUaQJyk0nCT0fNj1Yr7ex7vyicH8ziaL9r6icST3HqGD8j3P3wyaC3W8FogibQeHpCy9ibQ/0
                        // 需要转成的目标地址 http://upyun.ijucaimao.cn/moment/pic/wxid_kwd8tbhbbgsr22/12921400918396842177-IiaPEQGicElLUaQJyk0nCT0S2EfKbxahnc0HUIRMpzW06W3ZMGDaQWCSWtMTNJLfkpEicVOWrJzuicQ.jpg
                        int i = link.indexOf("/", 25);
                        String name=AppConfig.YOUPAIYUN+"/moment/pic/"+authorId+"/"+momentsId+"-"+ link.substring(i+1,link.lastIndexOf("/"))+".jpg";
                        fodderUrl.append(name).append(",");
                    }

                    String s = fodderUrl.toString();
                    String urls=s.substring(0,s.length()-1);


                    uploadDatas.add(new MomentUploadBean(content,"","","",operateTime,urls,momentsId));


                }else{//视频

                }

            }

            String json = JSON.toJSONString(uploadDatas);//最终需要上传的json
            OkGo.<String>post(AppConfig.OUT_NETWORK+NetApi.upload_moments_synchronous+wxid)
                    .tag(new Object())
                    .upJson(json)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {

                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            super.onError(call, response, e);
                        }
                    });

        }



        //================================查询图片或者视频上传并写入数据库处理(多的这张表)==================================
        Cursor c = database.rawQuery("select me.id,me.address,me.uploadsuccess,me.snsId ,mo.authorId " +
                "from media as me left join moment as mo on me.snsId=mo.snsId where me.uploadsuccess=? ", new String[]{"false"});
        int count = c.getCount();
        while(c.moveToNext()){
            final String address = c.getString(c.getColumnIndex("address"));
            String uploadsuccess = c.getString(c.getColumnIndex("uploadsuccess"));
            final String snsId = c.getString(c.getColumnIndex("snsId"));
            final String authorId = c.getString(c.getColumnIndex("authorId"));
            if(address.startsWith("http://szmmsns.qpic.cn")){
                //为图片
                downloadPic(address,snsId);
                MomentReceiver.runHandle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int i = address.indexOf("/", 25);
                        String fileName=snsId+"-"+ address.substring(i+1,address.lastIndexOf("/"))+".jpg";
                        String path=Config.EXT_DIR+"/pic/"+fileName;
                        uploadPic(path,fileName,authorId);
                    }
                },10*1000);//延迟10s上传图片到云平台
            }
        }

        cursor.close();
        database.close();
    }

    private static void downloadVideo(String videoAddress) {
        OkGo.<File>get(videoAddress)
                .tag(new Object())
                .execute(new FileCallback(Config.EXT_DIR+"/video",videoAddress) {
                    @Override
                    public void onSuccess(File file, Call call, Response response) {

                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                    }
                });
    }

    /**
     * @param linkAddress 下载链接
     * @param snsId 朋友圈id 区别朋友圈说说的唯一id 上传完成后地址要存在数据库
     * **/
    private static void downloadPic(String linkAddress,String snsId) {
//        http://shmmsns.qpic.cn/mmsns/ricicAmibSGY8By3j4U8gx4ywD3lRrKTMicZVniad7jN6mx3T0abPu7iaZPAAMA2vb2oxkhB0SIqVs2AM/
        int i = linkAddress.indexOf("/", 25);
        String fileName=snsId+"-"+ linkAddress.substring(i+1,linkAddress.lastIndexOf("/"))+".jpg";
        OkGo.<File>get(linkAddress)
                .tag(new Object())
                .execute(new FileCallback(Config.EXT_DIR+"/pic",fileName) {
                    @Override
                    public void onSuccess(File file, Call call, Response response) {

                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                    }
                });
    }


    /**
     * 朋友圈上传图片
     * @param picPath 图片sd卡路径
     * @param name 文件名字(自定义一个)
     * * **/
    public static String uploadPic(String picPath,String name,String authorId) {
        File temp = new File(picPath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");
        //又拍云的保存路径，任选其中一个
        String savePath = "/moment/" +"/pic/"+authorId+"/"+name;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis()+15);
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

            //上传成功后的图片// http://upyun.ijucaimao.cn/moment/pic/wxid_kwd8tbhbbgsr22/12921400918396842177-IiaPEQGicElLUaQJyk0nCT0S2EfKbxahnc0HUIRMpzW06W3ZMGDaQWCSWtMTNJLfkpEicVOWrJzuicQ.jpg
                if(isSuccess){
                    try {
                        String url = new JSONObject(result).optString("url");
                        String snsId=url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("-"));
                        String likeName=url.substring(url.lastIndexOf("-")+1,url.lastIndexOf("."));
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
                        Cursor cursor = database.rawQuery("select * from media where snsId=? and address like ?", new String[]{snsId,"%"+likeName+"%"});
                        while(cursor.moveToNext()){
                            String id = cursor.getString(cursor.getColumnIndex("id"));
                            ContentValues cv=new ContentValues();
                            cv.put("uploadsuccess","true");
                            int updatemediaCount = database.update("media", cv, "id=?", new String[]{id});
                        }
                        cursor.close();
                        database.close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp,paramsMap , "unesmall",UpYunUtils.md5("unesmall123456"), completeListener, progressListener);
        return "";
    }




    /**
     * 朋友圈上传视频
     * **/
    //上传自己发送的视频
    public String uploadVideo(String sendVideoPath) {
        File temp = new File(sendVideoPath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");
        //又拍云的保存路径，任选其中一个
        final String savePath = "/videoforapp/"+"xxxxxccccc" ;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis()+15);
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
                String newSavePath=AppConfig.YOUPAIYUN+savePath;

            }
        };
        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp,paramsMap , "unesmall",UpYunUtils.md5("unesmall123456"), completeListener, progressListener);

        return "";
    }



    /**从sd卡获取json**/
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



    public static class MomentUploadBean{

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
