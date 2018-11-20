package com.mikuwxc.autoreply.wchook;

import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.wcutil.RiskActionUtil;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import org.jetbrains.annotations.Nullable;

/* compiled from: WeChatLogoutHook.kt */
public final class WeChatLogoutHook$hook$1 extends XC_MethodHook {
    WeChatLogoutHook$hook$1() {
    }

    protected void beforeHookedMethod(@Nullable MethodHookParam param) {
        RiskActionUtil.send(24, false);
        XposedBridge.log(("===== LOGOUT"+ new Object[0]));
        MyFileUtil.writeToNewFile(AppConfig.APP_FILE+"/wxno","");
    }
}
