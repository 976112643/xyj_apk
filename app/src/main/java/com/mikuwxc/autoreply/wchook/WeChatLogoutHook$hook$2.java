package com.mikuwxc.autoreply.wchook;

import com.orhanobut.logger.Logger;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/* compiled from: WeChatLogoutHook.kt */
public final class WeChatLogoutHook$hook$2 extends XC_MethodHook {
    WeChatLogoutHook$hook$2() {
    }

    protected void afterHookedMethod(@Nullable MethodHookParam param) {
        String str = "===== com.tencent.mm.ui.base.h %s";
        Object[] objArr = new Object[1];
        if (param == null) {
            Intrinsics.throwNpe();
        }
        objArr[0] = param.args[1];
        XposedBridge.log(str+objArr);
        XposedBridge.log("===== com.tencent.mm.ui.base.h %s"+ param.args[2]);
        Object obj = param.args[1];
        if (obj == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.String");
        } else if (StringUtils.startsWith((String) obj, "你的微信帐号于")) {
          //  WxEventBus.publish(new WxLogout());
        }
    }
}
