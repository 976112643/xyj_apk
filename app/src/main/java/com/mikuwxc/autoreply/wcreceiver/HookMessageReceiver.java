package com.mikuwxc.autoreply.wcreceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.mikuwxc.autoreply.modle.HookMessageBean;
import com.mikuwxc.autoreply.receiver.Constance;
import com.mikuwxc.autoreply.utils.UpYunUtil;
import com.mikuwxc.autoreply.wcutil.Tag;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import static com.android.volley.VolleyLog.TAG;

public class HookMessageReceiver extends BroadcastReceiver {
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();
        mContext = context;
        String hookMessageBeanJson = intent.getStringExtra("hookMessageBeanJson");
        if(action.equals(Constance.getAction_hookmessage)) {
            HookMessageBean hookMessageBean = new Gson().fromJson(hookMessageBeanJson, HookMessageBean.class);
            Log.i(TAG,hookMessageBean.toString());
            if (StringUtils.isBlank(hookMessageBean.getUserNameChatroom())){
                hookMessageBean.setUserNameChatroom("");
            }
            String msgType = hookMessageBean.getMsgType();
            if ("43".equals(msgType)){  //监听视频上传
                parseVideoInfo(hookMessageBean);
            }else if ("3".equals(hookMessageBean.getMsgType())){//监听图片上传
                parsePicInfo(hookMessageBean);
            }else if ("34".equals(hookMessageBean.getMsgType())){
                parseVoiseInfo(hookMessageBean);
            }else if ("49".equals(hookMessageBean.getMsgType())){
                parseFileInfo(hookMessageBean);
            }
        }
    }

    private void parseFileInfo(final HookMessageBean hookMessageBean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long fileSize = hookMessageBean.getFileSize();
                File file=new File(hookMessageBean.getContent());

                while (file.length()<fileSize){
                    file=new File(hookMessageBean.getContent());
                    Log.e(TAG,"文件大小"+file.length()+"<"+fileSize);
                }
                if (file.length()<fileSize){
                    Log.e(TAG,"文件大小"+file.length()+"<"+fileSize);
                }else{
                    Log.e(TAG,"文件大小"+file.length()+">="+fileSize);
                    if (!file.exists()){
                        try {
                            Thread.sleep(5000);
                            UpYunUtil.uploadFile(hookMessageBean.getContent(),hookMessageBean.getUsername(),hookMessageBean.getSign(),0,
                                    hookMessageBean.getStatus(),hookMessageBean.getUsername(),hookMessageBean.getMsgType(),hookMessageBean.getConversationTime(),
                                    hookMessageBean.getUserNameChatroom(),hookMessageBean.getMsgId(),mContext);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        UpYunUtil.uploadFile(hookMessageBean.getContent(),hookMessageBean.getUsername(),hookMessageBean.getSign(),0,
                                hookMessageBean.getStatus(),hookMessageBean.getUsername(),hookMessageBean.getMsgType(),hookMessageBean.getConversationTime(),
                                hookMessageBean.getUserNameChatroom(),hookMessageBean.getMsgId(),mContext);
                    }
                }
            }
        }).start();



    }

    private void parseVoiseInfo(HookMessageBean hookMessageBean) {
        UpYunUtil.uploadAmr(hookMessageBean.getContent(),hookMessageBean.getUsername(),hookMessageBean.getSign(),0,
                hookMessageBean.getStatus(),hookMessageBean.getUsername(),hookMessageBean.getMsgType(),hookMessageBean.getConversationTime(),
                hookMessageBean.getUserNameChatroom(),hookMessageBean.getMsgId(),mContext);
    }

    private void parsePicInfo(HookMessageBean hookMessageBean) {

        File file=new File(hookMessageBean.getContent());
        if (!file.exists()){
            try {
                Thread.sleep(5000);
                UpYunUtil.uploadPic(hookMessageBean.getContent(),hookMessageBean.getUsername(),hookMessageBean.getSign(),0,
                        hookMessageBean.getStatus(),hookMessageBean.getUsername(),hookMessageBean.getMsgType(),hookMessageBean.getConversationTime(),
                        hookMessageBean.getUserNameChatroom(),hookMessageBean.getMsgId(),mContext);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            UpYunUtil.uploadPic(hookMessageBean.getContent(),hookMessageBean.getUsername(),hookMessageBean.getSign(),0,
                    hookMessageBean.getStatus(),hookMessageBean.getUsername(),hookMessageBean.getMsgType(),hookMessageBean.getConversationTime(),
                    hookMessageBean.getUserNameChatroom(),hookMessageBean.getMsgId(),mContext);
        }

    }

    private void parseVideoInfo(final HookMessageBean hookMessageBean) {
        UpYunUtil.handleMessage(0, hookMessageBean.getStatus(), hookMessageBean.getUsername(), "", hookMessageBean.getMsgType(),
                hookMessageBean.getConversationTime(),hookMessageBean.getMsgId(),mContext);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long fileSize = hookMessageBean.getFileSize();
                File file=new File(hookMessageBean.getContent());
                while (file.length()<fileSize){
                    file=new File(hookMessageBean.getContent());
                    Log.e(TAG,"文件大小"+file.length()+"<"+fileSize);
                }
                if (file.length()<fileSize){
                    Log.e(TAG,"文件大小"+file.length()+"<"+fileSize);
                }else {
                    Log.e(TAG, "文件大小" + file.length() + ">=" + fileSize);
                    UpYunUtil.uploadVideo(hookMessageBean.getContent(),hookMessageBean.getUsername(),hookMessageBean.getSign(),0,
                            hookMessageBean.getStatus(),hookMessageBean.getUsername(),hookMessageBean.getMsgType(),hookMessageBean.getConversationTime(),
                            hookMessageBean.getUserNameChatroom(),hookMessageBean.getMsgId(),mContext);
                }


            }
        }).start();

    }


}
