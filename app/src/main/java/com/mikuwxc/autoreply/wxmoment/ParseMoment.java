package com.mikuwxc.autoreply.wxmoment;

import android.os.Environment;

import com.mikuwxc.autoreply.utils.ShellUtils;
import com.mikuwxc.autoreply.wcutil.FileIoUtil;
import com.mikuwxc.autoreply.wcutil.GlobalUtil;

import java.io.DataOutputStream;
import java.io.IOException;

public class ParseMoment {
    public static void copySnsMicroMsgDB() {

        String EXT_DIR = Environment.getExternalStorageDirectory() + "/WeChatMomentStat/SnsMicroMsg.db";
        String uniqueName = FileIoUtil.getValueFromPath(GlobalUtil.WX_UNIQUENAME_SAVE_PATH);
        String dbPath = GlobalUtil.WX_BASE_PATH_ + uniqueName + "/";

        String permissionPath=GlobalUtil.WX_BASE_PATH_ + uniqueName + "/SnsMicroMsg.db";
        try {
            Process su = Runtime.getRuntime().exec("su");//执行超级管理员root
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("chmod 777 " + permissionPath);//修改WeChatMomentStat的执行权限

            outputStream.writeBytes("exit\n");//退出命令
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ShellUtils.execCommand("cp " + dbPath + "SnsMicroMsg.db " + EXT_DIR, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
