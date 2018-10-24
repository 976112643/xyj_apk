package com.mikuwxc.autoreply.wchook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mikuwxc.autoreply.HookLoader;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.utils.FileUtil;
import com.mikuwxc.autoreply.wcutil.AuthUtil;
import com.mikuwxc.autoreply.wcutil.FileIoUtil;
import com.mikuwxc.autoreply.wcutil.GlobalUtil;
import com.mikuwxc.autoreply.wcutil.OtherUtils;
import com.mikuwxc.autoreply.wcutil.RiskUtil;
import com.orhanobut.logger.Logger;

import net.sqlcipher.BuildConfig;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PreventActivityForRiskAndForbiddenHook {
    public static void hook(LoadPackageParam param) {
        final Context applicationContext = HookLoader.getApplicationContext();
        XposedHelpers.findAndHookMethod("android.app.Application", param.classLoader, "dispatchActivityResumed", new Object[]{Activity.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    XposedBridge.log("ActivityActivityActivityActivity");
                    Activity activity = (Activity) param.args[0];

                    PreventActivityForRiskAndForbiddenHook.handleAppPackageName(applicationContext, activity.getPackageName());
                    XposedBridge.log("activity.getPackageName()::"+activity.getPackageName());
                    String currentClassName = activity.getComponentName().getClassName();
                    XposedBridge.log("android.app.Application:::::"+currentClassName);
                    PreventActivityForRiskAndForbiddenHook.checkIsRisk(applicationContext, currentClassName);
                    PreventActivityForRiskAndForbiddenHook.checkIsHome(applicationContext, currentClassName);
                } catch (Exception e) {
                    XposedBridge.log("eeeeeeee"+e.toString());
                }
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.Instrumentation", param.classLoader, "checkStartActivityResult", new Object[]{Integer.TYPE, Object.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object hookIntent = param.args[1];
                    if (hookIntent instanceof Intent) {
                        Intent intent = (Intent) hookIntent;
                        if (!OtherUtils.isEmpty(intent)) {
                            ComponentName cn = intent.getComponent();
                            XposedBridge.log("android.app.Instrumentation:::::"+cn.getClassName());
                            if (!OtherUtils.isEmpty(cn)) {
                                PreventActivityForRiskAndForbiddenHook.checkIsRisk(applicationContext, cn.getClassName());
                                Bundle bundle = intent.getExtras();
                                for (String set : bundle.keySet()) {
                                    Logger.i("===== Instrumentation key[" + set + "] value[" + bundle.get(set) + "]", new Object[0]);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }});
    }

    private static void handleAppPackageName(Context applicationContext, String packageName) {
        Boolean packageStaue_put=true;
        packageStaue_put = MyFileUtil.readProperties("packageStaue_put");
        XposedBridge.log("packageStaue_putpackageStaue_putpackageStaue_put::"+packageStaue_put);
        if (packageName.contains("com.android.settings")&&packageStaue_put==true){
            RiskUtil.goForbidden(applicationContext, 0);
        }
  /*      if (packageName.contains("com.android.systemui")) {
            if (AuthUtil.isForbiddenAuth(16)) {
                RiskUtil.goForbidden(applicationContext, 0);
            }
        } else if (packageName.contains("com.android.updater")) {
            RiskUtil.goForbidden(applicationContext, 999);
        } else if (!packageName.contains("baidu") && !packageName.contains("android") && !packageName.contains("wechat") && !packageName.contains("system") && !packageName.contains("miui") && !packageName.contains("xiaomi") && !packageName.contains("lenovo") && !packageName.contains("meizu") && !packageName.contains("huawei") && !packageName.equals(BuildConfig.APPLICATION_ID)) {
            String whiteAppList = FileIoUtil.getValueFromPath(GlobalUtil.WHITE_APP_SAVE_PATH);
            if (!OtherUtils.isEmpty(whiteAppList) && !"[]".equals(whiteAppList) && !whiteAppList.contains(packageName)) {
                RiskUtil.goForbidden(applicationContext, 0);
            }
        }*/
    }

    private static void checkIsHome(Context applicationContext, String className) {
        boolean isHome = false;
        int i = -1;
        switch (className.hashCode()) {
            case -1941262704:
                if (className.equals("com.miui.home.launcher.Launcher")) {
                    i = 0;
                    break;
                }
                break;
            case -1635433060:
                if (className.equals("com.meizu.flyme.launcher.Launcher")) {
                    i = 2;
                    break;
                }
                break;
            case 2037544480:
                if (className.equals("com.lenovo.launcher.Launcher")) {
                    i = 1;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                isHome = true;
                break;
            case 1:
                isHome = true;
                break;
            case 2:
                isHome = true;
                break;
        }
        if (isHome && AuthUtil.isForbiddenAuth(1)) {
            RiskUtil.goHome(applicationContext);
        }
    }

    private static void checkIsRisk(Context applicationContext, String className) {
        if ("com.tencent.mobileqq.activity.LoginActivity".equals(className)){
            RiskUtil.goForbidden(applicationContext, 1);
        }

        boolean z = true;
        switch (className.hashCode()) {
            case -2069845153:
                if (className.equals("com.huawei.camera")) {
                    z = false;
                    break;
                }
                break;
            case -1547776564:
                if (className.equals("com.tencent.mm.plugin.voiceprint.ui.VoicePrintFinishUI")) {
                    z = true;
                    break;
                }
                break;
            case -1005668626:
                if (className.equals("com.tencent.mm.plugin.setting.ui.setting.SettingsUI")) {
                    z = true;
                    break;
                }
                break;
            case -962104637:
                if (className.equals("com.tencent.mm.plugin.mmsight.ui.SightCaptureUI")) {
                    z = true;
                    break;
                }
                break;
            case -4446770:
                if (className.equals("com.android.settings.deviceinfo.UsbModeChooserActivity")) {
                    z = true;
                    break;
                }
                break;
            case 128398883:
                if (className.equals("com.tencent.mm.ui.bindmobile.BindMContactStatusUI")) {
                    z = true;
                    break;
                }
                break;
            case 1156173513:
                if (className.equals("com.tencent.mm.plugin.scanner.ui.BaseScanUI")) {
                    z = true;
                    break;
                }
                break;
            case 1516986499:
                if (className.equals("com.tencent.mm.plugin.account.bind.ui.BindMContactStatusUI")) {
                    z = true;
                    break;
                }
                break;
        }
       /* switch (z) {
            case false:
                if (AuthUtil.isForbiddenAuth(4)) {
                    RiskUtil.goForbidden(applicationContext, 1);
                    break;
                }
                break;
          case true:
                RiskActionUtil.send(20, false);
                if (AuthUtil.isForbiddenAuth(3)) {
                    RiskUtil.goForbidden(applicationContext, 1);
                    break;
                }
                break;
            case true:
                RiskActionUtil.send(21, false);
                if (AuthUtil.isForbiddenAuth(14)) {
                    RiskUtil.goForbidden(applicationContext, 1);
                    break;
                }
                break;
            case true:
                RiskActionUtil.send(22, false);
                break;
            case true:
            case true:
                RiskActionUtil.send(23, false);
                break;
            case true:
                if (AuthUtil.isForbiddenAuth(10)) {
                    RiskUtil.goForbidden(applicationContext, 1);
                    break;
                }
                break;
            case true:
                if (AuthUtil.isForbiddenAuth(13)) {
                    RiskUtil.goHome(applicationContext);
                    break;
                }
                break;
            default:
                if (className.contains("settings")) {
                    className = className.toLowerCase();
                    if (!(className.contains("wifi") || className.contains("sub") || className.contains("baidu") || !AuthUtil.isForbiddenAuth(2))) {
                        RiskUtil.goForbidden(applicationContext, 0);
                        break;
                    }
                }
                break;
        }
        FileIoUtil.setValueToPath("send:" + className, false, GlobalUtil.LAST_CLASS_NAME);*/
    }
}
