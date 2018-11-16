package com.mikuwxc.autoreply.wchook;

import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.orhanobut.logger.Logger;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ForbiddenWxRootCheck {
    public static void hook(WechatEntity wechatEntity, LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(wechatEntity.forbidden_wx_root_class1, loadPackageParam.classLoader, wechatEntity.forbidden_wx_root_method1, new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Logger.i("===== isSu %s", param.getResult());
                param.setResult(Boolean.valueOf(false));
            }
        }});
    }
}
