package com.mikuwxc.autoreply.wchook;

import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wx.WechatDb;

import org.apache.commons.lang3.StringUtils;


/**
 * 获取微信用户名 昵称 微信id等
 * **/
public class WechatUsernameHook {


    public static void hook(){
        UserEntity userEntity = WechatDb.getInstance().selectSelf();
        String userName = userEntity.getUserName();
        String userTalker = userEntity.getUserTalker();
        String headPic = userEntity.getHeadPic();
        String alias = userEntity.getAlias();  //微信号
        if (StringUtils.isBlank(alias)){
            alias = userTalker;
        }

        StringBuilder sb=new StringBuilder();
        sb.append(userName+",");
        sb.append(userTalker+",");
        sb.append(headPic+",");
        sb.append(alias==""?"null":alias+",");
        MyFileUtil.writeToNewFile(AppConfig.APP_USERNAME,sb.toString());

    }
}
