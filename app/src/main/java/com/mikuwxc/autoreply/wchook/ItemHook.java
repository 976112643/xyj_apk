package com.mikuwxc.autoreply.wchook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.tencent.bugly.imsdk.Bugly.applicationContext;

public class ItemHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.voip.ui.VideoActivity", loadPackageParam.classLoader,
                "onCreate",Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        param.setResult(null);   //加了这句微信会cash掉
                        Activity activity=(Activity) param.thisObject;
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

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });




        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", loadPackageParam.classLoader,
                "onCreate",Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //param.setResult(null);
                        Activity activity=(Activity) param.thisObject;
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

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });



        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI", loadPackageParam.classLoader,
                "onCreate",Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //param.setResult(null);
                        Activity activity=(Activity) param.thisObject;
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

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });


        //单条聊天消息删除时
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.viewitems.b$c$a",
                loadPackageParam.classLoader,
                "onMMMenuItemSelected",
                MenuItem.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("onMMMenuItemSelectedonMMMenuItemSelectedonMMMenuItemSelectedonMMMenuItemSelected::");
                        param.setResult(0);
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });




        //删除一个好友会话记录
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.conversation.b$2",
                loadPackageParam.classLoader,
                "onClick",
                DialogInterface.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log(".b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9::");
                        param.setResult(0);
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });




        //禁止微信删除好友聊天记录
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.SingleChatInfoUI$8",
                loadPackageParam.classLoader,
                "onClick",
                DialogInterface.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("c$4c$4c$4c$4");
                        param.setResult(0);
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });


        //禁止删好友
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.profile.ui.NormalUserFooterPreference$a$1$1$2",
                loadPackageParam.classLoader,
                "onClick",
                DialogInterface.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("NormalUserFooterPreference$a$1$1$2NormalUserFooterPreference$a$1$1$2NormalUserFooterPreference$a$1$1$2");
                        param.setResult(0);
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });








/*
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.base.MMPullDownView",
                loadPackageParam.classLoader,
                "onLayout",
                boolean.class,
                int.class,
                int.class,
                int.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        ViewGroup mMPullDownView = (ViewGroup) param.thisObject;
//                        if (mMPullDownView.getVisibility() == View.GONE) return;
                        for (int i = 0; i < mMPullDownView.getChildCount(); i++) {
                            View childAt = mMPullDownView.getChildAt(i);
                            if (childAt instanceof ListView) {
                                final ListView listView = (ListView) childAt;
                                final ListAdapter adapter = listView.getAdapter();
                                XposedHelpers.findAndHookMethod(adapter.getClass(),
                                        "getView",
                                        int.class,
                                        View.class,
                                        ViewGroup.class,
                                        new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                                super.beforeHookedMethod(param);
                                                int position = (int) param.args[0];
                                                View view = (View) param.args[1];
                                                JSONObject itemData = null;
//                                                LogUtils.i(position, view.toString());
                                                if (position < adapter.getCount()) {
                                                    itemData = JSON.parseObject(JSON.toJSONString(adapter.getItem(position)), JSONObject.class);
                                                    int itemViewType = adapter.getItemViewType(position);
//                                                    LogUtils.i(itemViewType);
                                                    //经过以上代码可以知道    itemViewType == 1的时候打印的值是正常对话列表的值
                                                    XposedBridge.log("itemDataitemDataitemData::" + itemData);
                                                    XposedBridge.log("itemViewTypeitemViewTypeitemViewTypeitemViewType::" + itemViewType);
                                                }
//

                                            }

                                        });
                            }
                        }
                    }
                });*/
    }

}
