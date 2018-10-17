package com.mikuwxc.autoreply.callrecorder;

import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class RecordUpload {


    //上传又拍云语音电话文件
    public static String uploadAmr(String name, final String absolutePath, final String startTime, final String endTime, final String imei, final String type, final String phoneNum) {
       final File temp = new File(absolutePath);
//        temp = new File(absolutePath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");


/*        final String newArmLast = amrPath.substring(amrPath.lastIndexOf("/")+1);

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);*/

        //又拍云的保存路径，任选其中一个
//        final String savePath="/amrforapp/"+username+"/"+sign +"/"+str+"/" +newArmLast;
        final String savePath = "/callrecord/" + name;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis() + 15);
        //进度回调，可为空
        final UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                Log.e("callrecord", (100 * bytesWrite) / contentLength + "%");
                Log.e("callrecord", bytesWrite + "::" + contentLength);

            }
        };

        //结束回调，不可为空
        final UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                // textView.setText(isSuccess + ":" + result);
                Log.e("callrecord", isSuccess + ":" + result);


                String newSavePath = AppConfig.YOUPAIYUN + savePath;
                Log.d("call", newSavePath);


                if (isSuccess) {
                    RecordBean recordBean = new RecordBean(imei, phoneNum, type, startTime, endTime,newSavePath);
                    OkGo.<String>post(AppConfig.OUT_NETWORK + NetApi.upload_phone_record)
                            .tag(this)
                            .upJson(new Gson().toJson(recordBean))
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(String s, Call call, Response response) {

                                    UploadSuccess uploadSuccess = new Gson().fromJson(s, UploadSuccess.class);
                                    if("200".equals(uploadSuccess.code)){
                                        File file=new File(absolutePath);
                                        boolean exists = file.exists();
                                        if(exists){
                                            boolean delete = file.delete();
//                                            ToastUtil.showLongToast("录音文件上传成功,并且已删除");
                                        }
                                    }

                                }

                                @Override
                                public void onError(Call call, Response response, Exception e) {
                                    super.onError(call, response, e);
                                }
                            });
                }else{

                }

            }
        };

        //表单上传（本地签名方式）

        UploadEngine.getInstance().formUpload(temp, paramsMap, "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);




        return "";
    }


    static class RecordBean{

        private String imei;
        private String phoneNum;
        private String type;
        private String starTime;
        private String endTime;
        private String recordingFile;

        public RecordBean(String imei, String phoneNum, String type, String starTime, String endTime, String recordingFile) {
            this.imei = imei;
            this.phoneNum = phoneNum;
            this.type = type;
            this.starTime = starTime;
            this.endTime = endTime;
            this.recordingFile = recordingFile;
        }


        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getPhoneNum() {
            return phoneNum;
        }

        public void setPhoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStarTime() {
            return starTime;
        }

        public void setStarTime(String starTime) {
            this.starTime = starTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getRecordingFile() {
            return recordingFile;
        }

        public void setRecordingFile(String recordingFile) {
            this.recordingFile = recordingFile;
        }
    }


    class UploadSuccess{

        /**
         * msg : 添加成功
         * code : 200
         * success : true
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
