package com.mikuwxc.autoreply.wchook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WScanxHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.MMActivity", loadPackageParam.classLoader,
                "onCreateOptionsMenu",Menu.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String cn=param.thisObject.getClass().getSimpleName();
                        XposedBridge.log("OnCreateOptionsMenu cn="+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn+cn);
                        Activity activity=(Activity) param.thisObject;


                    /*    XSharedPreferences saoyisaoStaus = new XSharedPreferences("com.mikuwxc.autoreply", "saoyisaoStaus");
                        boolean saoyisaoStaus_put = saoyisaoStaus.getBoolean("saoyisaoStaus_put", true);*/



                        Properties properties = new Properties();
                        InputStream input = null;
                        boolean saoyisaoStaus_put = true;
                        boolean setting_put = true;
                        try {
                            input = new FileInputStream("/storage/emulated/0/hongbao.properties");//加载Java项目根路径下的配置文件
                            properties.load(input);// 加载属性文件
                            saoyisaoStaus_put = Boolean.parseBoolean(properties.getProperty("saoyisaoStaus_put"));
                            setting_put = Boolean.parseBoolean(properties.getProperty("setting_put"));
                        } catch (IOException io) {

                        } finally {
                            if (input != null) {
                                try {
                                    input.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }



                        if ("BaseScanUI".equals(cn)&&saoyisaoStaus_put==false){
                            ComponentName componentName = new ComponentName(
                                    "com.mikuwxc.autoreply",   //要去启动的App的包名
                                    "com.mikuwxc.autoreply.activity.AuthorityActivity");
                            //要去启动的App中的Activity的类名
                            // ComponentName : 参数说明
                            //组件名称，第一个参数是包名，也是主配置文件Manifest里设置好的包名
                            //第二个是类名，要带上包名
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            intent.setComponent(componentName);
                            activity.startActivity(intent);
                            activity.finish();
                        }

                        if ("SettingsUI".equals(cn)&&setting_put==false){
                            ComponentName componentName = new ComponentName(
                                    "com.mikuwxc.autoreply",   //要去启动的App的包名
                                    "com.mikuwxc.autoreply.activity.AuthorityActivity");
                            //要去启动的App中的Activity的类名
                            // ComponentName : 参数说明
                            //组件名称，第一个参数是包名，也是主配置文件Manifest里设置好的包名
                            //第二个是类名，要带上包名
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            intent.setComponent(componentName);
                            activity.startActivity(intent);
                            activity.finish();
                        }




                    }
                });

    }
}
