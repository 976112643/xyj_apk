package com.mikuwxc.autoreply.wchook;

import android.os.Handler;

import com.google.gson.Gson;
import com.mikuwxc.autoreply.wcentity.WechatEntity;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class VerifyFriendHook {
    public static Handler handler = new Handler();

    public static void hook(final WechatEntity wechatEntity, LoadPackageParam param) {
        final ClassLoader wxClassLoader = param.classLoader;
        Class class_auto_verify_user = XposedHelpers.findClass(wechatEntity.verify_callback_class1, wxClassLoader);
        Class class_q = XposedHelpers.findClass(wechatEntity.verify_callback_class2, wxClassLoader);
        XposedHelpers.findAndHookMethod(class_auto_verify_user, wechatEntity.verify_callback_method1, new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, class_q, byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
               // WxEventBus.publish(new WxVerifyFriend(wechatEntity, wxClassLoader, VerifyFriendHook.handler, param.thisObject, ((Integer) param.args[1]).intValue(), ((Integer) param.args[2]).intValue()));
               XposedBridge.log("接受好友4："+new Gson().toJson(param.args[4]));
                XposedBridge.log("接受好友0："+new Gson().toJson(param.args[0]));
                XposedBridge.log("接受好友1："+new Gson().toJson(param.args[1]));
                XposedBridge.log("接受好友2："+new Gson().toJson(param.args[2]));
                XposedBridge.log("接受好友3："+new Gson().toJson(param.args[3]));
                XposedBridge.log("接受好友5："+new Gson().toJson(param.args[5]));
            }
        }});
    }
}
