package com.mikuwxc.autoreply.wxmoment;

import android.util.Log;

import com.mikuwxc.autoreply.common.util.AppConfig;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * 朋友圈图片 视频 上传
 * **/
public class MomentPicUpload {


    /**
     * 朋友圈上传图片
     * **/
    private static String uploadPic(String picPath) {
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
    private String uploadVideo(String sendVideoPath) {
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

}
