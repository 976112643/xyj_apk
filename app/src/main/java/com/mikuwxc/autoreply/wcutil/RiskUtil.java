package com.mikuwxc.autoreply.wcutil;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.sqlcipher.database.SQLiteDatabase;

public class RiskUtil {
/*    public static void insertRisk(JSONObject js) {
        String talker = js.getString("talker");
        int riskType = js.getIntValue("riskOperateType");
        UserEntity userEntity;
        if (!OtherUtils.isEmpty(talker)) {
            userEntity = UserManager.getInstance().selectUser();
            if (!OtherUtils.isEmpty(userEntity)) {
                RiskManager.getInstance().insertRisk(createRisk(userEntity.getUserTalker(), js.getLongValue("operateTime"), js.getString("extra"), js.getString("talker"), riskType));
            }
        } else if (riskType != 0) {
            userEntity = UserManager.getInstance().selectUser();
            RiskManager.getInstance().insertRisk(createRisk(OtherUtils.isEmpty(userEntity) ? "" : userEntity.getUserTalker(), System.currentTimeMillis(), "", "", riskType));
        }
    }

    public static RiskEntity createRisk(String selfId, long operateTime, String extra, String talker, int riskOperateType) {
        RiskEntity riskEntity = new RiskEntity();
        riskEntity.setRiskId(UUID.randomUUID().toString());
        riskEntity.setOperateTime(operateTime);
        riskEntity.setExtra(extra);
        riskEntity.setTalker(talker);
        riskEntity.setWechatId(selfId);
        riskEntity.setRiskOperateType(riskOperateType);
        return riskEntity;
    }*/

    @SuppressLint("WrongConstant")
    public static void goHome(Context applicationContext) {
        Intent homeIntent = new Intent(GlobalUtil.ACTION_GOBACK_ACTIVITY);
        homeIntent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        applicationContext.startActivity(homeIntent);
    }

    @SuppressLint("WrongConstant")
    public static void goForbidden(Context applicationContext, int isReturnWx) {
       /* Intent intent = new Intent("com.mikuwxc.autoreply.activity.AuthorityActivity");
        //intent.putExtra("isReturnWx", isReturnWx);
        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        applicationContext.startActivity(intent);*/


        ComponentName componentName = new ComponentName(
                "com.mikuwxc.autoreply",   //要去启动的App的包名
                "com.mikuwxc.autoreply.activity.AuthorityActivity");
        //要去启动的App中的Activity的类名
        // ComponentName : 参数说明
        //组件名称，第一个参数是包名，也是主配置文件Manifest里设置好的包名
        //第二个是类名，要带上包名
        Intent intent1 = new Intent();
        Bundle bundle = new Bundle();
        intent1.setComponent(componentName);
        intent1.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        applicationContext.startActivity(intent1);
        //applicationContext.finish();
    }
}
