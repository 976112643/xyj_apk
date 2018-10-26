package com.mikuwxc.autoreply.wchook;

import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wx.WechatDb;

import org.apache.commons.lang3.StringUtils;

import de.robv.android.xposed.XposedBridge;


/**
 * 获取微信用户名 昵称 微信id等
 * **/
public class WechatUsernameHook {


    public static void hook(){
        try {
            UserEntity userEntity = WechatDb.getInstance().selectSelf();
            String userName = userEntity.getUserName();//用户名 说了你
            String userTalker = userEntity.getUserTalker();//微信id wxid_kwd8tbhbbgsr22
            String headPic = userEntity.getHeadPic();//头像
            String alias = userEntity.getAlias();  //微信号 a54414512
            if (StringUtils.isBlank(alias)) {
                alias = userTalker;
            }

            StringBuilder sb=new StringBuilder();
            sb.append(userName+",");
            sb.append(userTalker+",");
            sb.append(headPic+",");
            sb.append(alias==""?"null":alias+",");
            MyFileUtil.writeToNewFile(AppConfig.APP_USERNAME,sb.toString());
        }catch (Exception e){
            XposedBridge.log(e.getMessage()+"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
        }



    }
}
