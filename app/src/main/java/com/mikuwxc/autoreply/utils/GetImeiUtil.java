package com.mikuwxc.autoreply.utils;

import android.content.Context;
import android.provider.Settings;


public class GetImeiUtil {
    public static String getOnlyIdentification(Context context) throws Exception{

       /* TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            return tm.getDeviceId();
        }*/
        return  Settings.Secure.getString(context.getContentResolver(), "android_id").toUpperCase();

        // return Build.SERIAL;
    }
}
