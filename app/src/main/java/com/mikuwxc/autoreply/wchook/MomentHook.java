package com.mikuwxc.autoreply.wchook;



import android.content.Context;
import android.content.Intent;

import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcutil.DownLoadWxResFromWxUtil;
import com.mikuwxc.autoreply.xposed.MainHook;

import org.greenrobot.eventbus.EventBus;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MomentHook {

    public static void hook(final Context context, final WechatEntity wechatEntity, XC_LoadPackage.LoadPackageParam param) {
        try {
            final ClassLoader wxClassLoader = param.classLoader;
            Class class1 = XposedHelpers.findClass(wechatEntity.hook_sns_callback_class1, wxClassLoader);
            XposedHelpers.findAndHookMethod(wechatEntity.hook_sns_callback_class2, wxClassLoader, wechatEntity.hook_sns_callback_method1, new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, class1, byte[].class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    WxEventBus.publish(new WxCollectCurrentMoment(wechatEntity, wxClassLoader, param.thisObject, param.args[4]));
                    XposedBridge.log("====朋友圈11===="+param.args[4]);

                    Intent intent = new Intent();
                    intent.setAction("moment");
                    intent.putExtra("source","进入了当前朋友圈");
                    context.sendBroadcast(intent);

                      WechatUsernameHook.hook();//获取微信昵称 微信号 用户名等
                    DownLoadWxResFromWxUtil.downloadMomentPic(wxClassLoader,wechatEntity,"-5523728528802566015",1540543502);


                }
            }});
            XposedHelpers.findAndHookMethod(wechatEntity.hook_sns_callback_class3, wxClassLoader, wechatEntity.hook_sns_callback_method2, new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, class1, byte[].class, new XC_MethodHook() {

                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    WxEventBus.publish(new WxCollectOtherMoment(wechatEntity, wxClassLoader, param.thisObject, param.args[4]));
                    XposedBridge.log("====朋友圈22===="+param.args[0]);
                    Intent intent = new Intent();
                    intent.setAction("moment");
                    intent.putExtra("source","进入了别人朋友圈");
                    context.sendBroadcast(intent);
                }
            }});
        } catch (Throwable e) {

//            Logger.e(e, "MomentHook ERROR", new Object[0]);
        }
    }
}
