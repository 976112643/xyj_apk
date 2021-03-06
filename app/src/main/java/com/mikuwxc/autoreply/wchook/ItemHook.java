package com.mikuwxc.autoreply.wchook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.wcentity.WechatEntity;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class ItemHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam,WechatEntity paramWechatEntity) throws ClassNotFoundException {

        /*//是否能发起语音视频聊天
        XposedHelpers.findAndHookMethod("com.tencent.mm.pluginsdk.ui.chat.AppGrid$1", loadPackageParam.classLoader,
                "onItemClick", AdapterView.class, View.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        param.setResult(0);   //加了这句微信会cash掉
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
                });*/


     /*   XposedHelpers.findAndHookMethod("com.tencent.mm.ui.p$10", loadPackageParam.classLoader, "onClick", View.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                XposedBridge.log("点击了发送按钮");
                
            }
        });*/


        //是否能领红包
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", loadPackageParam.classLoader,
                "onCreate",Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //param.setResult(null);
                        boolean receiveLuckyMoneyStaus_put = true;

                        receiveLuckyMoneyStaus_put = MyFileUtil.readProperties("receiveLuckyMoneyStaus_put");

                        if (receiveLuckyMoneyStaus_put){
                            //可以领红包
                        }else{
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
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });


        //是否能领转账
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI", loadPackageParam.classLoader,
                "onCreate",Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //param.setResult(null);
                        boolean receiveLuckyMoneyStaus_put = true;

                        receiveLuckyMoneyStaus_put = MyFileUtil.readProperties("receiveLuckyMoneyStaus_put");

                        if (receiveLuckyMoneyStaus_put){
                            //可以领红包
                        }else{
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
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });


        //单条聊天消息删除时
        XposedHelpers.findAndHookMethod(paramWechatEntity.delete_selected_friend_chat_record_class,
                loadPackageParam.classLoader,
                paramWechatEntity.delete_selected_friend_chat_record_method,
                MenuItem.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        MenuItem menuItem= (MenuItem) param.args[0];
                        XposedBridge.log("onMMMenuItemSelectedonMMMenuItemSelectedonMMMenuItemSelectedonMMMenuItemSelected::"+menuItem.getTitle());
                        boolean onFriendChatDeleteStaus_put = true;
                        onFriendChatDeleteStaus_put = MyFileUtil.readProperties("onFriendChatDeleteStaus_put");
                        if (onFriendChatDeleteStaus_put==false&&"删除".equals(menuItem.getTitle())){
                            param.setResult(0);
                        }else{
                            //可以删除好友聊天会话
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });




        //删除一个好友会话记录
        XposedHelpers.findAndHookMethod(paramWechatEntity.delete_single_friend_chat_record_class,
                loadPackageParam.classLoader,
                paramWechatEntity.delete_single_friend_chat_record_method,
                DialogInterface.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log(".b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9.b$9::");
                        boolean onFriendChatDeleteStaus_put = true;

                        onFriendChatDeleteStaus_put = MyFileUtil.readProperties("onFriendChatDeleteStaus_put");
                        if (onFriendChatDeleteStaus_put){
                            //可以删除好友聊天会话
                        }else{
                            param.setResult(0);
                        }

                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });




        //禁止微信删除好友聊天记录
        XposedHelpers.findAndHookMethod(paramWechatEntity.delete_friend_chat_record_class,
                loadPackageParam.classLoader,
                paramWechatEntity.delete_friend_chat_record_method,
                DialogInterface.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("c$4c$4c$4c$4");
                        boolean onFriendChatDeleteStaus_put = true;

                        onFriendChatDeleteStaus_put = MyFileUtil.readProperties("onFriendChatDeleteStaus_put");
                        if (onFriendChatDeleteStaus_put){
                            //可以删除好友聊天会话
                        }else{
                            param.setResult(0);
                        }
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });


        //禁止删好友
        XposedHelpers.findAndHookMethod(paramWechatEntity.no_friends_deleted_class,
                loadPackageParam.classLoader,
                paramWechatEntity.no_friends_deleted_method,
                DialogInterface.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("NormalUserFooterPreference$a$1$1$2NormalUserFooterPreference$a$1$1$2NormalUserFooterPreference$a$1$1$2");

                        boolean onDeleteFriendStaus_put = true;

                        onDeleteFriendStaus_put = MyFileUtil.readProperties("onDeleteFriendStaus_put");
                        if (onDeleteFriendStaus_put){
                            //可以删除好友
                        }else{
                            param.setResult(0);
                        }
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                    }
                });

    }

}
