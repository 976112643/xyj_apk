package com.mikuwxc.autoreply.wcreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.bean.ImLoginBean;
import com.mikuwxc.autoreply.bean.SystemBean;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.VersionInfo;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.Constants;
import com.mikuwxc.autoreply.common.util.EventBusUtil;
import com.mikuwxc.autoreply.common.util.LogUtils;
import com.mikuwxc.autoreply.common.util.Logger;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.modle.C;
import com.mikuwxc.autoreply.modle.Event;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.modle.HookMessageBean;
import com.mikuwxc.autoreply.modle.HttpBean;
import com.mikuwxc.autoreply.modle.ImMessageBean;
import com.mikuwxc.autoreply.presenter.tasks.AsyncFriendTask;
import com.mikuwxc.autoreply.receiver.Constance;
import com.mikuwxc.autoreply.service.LoopService;
import com.mikuwxc.autoreply.utils.GetImeiUtil;
import com.mikuwxc.autoreply.utils.IntentUtil;
import com.mikuwxc.autoreply.utils.ParseUtil;
import com.mikuwxc.autoreply.utils.ReconnectWXutil;
import com.mikuwxc.autoreply.utils.SystemUtil;
import com.mikuwxc.autoreply.wcentity.AddFriendEntity;
import com.mikuwxc.autoreply.wcentity.AddFriendEntitys;
import com.mikuwxc.autoreply.wcentity.CircleFriendEntity;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMLogListener;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUser;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMValueCallBack;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.apache.commons.lang3.StringUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.XposedBridge;
import okhttp3.Call;
import okhttp3.Response;

import static com.mikuwxc.autoreply.activity.RunningActivity.tv3;
import static com.mikuwxc.autoreply.activity.RunningActivity.wxState;


