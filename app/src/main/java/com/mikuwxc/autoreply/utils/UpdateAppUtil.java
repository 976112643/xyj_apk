package com.mikuwxc.autoreply.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.VersionInfo;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.modle.AppVersionBean;
import com.mikuwxc.autoreply.modle.HttpImeiBean;
import com.taobao.sophix.SophixManager;

import okhttp3.Call;

public class UpdateAppUtil {
    public static void getAppVersion(){
        OkGo.get(AppConfig.OUT_NETWORK + NetApi.upDateAppVersion).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                Log.e("111","result:" + s);
                try {
                    HttpImeiBean<AppVersionBean> bean = new Gson().fromJson(s, new TypeToken<HttpImeiBean<AppVersionBean>>(){}.getType());
                    if (bean.isSuccess()) {

                        //获取当前app版本号与后台对比后台版本号大于本地app版本号时进行更新
                     /*   PackageInfo packageInfo = null;
                        packageInfo = getApplicationContext()
                                      .getPackageManager()
                                      .getPackageInfo(getPackageName(), 0);*/
                        //Integer version = Integer.valueOf(packageInfo.versionCode);
                        if(bean.getResult().getVersionCode() > VersionInfo.versionCode){
                         /*queryAndLoadNewPatch不可放在attachBaseContext 中，
                           否则无网络权限，建议放在后面任意时刻，如onCreate中*/
                            //去阿里看是否有补丁包
                            SophixManager.getInstance().queryAndLoadNewPatch();
                        }

                        Log.e("111", "获取App版本信息成功:"+bean.getResult());
                    }else {
                        Log.e("111", "获取App版本信息失败:");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("111", "获取App版本信息失败:"+e.toString());
                }
            }
            @Override
            public void onError(Call call, okhttp3.Response response, Exception e) {
                super.onError(call, response, e);
                Log.e("111", "获取App版本信息失败:");
            }
        });
    }
}
