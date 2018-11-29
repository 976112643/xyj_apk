package com.mikuwxc.autoreply.callrecorder;

import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.upyun.library.common.Params;
import com.upyun.library.common.ResumeUploader;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        paramsMap.put("avopts", "/f/mp3");
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


    /**
     *
     * 上传语音格式文件
     * 并将Arm转码为MP3格式
     * 官网demo文档有问题，我按照自己的思路整理了一下
     * **/
    public static void handleArm2mp3(final String name, final String absolute, final String startTime, final String endTime, final String imei, final String type, final String phoneNum){
        File file = new File(absolute);

        //初始化断点续传
        ResumeUploader uploader = new ResumeUploader("cloned", "unesmall", UpYunUtils.md5("unesmall123456"));

        //设置 MD5 校验
        uploader.setCheckMD5(true);

        //设置进度监听
        uploader.setOnProgressListener(new UpProgressListener() {
            @Override
            public void onRequestProgress(long bytesWrite, long contentLength) {
                Log.e("xx", bytesWrite + ":" + contentLength);
            }
        });

        //初始化异步音视频处理参数,参数规则详见http://docs.upyun.com/cloud/av/#_3
        Map<String, Object> processParam = new HashMap<String, Object>();
        processParam.put(ResumeUploader.Params.BUCKET_NAME, "cloned");
        processParam.put(ResumeUploader.Params.NOTIFY_URL, "http://httpbin.org/post");//http://upyun.ijucaimao.cn/callrecord/CallRecorderTestFile_outgoing_10086_1539864824248_665768196.amr
        processParam.put(ResumeUploader.Params.ACCEPT, "json");
        //processParam.put(ResumeUploader.Params.SOURCE, "/callrecord/"+"CallRecorderTestFile_outgoing_10086_1539917792916_1870828482.amr");//待处理文件路径   /callrecord/CallRecorderTestFile_outgoing_10086_1539914363835_1937694109.amr
        processParam.put(ResumeUploader.Params.SOURCE, "/callrecord/"+name);//待处理文件路径  需要的是又拍云线上的路径   /callrecord/CallRecorderTestFile_outgoing_10086_1539914363835_1937694109.amr

        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();

        try {
            json.put(ResumeUploader.Params.TYPE, "audio");
            json.put(ResumeUploader.Params.AVOPTS, "/f/mp3");
            json.put(ResumeUploader.Params.RETURN_INFO, "true");
            //json.put(ResumeUploader.Params.SAVE_AS, "/callrecord");//输出文件保存路径（同一个空间下），如果没有指定，系统自动生成在同空间同目录下
        } catch (JSONException e) {
            e.printStackTrace();
        }
        array.put(json);

        processParam.put(ResumeUploader.Params.TASKS, array);


        uploader.upload(file, "/callrecord/"+name, null, processParam, new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                Log.e("xx", "isSuccess:" + isSuccess + "  result:" + result);
                final String savePath = "/callrecord/" + name.replace(".amr",".mp3");
                String newSavePath = AppConfig.YOUPAIYUN + savePath;//告诉服务端的路径
                newSavePath=newSavePath.substring(0,newSavePath.lastIndexOf("."))+"_fmp3.mp3";
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
                                        File file=new File(absolute);
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
        });
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
