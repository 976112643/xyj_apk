package com.mikuwxc.autoreply.utils;

import android.app.Activity;
import android.content.Intent;

import com.mikuwxc.autoreply.activity.ZxingActivity;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;

import org.apache.commons.lang3.StringUtils;

public class CheckUtil {
    public static void selectActivity(Activity activity){
        String tenantConfig = MyFileUtil.readFromFile(AppConfig.APP_FILE + "/tenantConfig");
        if (StringUtils.isBlank(tenantConfig)) {
            Intent intent=new Intent(activity,ZxingActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }else{

        }
    }
}
