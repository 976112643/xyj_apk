package com.mikuwxc.autoreply.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

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
import com.mikuwxc.autoreply.receiver.NetworkChangeReceiver;
import com.mikuwxc.autoreply.wxmoment.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Response;

public class CallSmsUploadUtils {


    public static void uploadPhoneRecord() throws Exception {
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

                String imei = GetImeiUtil.getOnlyIdentification(MyApp.getAppContext());
//                RecordUpload.uploadAmr(name, f.getAbsolutePath(), startTime, endTime, imei, type, phoneNum);
                RecordUpload.handleArm2mp3(name, f.getAbsolutePath(), startTime, endTime, imei, type, phoneNum);
            }
        }
    }



    public static void uploadLocalSms() throws Exception {
        SPHelper.init(MyApp.getAppContext());
        String sms_list_data = SPHelper.getInstance().getString("SMS_LIST_DATA");
        SharePerSmsBean perSmsBean = new Gson().fromJson(sms_list_data, SharePerSmsBean.class);
        NetworkChangeReceiver.SmssBean smssBean = new NetworkChangeReceiver.SmssBean();


        if (perSmsBean != null) {
            List<SharePerSmsBean.DataBean> data = perSmsBean.getData();
            if (data!=null&&data.size() > 0) {

                List<NetworkChangeReceiver.SmssBean> d=new ArrayList<>();
                for (SharePerSmsBean.DataBean datum : data) {
                    String onlyIdentification = GetImeiUtil.getOnlyIdentification(MyApp.getAppContext());
                    d.add(new NetworkChangeReceiver.SmssBean(datum.getContent(), onlyIdentification,"2".equals(datum.getType())?"true":"false",datum.getTime(),datum.getPhone()));
                }
                String json=new Gson().toJson(d);
                String url = AppConfig.OUT_NETWORK + NetApi.upload_sms_messages;
                OkGo.<String>post(url)
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


}