public class MsgReceiver extends BroadcastReceiver {
    private String sig;
    private String id;
    private String sdkAppId;
    private final String SDcardPath = "/storage/emulated/0/JCM/";
    ArrayList<FriendBean> beanArrayList=new ArrayList<>();
    public static List<HookMessageBean> list_msgFail = new ArrayList<>();
    FriendBean friendBean;
    private String token;

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            sendMsg();
            handler.postDelayed(this, 20000);
        }
    };

    Handler handlerAlive=new Handler();
    Runnable runnableAlive=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            permissionAlive(wxno,context1);
            handlerAlive.postDelayed(this, 30000);
        }
    };

    private String[] search = {
            "am force-stop com.tencent.mm",
            "am start -a com.tencent.mm.action.BIZSHORTCUT -f 67108864"
    };
    private String wxno;
    private Context context1;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();
        context1 = context;
     if(TextUtils.isEmpty(action))
     {
        return;
     }

     if(action.equals(Constance.action_getWechatFriends))
     {
     }else if (action.equals(Constance.action_returnRoom)){
         String momyType = intent.getStringExtra("momyType");
         Toast.makeText(context, "是否开启自动抢红包"+momyType, Toast.LENGTH_LONG).show();
         action_returnRooms(context,momyType);
     }else if (action.equals(Constance.action_getcpWechatDb)){
         Log.e("111","收到广播");
         action_getWechatDB(context,intent);
     }else  if (action.equals(Constance.action_verify_friend)){
         String verifyType = intent.getStringExtra("verifyType");
         Toast.makeText(context, "是否开启自动通过好友"+verifyType, Toast.LENGTH_LONG).show();
         action_verify_friend(context,verifyType);
     }else if (action.equals(Constance.action_hookmessagefail)){
         String status = intent.getStringExtra("status");
         String username = intent.getStringExtra("username");
         String content = intent.getStringExtra("content");
         String msgType = intent.getStringExtra("msgType");
         String conversationTime = intent.getStringExtra("conversationTime");
         String msgId = intent.getStringExtra("msgId");
         list_msgFail.add(new HookMessageBean(Integer.parseInt(status), username, content, msgType, Long.parseLong(conversationTime),msgId));
     }else if (action.equals(Constance.action_canseewxno)){
         String canSeewxType = intent.getStringExtra("canSeewxType");
         Toast.makeText(context, "是否可以看微信号"+canSeewxType, Toast.LENGTH_LONG).show();
         action_canseewxno(context,canSeewxType);
     }else if(action.equals(Constance.action_canseephone)){
         String canSeePhoneType = intent.getStringExtra("canSeePhoneType");
         Toast.makeText(context, "是否可以看手机号"+canSeePhoneType, Toast.LENGTH_LONG).show();
         action_canseephone(context,canSeePhoneType);
     } else if (action.equals(Constance.action_saoyisao)){
         String saoyisaoType = intent.getStringExtra("saoyisaoType");
         Toast.makeText(context, "是否打开扫一扫"+saoyisaoType, Toast.LENGTH_LONG).show();
         action_saoyisao(context,saoyisaoType);
     }else if (action.equals(Constance.action_settings)){
         String settingType = intent.getStringExtra("settingType");
         Toast.makeText(context, "是否可以设置"+settingType, Toast.LENGTH_LONG).show();
         action_settings(context,settingType);
     }else if (action.equals(Constance.action_receiveluckmoney)){
         String receivemomyType = intent.getStringExtra("receivemomyType");
         Toast.makeText(context, "是否可以领红包转账"+receivemomyType, Toast.LENGTH_LONG).show();
         action_receiveluckmoney(context,receivemomyType);
     }else if (action.equals(Constance.action_candeletefriend)){
         String deletefriendType = intent.getStringExtra("deletefriendType");
         Toast.makeText(context, "是否可以删除好友"+deletefriendType, Toast.LENGTH_LONG).show();
         action_candeletefriend(context,deletefriendType);
     }else if (action.equals(Constance.action_candeletefriendchat)){
         String deletefriendchatType = intent.getStringExtra("deletefriendchatType");
         Toast.makeText(context, "是否可以删除好友聊天记录"+deletefriendchatType, Toast.LENGTH_LONG).show();
         action_candeletefriendchat(context,deletefriendchatType);
     }else if(action.equals(Constance.action_reconnenct_wx)){
         ReconnectWXutil.open();
     }

    }

    private void action_candeletefriendchat(Context context,String deletefriendchatType) {
        if ("true".equals(deletefriendchatType)) {
            MyFileUtil.writeProperties(Constants.ONFRIENDCHATDELETESTAUS_PUT,"true");
        } else {
            MyFileUtil.writeProperties(Constants.ONFRIENDCHATDELETESTAUS_PUT,"false");
        }
    }
    private void action_candeletefriend(Context context,String deletefriendType) {
        if ("true".equals(deletefriendType)) {
            MyFileUtil.writeProperties(Constants.ONDELETEFRIENDSTAUS_PUT,"true");
        } else {
            MyFileUtil.writeProperties(Constants.ONDELETEFRIENDSTAUS_PUT,"false");
        }
    }
    private void action_receiveluckmoney(Context context,String receivemomyType) {
        if ("true".equals(receivemomyType)) {
            MyFileUtil.writeProperties(Constants.RECEIVELUCKYMONEYSTAUS_PUT,"true");
        } else {
            MyFileUtil.writeProperties(Constants.RECEIVELUCKYMONEYSTAUS_PUT,"false");
        }
    }
    private void action_settings(Context context,String saoyisaoType) {
        if ("true".equals(saoyisaoType)) {
          //  ToastUtil.showLongToast("开启设置权限");
            MyFileUtil.writeProperties(Constants.SETTING_PUT,"true");

        } else {
        //    ToastUtil.showLongToast("关闭设置权限");
            MyFileUtil.writeProperties(Constants.SETTING_PUT,"false");
        }
    }
    private void action_saoyisao(Context context,String saoyisaoType) {
        if ("true".equals(saoyisaoType)) {
            //重连微信并且更改红包是否能自动获取
            MyFileUtil.writeProperties(Constants.SAOYISAOSTAUS_PUT,"true");

        } else {
            MyFileUtil.writeProperties(Constants.SAOYISAOSTAUS_PUT,"false");
        }
    }
    private void action_canseephone(Context context,String canSeePhoneType) {
        if ("true".equals(canSeePhoneType)) {
            //重连微信并且更改红包是否能自动获取
            MyFileUtil.writeProperties(Constants.CANSEEPHONESTAUS_PUT,"true");

        } else {
            MyFileUtil.writeProperties(Constants.CANSEEPHONESTAUS_PUT,"false");
        }
    }
    private void action_canseewxno(Context context,String canSeewxType) {
        if ("true".equals(canSeewxType)) {
            //重连微信并且更改红包是否能自动获取
            MyFileUtil.writeProperties(Constants.CANSEEWXSTAUS_PUT,"true");

        } else {
            MyFileUtil.writeProperties(Constants.CANSEEWXSTAUS_PUT,"false");
        }
    }
    private void action_verify_friend(Context context,String verifyType) {
        if ("true".equals(verifyType)) {
            //重连微信并且更改红包是否能自动获取
            MyFileUtil.writeProperties(Constants.VERIFYSTAUS_PUT,"true");

        } else {
            MyFileUtil.writeProperties(Constants.VERIFYSTAUS_PUT,"false");
        }

    }


    private void action_returnRooms(Context context,String momyType) {
        if ("true".equals(momyType)){
            //重连微信并且更改红包是否能自动获取
            MyFileUtil.writeProperties(Constants.MONEYSTAUS_PUT,"true");

        }else{
          //  ToastUtil.showLongToast("关闭微信自动抢红包权限");
            MyFileUtil.writeProperties(Constants.MONEYSTAUS_PUT,"false");
        }


    }


    //获取数据库的路径并复制去指定文件夹，和密码
    private void action_getWechatDB(Context context, Intent intent) {
        String friendBeans = MyFileUtil.readFromFile(AppConfig.APP_FILE + "/friendBeans");
        String chatroomEntitiesJson = MyFileUtil.readFromFile(AppConfig.APP_FILE + "/chatroomEntitiesJson");
        wxno = intent.getStringExtra("wxno");
        String wxid = intent.getStringExtra("wxid");
        if (StringUtils.isBlank(wxno)){
            wxno=wxid;
        }
        MyFileUtil.writeToNewFile(AppConfig.APP_FILE+"/wxno",wxno);
        String headImgUrl = intent.getStringExtra("headImgUrl");
        String userName = intent.getStringExtra("userName");
        Toast.makeText(context,"连接中",Toast.LENGTH_LONG).show();

        //登录IM同步好友
        if (StringUtils.isNotBlank(friendBeans)){
            Log.e("111","friendBeansJson：："+friendBeans);
            sendWXFriendList(friendBeans,context, wxno,wxid,headImgUrl,userName);
        }

        //上传群消息
        if (StringUtils.isNotBlank(chatroomEntitiesJson)){
            sendWXChatroomList(chatroomEntitiesJson,wxno);
        }
    }

    private void sendWXChatroomList(String chatroomEntitiesJson,String wxno) {

        OkGo.post(AppConfig.OUT_NETWORK+ NetApi.synchronChatroom+"/"+wxno).upJson(chatroomEntitiesJson).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                Log.e("TAG","同步群成功："+s);
            }


            @Override
            public void onError(Call call, Response response, Exception e) {
                Log.e("TAG","同步群失败："+e.toString());
            }
        });
    }


    private void sendWXFriendList(String WXFriendList,Context context,String wxno,String wxid,String headImgUrl,String userName) {
         List<FriendBean> friendBean = new Gson().fromJson(WXFriendList, new TypeToken<List<FriendBean>>() {
        }.getType());
        String DEVICE_ID = null;
        try {
            DEVICE_ID = GetImeiUtil.getOnlyIdentification(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("111",DEVICE_ID);
        //获取当前app版本号与后台对比后台版本号大于本地app版本号时进行更新

        //获取微信版本号传给后台
        String wxVersion = MyFileUtil.readFromFile(AppConfig.APP_FOLDER + "/version");
        String TAG = "系统参数：";
        SystemBean systemBean=new SystemBean();
        systemBean.setManufacturer(SystemUtil.getDeviceBrand());
        systemBean.setModel(SystemUtil.getSystemModel());
        systemBean.setAndroidVersion(SystemUtil.getSystemVersion());
        systemBean.setAppVersion(SystemUtil.getAppVersionName(context));
        systemBean.setPhone(SystemUtil.getPhone(context));
        systemBean.setPatchCode(VersionInfo.versionCode);
        //登录IM
        Log.e(TAG, "systemBean：" + systemBean.toString());
        volleyGet(context, wxno, wxid, headImgUrl,friendBean,userName,DEVICE_ID,wxVersion,systemBean.getManufacturer(),systemBean.getModel(),
                systemBean.getAndroidVersion(),systemBean.getAppVersion(),systemBean.getPhone(),systemBean.getPatchCode());

    }


    private void volleyGet(final Context context, final String wxno, final String wxid, String headImgUrl, final List<FriendBean> friendBean,String userName,String DEVICE_ID,String versionName,
                           String manufacturer,String model,String androidVersion,String appVersion,String phone,int patchCode) {
        String url = AppConfig.OUT_NETWORK + NetApi.imLogin+"?";

        try{
            url+= "wxno="+wxno
                    +"&"+"headImgUrl="+headImgUrl+"&"+"wxid="+wxid+"&"+"nickname="+URLEncoder.encode(userName,"UTF-8")+"&"+"jpush="+DEVICE_ID+"&"+"versionName="+versionName+"&"+"manufacturer="+URLEncoder.encode(manufacturer,"UTF-8")
                    +"&"+"appVersion="+appVersion+"&"+"androidVersion="+androidVersion+"&"+"model="+URLEncoder.encode(model,"UTF-8")+"&"+"phone="+phone+"&"+"patchCode="+patchCode;
        }catch (Exception e){

        }


        Log.e("111",url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {//s为请求返回的字符串数据
                        ImLoginBean imLoginBean = new Gson().fromJson(s, ImLoginBean.class);
                        if(imLoginBean!=null&&imLoginBean.getCode().equals("200")&&imLoginBean.isSuccess()==true) {
                            Log.e("111",new Gson().toJson(imLoginBean));

                            sig = imLoginBean.getResult().getSig();
                            id = imLoginBean.getResult().getRelationId();
                            sdkAppId = imLoginBean.getResult().getSdkAppId();
                            boolean luckyPackage = imLoginBean.getResult().isLuckyPackage();
                            boolean passNewFriend = imLoginBean.getResult().isPassNewFriend();
                            boolean showWxno = imLoginBean.getResult().isShowWxno();
                            boolean scan = imLoginBean.getResult().isScan();
                            boolean setting=imLoginBean.getResult().isSetting();
                            boolean receipt= imLoginBean.getResult().isReceipt();
                            boolean deleteFriend = imLoginBean.getResult().isDeleteFriend();
                            boolean deleteChatRecord = imLoginBean.getResult().isDeleteChatRecord();
                            boolean showPhone = imLoginBean.getResult().isShowPhone();

                            String wordsIntercept = imLoginBean.getResult().getWordsIntercept();
                            String wordsNotice = imLoginBean.getResult().getWordsNotice();


                            if (wordsIntercept!=null){
                                MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/updateIntercept", "updateIntercept sensitive word");//告知微信hoook有敏感词需要更新
                                MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/sensitiveIntercept", wordsIntercept);
                            }
                            if (wordsNotice!=null){
                                MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/updateNotice", "updateNotice sensitive word");//告知微信hoook有敏感词需要更新
                                MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/sensitiveNotice", wordsNotice);
                            }
                            initDataLogin(imLoginBean);
                            initTMConfig(context);
                            loginIM(context,wxno);
                            //登陆IM成功再同步好友
                            AsyncFriendTask.sendFriendList(wxno, friendBean, false);

                            MyFileUtil.writeProperties(Constants.TEST_PUT,"true");

                            //服务保活加上权限会死循环，需要更改服务的状态
                            MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/updateAlive", "true");

                           // ToastUtil.showLongToast("开启所有权限");



                            if (luckyPackage){
                                //重连微信并且更改红包是否能自动获取
                              //  ToastUtil.showLongToast("开启微信自动抢红包权限");
                                MyFileUtil.writeProperties(Constants.MONEYSTAUS_PUT,"true");
                            }else{
                                //重连微信并且更改红包是否能自动获取
                              //  ToastUtil.showLongToast("开启微信自动抢红包权限");
                                MyFileUtil.writeProperties(Constants.MONEYSTAUS_PUT,"false");
                            }


                            if (passNewFriend){
                             //   ToastUtil.showLongToast("开启微信自动通过好友权限");
                                MyFileUtil.writeProperties(Constants.VERIFYSTAUS_PUT,"true");
                            }else{
                           //     ToastUtil.showLongToast("开启微信自动通过好友权限");
                                MyFileUtil.writeProperties(Constants.VERIFYSTAUS_PUT,"false");
                            }



                            if (showWxno){
                           //     ToastUtil.showLongToast("开启好友微信号权限");
                                MyFileUtil.writeProperties(Constants.CANSEEWXSTAUS_PUT,"true");
                            }else{
                           //     ToastUtil.showLongToast("关闭好友微信号权限");
                                MyFileUtil.writeProperties(Constants.CANSEEWXSTAUS_PUT,"false");
                            }


                            if (scan){
                            //    ToastUtil.showLongToast("开启扫一扫权限");
                                MyFileUtil.writeProperties(Constants.SAOYISAOSTAUS_PUT,"true");
                            }else{
                          //      ToastUtil.showLongToast("关闭扫一扫权限");
                                MyFileUtil.writeProperties(Constants.SAOYISAOSTAUS_PUT,"false");
                            }


                            if (setting){
                           //     ToastUtil.showLongToast("开启设置权限");
                                MyFileUtil.writeProperties(Constants.SETTING_PUT,"true");
                            }else{
                           //     ToastUtil.showLongToast("关闭设置权限");
                                MyFileUtil.writeProperties(Constants.SETTING_PUT,"false");
                            }

                            if (receipt){
                                MyFileUtil.writeProperties(Constants.RECEIVELUCKYMONEYSTAUS_PUT,"true");
                            }else{
                                MyFileUtil.writeProperties(Constants.RECEIVELUCKYMONEYSTAUS_PUT,"false");
                            }

                            if (deleteFriend){
                                MyFileUtil.writeProperties(Constants.ONDELETEFRIENDSTAUS_PUT,"true");
                            }else{
                                MyFileUtil.writeProperties(Constants.ONDELETEFRIENDSTAUS_PUT,"false");
                            }

                            if (deleteChatRecord){
                                MyFileUtil.writeProperties(Constants.ONFRIENDCHATDELETESTAUS_PUT,"true");
                            }else{
                                MyFileUtil.writeProperties(Constants.ONFRIENDCHATDELETESTAUS_PUT,"false");
                            }





                        }else {
                            //ToastUtil.showShortToast("登录IM此帐号不能授权");
                            MyFileUtil.writeProperties(Constants.TEST_PUT,"false");
                            //服务保活加上权限会死循环，需要更改服务的状态
                            MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/updateAlive", "false");

                          //  ToastUtil.showLongToast("关闭所有权限");
                           //移除定时保活功能
                            handlerAlive.removeCallbacks(runnableAlive);
                           // handlerAlive.removeMessages(0);
                            search[1] = chineseToUnicode(search[1]);
                            execShell(search);

                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(context,volleyError.toString(),Toast.LENGTH_LONG).show();
                        if (wxState!=null) {
                            wxState.setText("微信连接状态：false");
                            tv3.setText("服务器连接状态：false");
                            MyFileUtil.writeProperties("connection_put","false");
                        }
                    }
                });
        //设置请求的Tag标签，可以在全局请求队列中通过Tag标签进行请求的查找
        request.setTag("testGet");
        //将请求加入全局队列中
        MyApp.getHttpQueues().add(request);
    }



    private void initDataLogin(ImLoginBean imLoginBean) {
        Constants.token = imLoginBean.getResult().getRelationId();
        LogUtils.e("111", "登录成功");
        if (!TextUtils.isEmpty(Constants.token)) {
            //头尾添加1位随机数作加密
            Random rand = new Random();
            int start = rand.nextInt(10);
            int end = rand.nextInt(10);
            try {
                File tokenFile = new File(AppConfig.APP_FOLDER, "/token");
                tokenFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(tokenFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
                osw.write(start + Constants.token + end);
                osw.flush();
                fos.flush();
                osw.close();
                fos.close();
                LogUtils.e("111", "保存token:" + Constants.token);
            } catch (Exception e) {
                LogUtils.e("111", "保存token出错");
                e.printStackTrace();
            }
            //ToastUtil.showLongToast("登录成功");
            Toast.makeText(context1,"登录成功",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context1,"登录失败",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 初始化腾讯云IM 配置
     */
    private void initTMConfig(Context context) {
        //初始化SDK基本配置
        TIMSdkConfig config = new TIMSdkConfig(Integer.parseInt(sdkAppId))
                .enableCrashReport(false)
                .enableLogPrint(true)
                .setLogLevel(TIMLogLevel.DEBUG);
                //.setLogPath(Environment.getExternalStorageDirectory().getParent() + "/justfortest/");
        //初始化SDK
        TIMManager.getInstance().init(context, config);
        //2.初始化SDK配置
        TIMSdkConfig sdkConfig = TIMManager.getInstance().getSdkConfig();
        sdkConfig.setLogListener(new TIMLogListener() {
            @Override
            public void log(int i, String s, String s1) {

            }
        });
//2.初始化SDK配置
    }



    /**
     * 登录腾讯云 IM
     */
    private void loginIM(final Context context, final String wxno) {
        TIMUser user = new TIMUser();
        user.setIdentifier(AppConfig.getIdentifier());
        //发起登录请求
        TIMManager instance = TIMManager.getInstance();

        instance.login(
                id,//sdkAppId，由腾讯分配
                sig,//用户帐号签名，由私钥加密获得，具体请参考文档
                new TIMCallBack() {//回调接口

                    @Override
                    public void onSuccess() {//登录成功
                        Toast.makeText(context,"连接服务器成功",Toast.LENGTH_SHORT).show();
                        if (wxState!=null) {
                            wxState.setText("微信连接状态：true");
                            tv3.setText("服务器连接状态：true");
                            MyFileUtil.writeProperties("connection_put","true");
                        }
                        NewMessageListener(context);
                        handler.postDelayed(runnable, 20000);//每两秒执行一次runnable.
                        sendMsg();


                        handlerAlive.postDelayed(runnableAlive, 10000);//每两秒执行一次runnable.

                        //保活Service
                        Intent intent=new Intent(context,LoopService.class);
                        intent.putExtra("wxno",wxno);
                        context.startService(intent);


                        Calendar cal = Calendar.getInstance();
                        Intent intent1 = new Intent(context, LoopService.class);
                        PendingIntent pintent = PendingIntent.getService(context, 0, intent1, 0);
                        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        // 每分钟启动一次，这个时间值视具体情况而定
                        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 10*1000, pintent);
                    }

                    @Override
                    public void onError(int code, String desc) {//登录失败
                        if (wxState!=null) {
                        wxState.setText("微信连接状态：false");
                            tv3.setText("服务器连接状态：false");
                            MyFileUtil.writeProperties("connection_put","false");
                        }
                        //错误码code和错误描述desc，可用于定位请求失败原因
                        //错误码code含义请参见错误码表
                        Toast.makeText(context,"连接服务器失败",Toast.LENGTH_SHORT).show();
                        Log.e("111",code+"_____"+desc);
                        //  ToastUtil.showLongToast("关闭所有权限");
                        //移除定时保活功能
                        handlerAlive.removeCallbacks(runnableAlive);
                    }
                });
    }



    private void NewMessageListener(final Context context) {
        //设置消息监听器，收到新消息时，通过此监听器回调
        TIMManager.getInstance().addMessageListener(new TIMMessageListener() {//消息监听器
            @Override
            public boolean onNewMessages(List<TIMMessage> list) {//收到新消息
                TIMMessage msg = list.get(0);
                for (int i = 0; i < msg.getElementCount(); ++i) {
                    TIMElem elem = msg.getElement(i);
                    //获取当前元素的类型
                    TIMElemType elemType = elem.getType();
                    Log.d("111", "elem type: " + elemType.name());
                    if (elemType == TIMElemType.Text) {
                        //处理文本消息
                        TIMUserProfile sendUser = msg.getSenderProfile();
                        String sender = msg.getSender();
                        final TIMTextElem textElem = (TIMTextElem) elem;
                        if (!msg.isSelf()) {
                            if (!TextUtils.isEmpty(textElem.getText().replaceAll("&quot;", "\""))) {
                                Logger.d("收到消息了。。。。。" + textElem.getText().replaceAll("&quot;", "\""));
                                if (textElem.getText().contains("wxno")) {
                                    try {
                                        ImMessageBean messageBean = new Gson().fromJson(textElem.getText().replaceAll("&quot;", "\""), ImMessageBean.class);
                                        //ToastUtil.showLongToast("消息发送人: " + messageBean.getWxid() + "  消息内容: " + messageBean.getContent());
                                        EventBusUtil.sendEvent(new Event(C.EventCode.A, messageBean));
                                        Intent intent=new Intent();
                                        String type = messageBean.getType();
                                        if (type.equals("200")){
                                            CircleFriendEntity circleFriendEntity = new Gson().fromJson(messageBean.getContent(), CircleFriendEntity.class);
                                            String circleType = circleFriendEntity.getType();
                                            Toast.makeText(context,circleType+"343757500",Toast.LENGTH_LONG).show();
                                            if ("0".equals(circleType)){  //图文
                                                String circleText = circleFriendEntity.getContent();
                                                String fodderUrl = circleFriendEntity.getFodderUrl();
                                                intent.putExtra("name",messageBean.getWxid());
                                                intent.putExtra("content",messageBean.getContent());
                                                intent.putExtra("type",type);
                                                intent.putExtra("circleText",circleText);
                                                intent.putExtra("fodderUrl",fodderUrl);
                                                intent.putExtra("circleType",circleType);
                                            }else if("1".equals(circleType)){ //视频
                                                String circleText = circleFriendEntity.getContent();
                                                String fodderUrl = circleFriendEntity.getFodderUrl();
                                                intent.putExtra("name",messageBean.getWxid());
                                                intent.putExtra("content",messageBean.getContent());
                                                intent.putExtra("type",type);
                                                intent.putExtra("circleText",circleText);
                                                intent.putExtra("fodderUrl",fodderUrl);
                                                intent.putExtra("circleType",circleType);
                                            }else if ("2".equals(circleType)){//文本
                                                String circleText = circleFriendEntity.getContent();
                                                String fodderUrl = circleFriendEntity.getFodderUrl();
                                                intent.putExtra("name",messageBean.getWxid());
                                                intent.putExtra("content",messageBean.getContent());
                                                intent.putExtra("type",type);
                                                intent.putExtra("circleText",circleText);
                                                intent.putExtra("fodderUrl",fodderUrl);
                                                intent.putExtra("circleType",circleType);
                                            }else if("3".equals(circleType)){ //链接
                                                String circleText = circleFriendEntity.getContent();
                                                String fodderUrl = circleFriendEntity.getFodderUrl();
                                                intent.putExtra("name",messageBean.getWxid());
                                                intent.putExtra("content",messageBean.getContent());
                                                intent.putExtra("type",type);
                                                intent.putExtra("circleText",circleText);
                                                intent.putExtra("fodderUrl",fodderUrl);
                                                intent.putExtra("circleType",circleType);
                                            }
                                            intent.setAction(Constance.action_getWechatFriends);
                                            intent.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                                            context.sendBroadcast(intent);
                                          //  Toast.makeText(context,"发送广播朋友圈:"+Constance.action_getWechatFriends,Toast.LENGTH_LONG).show();
                                        }else if (type.equals("201")){   //加好友的type
                                            List<AddFriendEntity> wechatIdList = new Gson().fromJson(messageBean.getContent(), new TypeToken<List<AddFriendEntity>>() {
                                            }.getType());
                                            String wechatIdListJson = JSON.toJSONString(wechatIdList);
                                            MyFileUtil.writeToNewFile(AppConfig.APP_FILE+"/addFriendList",wechatIdListJson);
                                            intent.putExtra("name",messageBean.getWxid());
                                            intent.putExtra("type",type);
                                            intent.setAction(Constance.action_getWechatFriends);
                                            intent.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                                            context.sendBroadcast(intent);

                                        }else if (type.equals("210")){
                                            intent.putExtra("name",messageBean.getWxid());
                                            intent.putExtra("type",type);
                                            intent.setAction(Constance.action_getWechatFriends);
                                            intent.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                                            context.sendBroadcast(intent);

                                        }else if ("205".equals(type)){  //敏感词写在本地
                                            Toast.makeText(context,"敏感词更新",Toast.LENGTH_SHORT).show();
                                            Map<String, Object> imLoginBean = new Gson().fromJson(messageBean.getContent(), new TypeToken<Map<String, Object>>(){}.getType());
                                                String wordsIntercept = (String) imLoginBean.get("wordsIntercept");
                                                String wordsNotice = (String) imLoginBean.get("wordsNotice");
                                                if (wordsIntercept != null) {
                                                    MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/updateIntercept", "updateIntercept sensitive word");//告知微信hoook有敏感词需要更新
                                                    MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/sensitiveIntercept", wordsIntercept);
                                                }
                                                if (wordsNotice != null) {
                                                    MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/updateNotice", "updateNotice sensitive word");//告知微信hoook有敏感词需要更新
                                                    MyFileUtil.writeToNewFile(AppConfig.APP_FOLDER + "/sensitiveNotice", wordsNotice);
                                                }
                                        }else if ("101".equals(type)){

                                            String deleFriend = messageBean.getContent();
                                            intent.putExtra("deleFriend", deleFriend);
                                            intent.putExtra("type",type);
                                            intent.putExtra("name",messageBean.getWxid());
                                            intent.setAction(Constance.action_getWechatFriends);
                                            intent.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                                            context.sendBroadcast(intent);
                                        } else{
                                            intent.putExtra("name",messageBean.getWxid());
                                            intent.putExtra("content",messageBean.getContent());
                                            intent.putExtra("type",type);
                                            intent.setAction(Constance.action_getWechatFriends);
                                            intent.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                                            context.sendBroadcast(intent);
                                          //  Toast.makeText(context,"发送广播聊天:"+Constance.action_getWechatFriends,Toast.LENGTH_LONG).show();
                                        }
                                    }catch (Exception e){
                                      //  ToastUtil.showLongToast("收到的数据格式有错"+e.toString());
                                        Toast.makeText(context,"收到的数据格式有错"+e.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                  //  ToastUtil.showLongToast("收到的数据格式有错");
                                    Toast.makeText(context,"收到的数据格式有错",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else if (elemType == TIMElemType.Image) {
                        //处理图片消息
                    }//...处理更多消息
                }

                //消息的内容解析请参考消息收发文档中的消息解析说明
                return true;//返回true将终止回调链，不再调用下一个新消息监听器
            }
        });
    }


    private void sendMsg() {
        //获取单聊会话
//        String peer = "0911d2b559d04fb5b011dc64a6a25235";  //获取与用户 "sample_user_1" 的会话
        String peer = "xinyingjia";  //获取与用户 "sample_user_1" 的会话   //621c62f470e94160a4f9417fe82966b2
        TIMConversation conversation = TIMManager.getInstance().getConversation(
                TIMConversationType.C2C,    //会话类型：单聊
                peer);                      //会话对方用户帐号//对方id
        //构造一条消息
        TIMMessage msg = new TIMMessage();
        //添加文本内容
        TIMTextElem elem = new TIMTextElem();
        elem.setText("大雄 发的第2条消息");
        //将elem添加到消息
        if (msg.addElement(elem) != 0) {
            Log.d("111", "addElement failed");
            return;
        }
        //发送消息
        conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
            @Override
            public void onError(int code, String desc) {//发送消息失败
                //错误码code和错误描述desc，可用于定位请求失败原因
                //错误码code含义请参见错误码表
                Log.e("111", "send message failed. code: " + code + " errmsg: " + desc);
             //   ToastUtil.showShortToast("发送消息失败"+code+"--"+desc);
            }

            @Override
            public void onSuccess(TIMMessage msg) {//发送消息成功
              //  ToastUtil.showShortToast("发送消息成功");
            }
        });
    }

    //截取文件名
    public String getFileName(String pathandname){

        int start=pathandname.lastIndexOf("/");
        if(start!=-1){
            return pathandname.substring(start+1,pathandname.length());
        }else{
            return null;
        }

    }

    /**
     * 解密数据库
     * @param encryptedName 要解密的数据库名称
     * @param decryptedName 解密后的数据库名称
     * @param key 密码
     */
    private String decrypt(String encryptedName,String decryptedName,String key,Context context) {
        try {
            File databaseFile = context.getDatabasePath(SDcardPath + encryptedName);
            SQLiteDatabaseHook hook = new SQLiteDatabaseHook(){
                public void preKey(SQLiteDatabase database){
                }
                public void postKey(SQLiteDatabase database){
                    database.rawExecSQL("PRAGMA cipher_migrate;");  //最关键的一句！！！  因为微信的版本较低，不加会兼容不了微信数据库
                }
            };
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, key, null,hook);
            Cursor cursor = database.rawQuery("select r.username,r.alias,r.conRemark,r.nickname,r.pyInitial,r.quanPin,r.lvbuff,r.encryptUsername,r.contactLabelIds,i.reserved1,i.reserved2 from rcontact r left join img_flag i on r.username = i.username  where (r.type & 1 != 0 and r.type & 8 = 0 and r.type & 32 = 0 and r.verifyFlag & 8 = 0 and r.username not like '%@%' and r.username != 'filehelper' ) ",(Object[]) null);
            while (cursor.moveToNext()) {
                String username       = cursor.getString(0);
                String alias       = cursor.getString(1);
                String conRemark = cursor.getString(2);
                String nickname   = cursor.getString(3);
                byte[] blob       = cursor.getBlob(6);
                String reserved1  = cursor.getString(9);
                String reserved2  = cursor.getString(10);
                friendBean=new FriendBean();
                friendBean.setNickname(nickname);
                friendBean.setWxid(username);
                friendBean.setRemarkname(conRemark);
                friendBean.setHeadImgUrl(StringUtils.isNotBlank(reserved1) ? reserved1 : reserved2);
                if(blob != null){
                    //性别
                    friendBean.setSex(blob[8]);
                    //手机号
                    friendBean.setPhone(ParseUtil.parsePhone(blob));
                    //地区
                    friendBean.setRegion(ParseUtil.parseRegion(blob));
                    //来源
                    friendBean.setAddFrom(ParseUtil.parseFrom(blob));
                }
                //friendBean.setWxno(alias);
                if (StringUtils.isBlank(alias)){
                    friendBean.setWxno(username);
                }else{
                    friendBean.setWxno(alias);
                }
             beanArrayList.add(friendBean);

            }
            String s = JSON.toJSONString(beanArrayList);
            Log.e("111",s);
            File decrypteddatabaseFile = context.getDatabasePath(SDcardPath + decryptedName);
            //deleteDatabase(SDcardPath + decryptedName);
            //连接到解密后的数据库，并设置密码为空
            database.rawExecSQL(String.format("ATTACH DATABASE '%s' as "+ decryptedName.split("\\.")[0] +" KEY '';", decrypteddatabaseFile.getAbsolutePath()));
            database.rawExecSQL("SELECT sqlcipher_export('"+ decryptedName.split("\\.")[0] +"');");
            database.rawExecSQL("DETACH DATABASE "+ decryptedName.split("\\.")[0] +";");
            SQLiteDatabase decrypteddatabase = SQLiteDatabase.openOrCreateDatabase(decrypteddatabaseFile, "", null);

            cursor.close();
            decrypteddatabase.close();
            database.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String chineseToUnicode(String str){
        String result="";
        for (int i = 0; i < str.length(); i++){
            int chr1 = (char) str.charAt(i);
            if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)
                result+="\\u" + Integer.toHexString(chr1);
            }else{
                result+=str.charAt(i);
            }
        }
        return result;
    }

    /**
     30      * 执行Shell命令
     31      *
     32      * @param commands
     33      *            要执行的命令数组
     34      */
    public void execShell(String[] commands) {
        // 获取Runtime对象
        Runtime runtime = Runtime.getRuntime();
        DataOutputStream os = null;
        try {
            // 获取root权限
            Process process = runtime.exec("su");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //是否保活权限
    private void permissionAlive(String wxno1, final Context context) {
        String aliveStaue = MyFileUtil.readFromFile(AppConfig.APP_FOLDER + "/updateAlive");
        String wxno = MyFileUtil.readFromFile(AppConfig.APP_FILE + "/wxno");
        if ("true".equals(aliveStaue)){
            aliveStaue="0";
        }else{
            aliveStaue="1";
        }
        if (StringUtils.isNotBlank(wxno)) {
            Log.e("111","wxno:"+wxno);
            OkGo.post(AppConfig.OUT_NETWORK + NetApi.loginAlive + "?wxno=" + wxno + "&usingState=" + aliveStaue).execute(new StringCallback() {
                @Override
                public void onSuccess(String s, Call call, okhttp3.Response response) {
                    Log.e("111", "result:" + s);
                    try {
                        Date date = new Date();
                        Log.e("111", "保活成功" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + list_msgFail.size());
                        if (wxState != null) {
                            wxState.setText("微信连接状态：true");
                            tv3.setText("服务器连接状态：true");
                        }
                        if (list_msgFail.size() > 0) {
                            handleFailedMessage(list_msgFail);
                        } else {
                            Log.e("111", "保活成功::没有缓存失败的信息");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("111", "保活信息失败:" + e.toString());
                        if (wxState != null) {
                            wxState.setText("微信连接状态：false");
                            tv3.setText("服务器连接状态：false");
                        }
                    }
                }

                @Override
                public void onError(Call call, okhttp3.Response response, Exception e) {
                    super.onError(call, response, e);
                    Log.e("111", "保活信息失败:");
                    if (wxState != null) {
                        wxState.setText("微信连接状态：false");
                        tv3.setText("服务器连接状态：false");
                    }

                }
            });
        }else {
            Log.e("111","保活失败，微信号为空");
        }
    }



    /**
     * 定时发送实时同步失败的信息
     *
     */
    private void handleFailedMessage(List<HookMessageBean> hookMessageBeans) {

        Gson gson = new Gson();
        List<HookMessageBean> errorMsg = new ArrayList<>();
        for (int i = 0; i < hookMessageBeans.size(); i++) {
            HookMessageBean bean = hookMessageBeans.get(i);
            if (token == null) {
                token = MyFileUtil.readFromFile(AppConfig.APP_FOLDER + "/token");
                token = token.substring(1, token.length() - 1);
//            LogUtils.w(TAG, "token:" + token);
            }
            bean.setToken(token);
            errorMsg.add(bean);
        }
        String msgListStr = gson.toJson(errorMsg);
        Log.e("111","msgListStr:::"+msgListStr);
        if (AppConfig.getSelectHost() == null) {
            AppConfig.setHost(AppConfig.OUT_NETWORK);
        }
        OkGo.post(AppConfig.OUT_NETWORK + NetApi.syncMessage).headers("Content-Type", "application/json").upJson(msgListStr).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                try {
                    HttpBean bean = new Gson().fromJson(s, HttpBean.class);
                    if (bean.isSuccess()) {
                       list_msgFail.clear();
                    } else {
                        Log.e("111","同步缓存数据失败"+bean.isSuccess());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("111","同步缓存数据失败"+e.toString());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                Log.e("111","同步缓存数据失败"+e.toString());
            }
        });
    }

}
