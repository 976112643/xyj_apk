package com.mikuwxc.autoreply.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * 系统工具类 
 * Created by zhuwentao on 2016-07-18. 
 */  
public class SystemUtil {


    /** 
     * 获取当前手机系统语言。 
     * 
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN” 
     */  
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }  
  
    /** 
     * 获取当前系统上的语言列表(Locale列表) 
     * 
     * @return  语言列表 
     */  
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }  
  
    /** 
     * 获取当前手机系统版本号 
     * 
     * @return  系统版本号 
     */  
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;  
    }  
  
    /** 
     * 获取手机型号 
     * 
     * @return  手机型号 
     */  
    public static String getSystemModel() {
        return android.os.Build.MODEL;  
    }  
  
    /** 
     * 获取手机厂商 
     * 
     * @return  手机厂商 
     */  
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;  
    }  
  
    /** 
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限) 
     * 
     * @return  手机IMEI 
     */  
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {  
            return tm.getDeviceId();  
        }  
        return null;  
    }


    public static String getPhone(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = tm.getDeviceId();//获取智能设备唯一编号
        String te1  = tm.getLine1Number();//获取本机号码
        String imei = tm.getSimSerialNumber();//获得SIM卡的序号
        String imsi = tm.getSubscriberId();//得到用户Id
        return te1;
    }


    public static String getAppVersionName(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }


}  