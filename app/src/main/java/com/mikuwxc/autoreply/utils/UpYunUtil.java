package com.mikuwxc.autoreply.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.LogUtils;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.modle.HookMessageBean;
import com.mikuwxc.autoreply.modle.HttpBean;
import com.mikuwxc.autoreply.receiver.Constance;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wx.WechatDb;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

import static com.android.volley.VolleyLog.TAG;

public class UpYunUtil {
    //上传自己发送的视频
    public static String uploadVideo(String sendVideoPath, final String username, final String sign, final int field_unReadCount,
                               final int field_status, final String field_username, final String field_msgType, final long field_conversationTime , final String userNameChatroom, final String msgId, final Context context) {

            try {
                Thread.sleep(10000);
                int index       = sendVideoPath.lastIndexOf("/");
                String fileName = sendVideoPath.substring(index);
                String copyFile = "/storage/emulated/0/JCM" + fileName;
                try{
                    FileInputStream input  = new FileInputStream(sendVideoPath);
                    FileOutputStream output = new FileOutputStream(copyFile);

                    byte[] buffer = new byte[4096];
                    int    length = 0;
                    while((length = input.read(buffer)) > 0){
                        output.write(buffer, 0, length);
                    }
                }catch(Exception e){
                    Log.e("111","FileFile::"+e.toString());
                }
                File temp = null;
                temp = new File(copyFile);
                final Map<String, Object> paramsMap = new HashMap<>();
                //上传又拍云的命名空间
                paramsMap.put(Params.BUCKET, "cloned");
                final String newVideoLast = sendVideoPath.substring(sendVideoPath.lastIndexOf("/")+1);
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate);
                //又拍云的保存路径，任选其中一个
                final String savePath="/videoforapp/"+username+"/"+sign+"/"+str+"/"+newVideoLast;
                paramsMap.put(Params.SAVE_KEY, savePath);
                //时间戳加上15秒
                paramsMap.put(Params.EXPIRATION, System.currentTimeMillis()+15);
                final boolean[] flag = {false};
                //进度回调，可为空
                UpProgressListener progressListener = new UpProgressListener() {
                    @Override
                    public void onRequestProgress(final long bytesWrite, final long contentLength) {
                        Log.e(TAG, (100 * bytesWrite) / contentLength + "%");
                        Log.e(TAG, bytesWrite + "::" + contentLength);
                    }
                };
                //结束回调，不可为空
                final File finalTemp = temp;
                UpCompleteListener completeListener = new UpCompleteListener() {
                    @Override
                    public void onComplete(boolean isSuccess, String result) {
                        // textView.setText(isSuccess + ":" + result);
                        Log.e(TAG, isSuccess + ":" + result);
                        String newSavePath= AppConfig.YOUPAIYUN+savePath;

                        handleMessage(field_unReadCount, field_status, field_username, userNameChatroom+newSavePath, field_msgType, field_conversationTime,msgId,context);

                    }




                };
                //表单上传（本地签名方式）
                UploadEngine.getInstance().formUpload(temp,paramsMap , "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return "";
    }




    public static String uploadFile(String newFilePath, final String username, final String sign, final int field_unReadCount,
                              final int field_status, final String field_username, final String field_msgType, final long field_conversationTime, final String userNameChatroom, final String msgId, final Context context) {

        int index       = newFilePath.lastIndexOf("/");
        String fileName = newFilePath.substring(index);
        String copyFile = "/storage/emulated/0/JCM" + fileName;
        try{
            FileInputStream input  = new FileInputStream(newFilePath);
            FileOutputStream output = new FileOutputStream(copyFile);

            byte[] buffer = new byte[4096];
            int    length = 0;
            while((length = input.read(buffer)) > 0){
                output.write(buffer, 0, length);
            }
        }catch(Exception e){
            Log.e("111","FileFile::"+e.toString());
        }


        File temp = null;
        temp = new File(copyFile);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");

        final String newFileLast = newFilePath.substring(newFilePath.lastIndexOf("/")+1);

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        //又拍云的保存路径，任选其中一个
        final String savePath="/fileforapp/"+username+"/"+sign+"/"+str+"/"+newFileLast;

        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis()+15);
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                Log.e(TAG, (100 * bytesWrite) / contentLength + "%");
                Log.e(TAG, bytesWrite + "::" + contentLength);
            }
        };
        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                // textView.setText(isSuccess + ":" + result);
                Log.e(TAG, isSuccess + ":" + result);



                String newSavePath= AppConfig.YOUPAIYUN+savePath;
                handleMessage(field_unReadCount, field_status, field_username, userNameChatroom+newSavePath, field_msgType, field_conversationTime,msgId,context);
            }
        };

        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp,paramsMap , "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);


        return "";
    }


    //上传又拍云语音文件
    public static String uploadAmr(String amrPath, final String username, final String sign, final int field_unReadCount,
                             final int field_status, final String field_username, final String field_msgType, final long field_conversationTime, final String userNameChatroom, final String msgId, final Context context) {
        File temp = null;
        temp = new File(amrPath);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");


        final String newArmLast = amrPath.substring(amrPath.lastIndexOf("/")+1);

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        //又拍云的保存路径，任选其中一个
       final String savePath="/amrforapp/"+username+"/"+sign +"/"+str+"/" +newArmLast;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis()+15);
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                Log.e(TAG, (100 * bytesWrite) / contentLength + "%");
                Log.e(TAG, bytesWrite + "::" + contentLength);
            }
        };
        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                // textView.setText(isSuccess + ":" + result);
                Log.e(TAG, isSuccess + ":" + result);
                String newSavePath= AppConfig.YOUPAIYUN+savePath;
                handleMessage(field_unReadCount, field_status, field_username, userNameChatroom+newSavePath, field_msgType, field_conversationTime,msgId,context);
            }
        };

        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp,paramsMap , "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);



        return "";
    }


    public static String uploadPic(String newPicPath, String username, final String sign, final int field_unReadCount,
                             final int field_status, final String field_username, final String field_msgType, final long field_conversationTime, final String userNameChatroom, final String msgId, final Context context) {
        int index       = newPicPath.lastIndexOf("/");
        String fileName = newPicPath.substring(index);
        String copyFile = "/storage/emulated/0/JCM" + fileName;
        try{
            FileInputStream input  = new FileInputStream(newPicPath);
            FileOutputStream output = new FileOutputStream(copyFile);

            byte[] buffer = new byte[4096];
            int    length = 0;
            while((length = input.read(buffer)) > 0){
                output.write(buffer, 0, length);
            }
        }catch(Exception e){
            Log.e("111","FileFile::"+e.toString());
        }

        File temp = null;
        temp = new File(copyFile);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传又拍云的命名空间
        paramsMap.put(Params.BUCKET, "cloned");


        String newPicLast = newPicPath.substring(newPicPath.lastIndexOf("/")+1);

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        //又拍云的保存路径，任选其中一个
       final String savePath="/picforapp/"+username+"/"+sign+"/"+str+"/"+newPicLast;
        paramsMap.put(Params.SAVE_KEY, savePath);
        //时间戳加上15秒
        paramsMap.put(Params.EXPIRATION, System.currentTimeMillis()+15);
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                Log.e(TAG, (100 * bytesWrite) / contentLength + "%");
                Log.e(TAG, bytesWrite + "::" + contentLength);
            }
        };
        //结束回调，不可为空
        final File finalTemp = temp;
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                // textView.setText(isSuccess + ":" + result);
                Log.e(TAG, isSuccess + ":" + result);

                String newSavePath= AppConfig.YOUPAIYUN+savePath;
                handleMessage(field_unReadCount, field_status, field_username, userNameChatroom+newSavePath, field_msgType, field_conversationTime,msgId,context);

                if (finalTemp.exists()){
                    finalTemp.delete();
                }

            }
        };

        //表单上传（本地签名方式）
        UploadEngine.getInstance().formUpload(temp,paramsMap , "unesmall", UpYunUtils.md5("unesmall123456"), completeListener, progressListener);


        return "";
    }


    public static void handleMessage(int unreadCount, int status, String username, String content, String msgType, long conversationTime, String msgId, Context context) {
        getToken();
        saveMessage(status, msgType, username, content, conversationTime,msgId ,0,context);
    }


    private static String getToken() {
          String  token = MyFileUtil.readFromFile(AppConfig.APP_FOLDER + "/token");
            token = token.substring(1, token.length() - 1);

            return token;
    }




    /**
     * 保存信息到后台
     * @param status
     * @param msgType
     * @param username
     * @param content
     * @param conversationTime
     * @param index
     */
    private static void saveMessage(final int status, final String msgType, final String username, final String content, final long conversationTime, final String msgId, final int index, final Context context) {
        Log.e(TAG,content);
        if (getToken() == null) return;
        if ((content.contains("现在可以开始聊天了") || content.contains("accepted your friend request")) && msgType.equals("10000") && status == 4) {
            //刚刚把你添加到通讯录，现在可以开始聊天了
            //你已添加了*******，现在可以开始聊天了
            String nickname = "";
            if (content.contains("刚刚把你添加到通讯录，现在可以开始聊天了")) {
                nickname = content.substring(0, content.length() - 21);
            } else if (content.contains("你已添加了") && content.contains("，现在可以开始聊天了")) {
                nickname = content.substring(0, content.length() - 11);
                nickname = nickname.substring(5, nickname.length());
            } else {
                nickname = content;
            }


            UserEntity userEntity = WechatDb.getInstance().selectSelf();
            String userName = userEntity.getUserName();
            String userTalker = userEntity.getUserTalker();
            String headPic = userEntity.getHeadPic();
            String alias = userEntity.getAlias();  //微信号
            FriendBean friendBean = new FriendBean();
            friendBean.setNickname(nickname);
            friendBean.setWxid(username);
            addNewFriend(alias, friendBean); //新的好友,通知后台
        }
        Gson gson = new Gson();

        HookMessageBean msg = new HookMessageBean(getToken(), status, username, content, msgType, conversationTime,msgId);
        String msgStr = gson.toJson(msg);
        if (AppConfig.getSelectHost() == null) {
            AppConfig.setHost(AppConfig.OUT_NETWORK);
        }
        Log.e("111","实时发送信息---ip"+ AppConfig.getSelectHost());
        //实时发送信息
        OkGo.post(AppConfig.OUT_NETWORK + NetApi.sendMessage).headers("Content-Type", "application/json").upJson(msgStr).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                LogUtils.i(TAG, "result:" + s);
                try {
                    HttpBean bean = new Gson().fromJson(s, HttpBean.class);
                    if (bean.isSuccess()) {
                        LogUtils.i(TAG, "保存信息成功:" + conversationTime);
                    } else {
                        LogUtils.w(TAG, "保存第" + index + "条信息失败:" + conversationTime);


                        //保存失败时候发起广播
                        Intent in=new Intent();
                        in.setClassName(Constance.packageName_me, Constance.receiver_my);
                        in.setAction(Constance.action_hookmessagefail);
                        in.putExtra("status",status+"");
                        in.putExtra("username",username);
                        in.putExtra("content",content);
                        in.putExtra("msgType",msgType);
                        in.putExtra("conversationTime",conversationTime+"");
                        in.putExtra("msgId",msgId);
                        context.sendBroadcast(in);

                    }
                } catch (Exception e) {
                    //保存失败时候发起广播
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me, Constance.receiver_my);
                    in.setAction(Constance.action_hookmessagefail);
                    in.putExtra("status",status+"");
                    in.putExtra("username",username);
                    in.putExtra("content",content);
                    in.putExtra("msgType",msgType);
                    in.putExtra("conversationTime",conversationTime+"");
                    in.putExtra("msgId",msgId);
                    context.sendBroadcast(in);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                LogUtils.e(TAG, "保存第" + index + "条信息失败:" + e.getMessage());
                //  list_msgFail.add(new HookMessageBean(status, username, content, msgType, conversationTime));


                //保存失败时候发起广播
                Intent in=new Intent();
                in.setClassName(Constance.packageName_me, Constance.receiver_my);
                in.setAction(Constance.action_hookmessagefail);
                in.putExtra("status",status+"");
                in.putExtra("username",username);
                in.putExtra("content",content);
                in.putExtra("msgType",msgType);
                in.putExtra("conversationTime",conversationTime+"");
                in.putExtra("msgId",msgId);
                context.sendBroadcast(in);

//                ToastUtil.showLongToast("保存第"+index+"条信息失败:" + e.getMessage());
                //setAlarmToSyncMessage();
            }
        });
    }


    /**
     * 有新的好友,通知后台
     *
     * @param wxToken
     * @param friend
     */
    private static void addNewFriend(String wxToken, FriendBean friend) {
        Gson gson = new Gson();
        String friendStr = gson.toJson(friend);
        LogUtils.v(TAG, "有新好友:" + friendStr);
       /* if (AppConfig.getSelectHost() == null) {
            AppConfig.setHost(AppConfig.OUT_NETWORK);
        }*/
        OkGo.post(AppConfig.OUT_NETWORK + NetApi.addFriend + "/" + wxToken).headers("Content-Type", "application/json").upJson(friendStr).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                LogUtils.i(TAG, "result:" + s);
                try {
                    HttpBean bean = new Gson().fromJson(s, HttpBean.class);
                    if (bean.isSuccess()) {
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
            }
        });
    }

}
