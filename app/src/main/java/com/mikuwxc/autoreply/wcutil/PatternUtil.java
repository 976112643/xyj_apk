package com.mikuwxc.autoreply.wcutil;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PatternUtil {
    private static final String PHONE_REGEX = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$";

    public static boolean isPhoneNumber(String str){
        if (StringUtils.isBlank(str)){
            return false;
        }


        Pattern pattern  = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(str);

        return  matcher.matches();
    }
}
