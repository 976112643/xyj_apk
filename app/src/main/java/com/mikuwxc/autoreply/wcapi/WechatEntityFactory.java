package com.mikuwxc.autoreply.wcapi;


import android.content.Context;

import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcutil.AppUtil;
import com.mikuwxc.autoreply.wcutil.GlobalUtil;

public class WechatEntityFactory {
    private static WechatEntity current;

    public static synchronized WechatEntity create(Context context) {
        WechatEntity wechatEntity;
        synchronized (WechatEntityFactory.class) {
            if (context == null) {
                throw new IllegalArgumentException("context can not be null");
            }
            if (current == null) {
                try {
                    current = create(AppUtil.getAppVersion(context, GlobalUtil.WX_PM));
                } catch (Throwable ex) {
                    RuntimeException runtimeException = new RuntimeException(ex.getMessage(), ex);
                }
            }
            wechatEntity = current;
        }
        return wechatEntity;
    }

    public static WechatEntity create(String str) {
        WechatEntity wechatEntity6513;
        Object obj = -1;
        switch (str.hashCode()) {
            case 51293888:
                if (str.equals("6.6.0")) {
                    obj = 7;
                    break;
                }
                break;
            case 51293889:
                if (str.equals("6.6.1")) {
                    obj = 8;
                    break;
                }
                break;
            case 51293890:
                if (str.equals("6.6.2")) {
                    obj = 9;
                    break;
                }
                break;
            case 51293891:
                if (str.equals("6.6.3")) {
                    obj = 10;
                    break;
                }
                break;
            case 51293893:
                if (str.equals("6.6.5")) {
                    obj = 11;
                    break;
                }
                break;
            case 51293894:
                if (str.equals("6.6.6")) {
                    obj = 12;
                    break;
                }
                break;
            case 51293895:
                if (str.equals("6.6.7")) {
                    obj = 13;
                    break;
                }
                break;
            case 51294849:
                if (str.equals("6.7.0")) {
                    obj = 14;
                    break;
                }
                break;
            case 51294851:
                if (str.equals("6.7.2")) {
                    obj = 15;
                    break;
                }
                break;

            case 51294852:
                if (str.equals("6.7.3")) {
                    obj = 16;
                    break;
                }
                break;


            case 1590021297:
                if (str.equals("6.3.31")) {
                    obj = null;
                    break;
                }
                break;
            case 1590080819:
                if (str.equals("6.5.13")) {
                    obj = 1;
                    break;
                }
                break;
            case 1590080820:
                if (str.equals("6.5.14")) {
                    obj = 2;
                    break;
                }
                break;
            case 1590080822:
                if (str.equals("6.5.16")) {
                    obj = 3;
                    break;
                }
                break;
            case 1590080825:
                if (str.equals("6.5.19")) {
                    obj = 4;
                    break;
                }
                break;
            case 1590080849:
                if (str.equals("6.5.22")) {
                    obj = 5;
                    break;
                }
                break;
            case 1590080850:
                if (str.equals("6.5.23")) {
                    obj = 6;
                    break;
                }
                break;
        }
        switch (Integer.parseInt(obj.toString())) {
            case 0:
                wechatEntity6513 = new WechatEntity6513();
                break;
            case 1:
                wechatEntity6513 = new WechatEntity6513();
                break;
            case 2:
                wechatEntity6513 = new WechatEntity6514();
                break;
            case 3:
                wechatEntity6513 = new WechatEntity6516();
                break;
            case 4:
                wechatEntity6513 = new WechatEntity6519();
                break;
            case 5:
                wechatEntity6513 = new WechatEntity6522();
                break;
            case 6:
                wechatEntity6513 = new WechatEntity6523();
                break;
            case 7:
                wechatEntity6513 = new WechatEntity6600();
                break;
            case 8:
                wechatEntity6513 = new WechatEntity6601();
                break;
            case 9:
                wechatEntity6513 = new WechatEntity6602();
                break;
            case 10:
                wechatEntity6513 = new WechatEntity6603();
                break;
            case 11:
                wechatEntity6513 = new WechatEntity6605();
                break;
            case 12:
                wechatEntity6513 = new WechatEntity6606();
                break;
            case 13:
                wechatEntity6513 = new WechatEntity6607();
                break;
            case 14:
                wechatEntity6513 = new WechatEntity6700();
                break;
            case 15:
                wechatEntity6513 = new WechatEntity6702();
                break;
            case 16:
                wechatEntity6513 = new WechatEntity6703();
                break;
            default:
                wechatEntity6513 = new WechatEntityDefault();
                break;
        }
        if (str.startsWith("6.5.7")) {
            wechatEntity6513.sqlitedatabase_class_name = "com.tencent.mmdb.database.SQLiteDatabase";
            wechatEntity6513.cancellationsignal_class_name = "com.tencent.mmdb.support.CancellationSignal";
            wechatEntity6513.sqlitecipherspec_class_name = "com.tencent.mmdb.database.SQLiteCipherSpec";
            wechatEntity6513.sqlitedatabase$cursorfactory_class_name = "com.tencent.mmdb.database.SQLiteDatabase$CursorFactory";
            wechatEntity6513.databaseerrorhandler_class_name = "com.tencent.mmdb.DatabaseErrorHandler";
        }
        return wechatEntity6513;
    }
}