package com.mikuwxc.autoreply.wchook;

import android.content.ContentValues;

import com.mikuwxc.autoreply.wcentity.RequestParameters;
import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcutil.Throttle;

import org.jetbrains.annotations.NotNull;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;

/* compiled from: ChatroomChangedHook.kt */
public final class ChatroomChangedHook {
    public static final ChatroomChangedHook INSTANCE = new ChatroomChangedHook();

    private ChatroomChangedHook() {
    }

    @JvmStatic
    public static final void hook(@NotNull WechatEntity wechatEntity, @NotNull LoadPackageParam param) {
        Intrinsics.checkParameterIsNotNull(wechatEntity, "wechatEntity");
        Intrinsics.checkParameterIsNotNull(param, "param");
        String dbClassName = wechatEntity.sqlitedatabase_class_name;
        Throttle throttle = new Throttle(10000, ChatroomChangedHook$hook$throttle$1.INSTANCE);
        XposedHelpers.findAndHookMethod(dbClassName, param.classLoader, "insertWithOnConflict", new Object[]{String.class, String.class, ContentValues.class, JvmClassMappingKt.getJavaPrimitiveType(Reflection.getOrCreateKotlinClass(Integer.TYPE)), new ChatroomChangedHook$hook$1(throttle)});
        XposedHelpers.findAndHookMethod(dbClassName, param.classLoader, "updateWithOnConflict", new Object[]{String.class, ContentValues.class, String.class, String[].class, JvmClassMappingKt.getJavaPrimitiveType(Reflection.getOrCreateKotlinClass(Integer.TYPE)), new ChatroomChangedHook$hook$2(throttle)});
        XposedHelpers.findAndHookMethod(dbClassName, param.classLoader, RequestParameters.SUBRESOURCE_DELETE, new Object[]{String.class, String.class, String[].class, new ChatroomChangedHook$hook$3(throttle)});
    }
}
