package com.mikuwxc.autoreply.utils;

import android.content.Context;
import android.provider.Settings;


public class GetImeiUtil {
    public static String getOnlyIdentification(Context context) throws Exception{

        return  Settings.Secure.getString(context.getContentResolver(), "android_id").toUpperCase();
    }
}
