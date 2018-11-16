package com.mikuwxc.autoreply.wchook;

import android.content.ContentValues;

import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcutil.Throttle;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import org.jetbrains.annotations.NotNull;

/* compiled from: ContactsChangedHook.kt */
public final class ContactsChangedHook {
    public static final ContactsChangedHook INSTANCE = new ContactsChangedHook();

    private ContactsChangedHook() {
    }

    @JvmStatic
    public static final void hook(@NotNull WechatEntity wechatEntity, @NotNull LoadPackageParam param) {
        Intrinsics.checkParameterIsNotNull(wechatEntity, "wechatEntity");
        Intrinsics.checkParameterIsNotNull(param, "param");
        Throttle throttle = new Throttle(10000, ContactsChangedHook$hook$throttle$1.INSTANCE);
        String dbClassName = wechatEntity.sqlitedatabase_class_name;
        XposedHelpers.findAndHookMethod(dbClassName, param.classLoader, "insertWithOnConflict", new Object[]{String.class, String.class, ContentValues.class, JvmClassMappingKt.getJavaPrimitiveType(Reflection.getOrCreateKotlinClass(Integer.TYPE)), new ContactsChangedHook$hook$1(throttle)});
        XposedHelpers.findAndHookMethod(dbClassName, param.classLoader, "updateWithOnConflict", new Object[]{String.class, ContentValues.class, String.class, String[].class, JvmClassMappingKt.getJavaPrimitiveType(Reflection.getOrCreateKotlinClass(Integer.TYPE)), new ContactsChangedHook$hook$2(throttle)});
    }
}
