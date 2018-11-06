package com.mikuwxc.autoreply.utils;

import com.mikuwxc.autoreply.wcutil.ArrayUtil;
import com.mikuwxc.autoreply.wcutil.Rcontactlvbuff;

import org.apache.commons.lang3.StringUtils;

public class ParseUtil {
    public static String parseRegion(byte[] blob){
        StringBuilder sb = new StringBuilder();

        int index48 = blob[48];
        if(index48 < 0){
            index48 &= 255;
        }

        int indexLocationLength = blob[(index48 + 48) + 2];
        if(indexLocationLength == 0){
            return sb.toString();
        }

        int startIndex = ((index48 + 48) + 2) + 1;
        int endIndex   = startIndex + indexLocationLength;

        sb.append(new String(ArrayUtil.copyOfRange(blob, startIndex, endIndex)));

        startIndex = endIndex + 2;
        sb.append(new String(ArrayUtil.copyOfRange(blob, startIndex, startIndex + blob[endIndex + 1])));

        return sb.toString();
    }

    public static String parseProvince(byte[] blob){
        StringBuilder sb = new StringBuilder();

        int index48 = blob[48];
        if(index48 < 0){
            index48 &= 255;
        }

        int indexLocationLength = blob[(index48 + 48) + 2];
        if(indexLocationLength == 0){
            return sb.toString();
        }

        int startIndex = ((index48 + 48) + 2) + 1;
        int endIndex   = startIndex + indexLocationLength;

        sb.append(new String(ArrayUtil.copyOfRange(blob, startIndex, endIndex)));

        return sb.toString();
    }

    public static String parseCity(byte[] blob){
        StringBuilder sb = new StringBuilder();

        int index48 = blob[48];
        if(index48 < 0){
            index48 &= 255;
        }

        int indexLocationLength = blob[(index48 + 48) + 2];
        if(indexLocationLength == 0){
            return sb.toString();
        }

        int startIndex = ((index48 + 48) + 2) + 1;
        int endIndex   = startIndex + indexLocationLength;

        startIndex = endIndex + 2;
        sb.append(new String(ArrayUtil.copyOfRange(blob, startIndex, startIndex + blob[endIndex + 1])));

        return sb.toString();
    }

    public static String parsePhone(byte[] blob){
        Rcontactlvbuff buff = new Rcontactlvbuff();

        buff.field_lvbuff = blob;
        buff.pI();

        return (StringUtils.isBlank(buff.bAX) || "null".equals(buff.bAX) ? "" : buff.bAX);
    }

    public static int parseFrom(byte[] blob){
        Rcontactlvbuff buff = new Rcontactlvbuff();

        buff.field_lvbuff = blob;
        buff.pI();

        return buff.bbt;
    }

}
