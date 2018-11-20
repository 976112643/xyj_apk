package com.mikuwxc.autoreply.wchook;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

import com.mikuwxc.autoreply.wcentity.WechatEntity;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: WeChatLogoutHook.kt */
public final class WeChatLogoutHook {
    public static final WeChatLogoutHook INSTANCE = new WeChatLogoutHook();

    private WeChatLogoutHook() {
    }

    @JvmStatic
    public static final void hook(@NotNull WechatEntity wechatEntity, @NotNull LoadPackageParam loadPackageParam) {
        Intrinsics.checkParameterIsNotNull(wechatEntity, "wechatEntity");
        Intrinsics.checkParameterIsNotNull(loadPackageParam, "loadPackageParam");
        XposedHelpers.findAndHookConstructor(XposedHelpers.findClass(wechatEntity.wechat_logout_class1, loadPackageParam.classLoader), new Object[]{new WeChatLogoutHook$hook$1()});
        XposedHelpers.findAndHookMethod(wechatEntity.wechat_logout_class2, loadPackageParam.classLoader, wechatEntity.wechat_logout_method1, new Object[]{Context.class, String.class, String.class, OnClickListener.class, OnCancelListener.class, new WeChatLogoutHook$hook$2()});
    }
}
