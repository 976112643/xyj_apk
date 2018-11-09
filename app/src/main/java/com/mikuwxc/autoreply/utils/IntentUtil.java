package com.mikuwxc.autoreply.utils;


import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.mikuwxc.autoreply.modle.HookMessageBean;
import com.mikuwxc.autoreply.receiver.Constance;

import de.robv.android.xposed.XposedBridge;

public class IntentUtil {
    public static void  startBroadcastReceiver(String path, String createTime, String msgId, int status, String type, String talker,
                                               String content, String isSend, Context mContext,String sign){


        Intent intent=new Intent();
        intent.setClassName(Constance.packageName_me, Constance.receiver_message);
        intent.setAction(Constance.getAction_hookmessage);
        HookMessageBean hookMessageBean=new HookMessageBean();
        hookMessageBean.setContent(path);
        hookMessageBean.setConversationTime(Long.parseLong(createTime));
        hookMessageBean.setMsgId(msgId);
        hookMessageBean.setStatus(status);
        hookMessageBean.setMsgType(type);
        hookMessageBean.setUsername(talker);
        hookMessageBean.setSign(sign);
        if (talker!=null&&talker.contains("@chatroom")&&"0".equals(isSend)){
            String name[]=content.split(":");
            String userNameChatroom=name[0]+":";
            XposedBridge.log("chatroomContent"+name[0]);
            hookMessageBean.setUserNameChatroom(userNameChatroom);
        }
        String hookMessageBeanJson = JSON.toJSONString(hookMessageBean);
        intent.putExtra("hookMessageBeanJson",hookMessageBeanJson);
        mContext.sendBroadcast(intent);
    }

    public static void startNochatRoomBroadcastReceiver(String path, String createTime, String msgId, int status, String type, String talker,
                                               Context mContext,String sign){

        Intent intent=new Intent();
        intent.setClassName(Constance.packageName_me, Constance.receiver_message);
        intent.setAction(Constance.getAction_hookmessage);
        HookMessageBean hookMessageBean=new HookMessageBean();
        hookMessageBean.setContent(path);
        hookMessageBean.setConversationTime(Long.parseLong(createTime));
        hookMessageBean.setMsgId(msgId);
        hookMessageBean.setStatus(status);
        hookMessageBean.setMsgType(type);
        hookMessageBean.setUsername(talker);
        hookMessageBean.setSign(sign);
        String hookMessageBeanJson = JSON.toJSONString(hookMessageBean);
        intent.putExtra("hookMessageBeanJson",hookMessageBeanJson);
        mContext.sendBroadcast(intent);
    }
}
