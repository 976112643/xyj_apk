package com.mikuwxc.autoreply.wchook;

import com.mikuwxc.autoreply.wcentity.WechatEntity;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class WxXposedHook {
    public static void hook(WechatEntity wechatEntity, LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(wechatEntity.forbidden_xposed_class1, loadPackageParam.classLoader), wechatEntity.forbidden_xposed_method1, new Object[]{StackTraceElement[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(Boolean.valueOf(false));
            }
        }});
    }
}
