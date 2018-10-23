package com.mikuwxc.autoreply.wxmoment;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.receiver.MomentReceiver;
import com.mikuwxc.autoreply.wxmoment.model.SnsInfo;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

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
                        downloadPic(linkAddress);
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

    private static void downloadPic(String linkAddress) {
//        http://shmmsns.qpic.cn/mmsns/ricicAmibSGY8By3j4U8gx4ywD3lRrKTMicZVniad7jN6mx3T0abPu7iaZPAAMA2vb2oxkhB0SIqVs2AM/
        int i = linkAddress.indexOf("/", 25);

        String fileName= linkAddress.substring(i+1,linkAddress.lastIndexOf("/"))+".jpg";
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
     * **/
    public static String uploadPic(String picPath) {
        File temp = new File(picPath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");
        //又拍云的保存路径，任选其中一个
        String savePath = "/picforapp/" +"/testccc";
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

}
