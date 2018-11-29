package com.mikuwxc.autoreply.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.Tools;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.LogUtils;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.common.util.SharedPrefsUtils;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.modle.HttpBean;
import com.mikuwxc.autoreply.presenter.tasks.AsyncFriendTask;
import com.mikuwxc.autoreply.utils.ReconnectWXutil;
import com.mikuwxc.autoreply.wcapi.WechatEntityFactory;
import com.mikuwxc.autoreply.wcentity.AddFriendEntity;
import com.mikuwxc.autoreply.wcentity.AddFriendEntitys;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcentity.WxEntity;
import com.mikuwxc.autoreply.wchook.ChatroomHook;
import com.mikuwxc.autoreply.wcutil.BitmapPathUtil;
import com.mikuwxc.autoreply.wcutil.FriendUtil;
import com.mikuwxc.autoreply.wcutil.LabelUtil;
import com.mikuwxc.autoreply.wcutil.MomentUtil;
import com.mikuwxc.autoreply.wcutil.PatternUtil;
import com.mikuwxc.autoreply.wcutil.RemarkUtil;
import com.mikuwxc.autoreply.wcutil.SendMesUtil;
import com.mikuwxc.autoreply.wx.WechatDb;
import com.mikuwxc.autoreply.xposed.CommonHook;

import org.apache.commons.lang3.StringUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.Call;
import okhttp3.Response;

public class MountReceiver extends XC_MethodHook {

    private Activity activity;
    private String JcmDbPath="/storage/emulated/0/JCM/";

    private String substring;

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Context context=(Context) param.args[0];
        Intent intent=(Intent) param.args[1];
        String action=intent.getAction();
        if(TextUtils.isEmpty(action))
        {
            return;
        }

        if(action.equals(Constance.action_getWechatFriends))
        {
            action_getWechatFriends(context,intent);
        }else if (action.equals(Constance.action_getWechatDb)){
            action_getWechatDb(context,intent);
        }else if (action.equals(Constance.action_toast)){
            action_toast(context,intent);
        }
    }


    public void action_toast(Context context,Intent intent){
        String str_toast = intent.getStringExtra(Constance.str_toast);
        Toast.makeText(context,str_toast,Toast.LENGTH_SHORT).show();
    }



    public void action_getWechatDb(Context context, Intent intent){
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                activity = (Activity) param.thisObject;
                int versionCode= activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionCode;
                XposedBridge.log("weixin123 versionCode="+versionCode);
            }
        });

        Toast.makeText(context,"正在连接微信", Toast.LENGTH_LONG).show();
        ClassLoader classLoader=context.getClassLoader();

        String dabase_Route = intent.getStringExtra(Constance.dabase_Route);
        String dabase_Password = intent.getStringExtra(Constance.dabase_Password);

        //判断路径是否存在，不存在则创建
        File JcmDb=new File(JcmDbPath);
        if (!JcmDb.exists()){
            JcmDb.mkdir();
        }

        String copyRoute=JcmDbPath+"EnMicroMsg"+dabase_Password+".db";

        File file=new File(copyRoute);
        boolean exists = file.exists();
        if (exists){
            file.delete();
        }

        boolean b=copyFile(dabase_Route,copyRoute);

        Intent in=new Intent();
        in.setClassName(Constance.packageName_me,Constance.receiver_my);
        in.setAction(Constance.action_getcpWechatDb);
        in.putExtra(Constance.dabase_cpRoute,copyRoute);
        in.putExtra(Constance.dabase_cpPassword,dabase_Password);

        UserEntity userEntity = WechatDb.getInstance().selectSelf();
        String userName = userEntity.getUserName();
        String userTalker = userEntity.getUserTalker();
        String headPic = userEntity.getHeadPic();
        String alias = userEntity.getAlias();  //微信号
        XposedBridge.log(alias+userName+userTalker+headPic);     //获取数据库里面的历史数据
        in.putExtra("wxno",alias);
        in.putExtra("wxid",userTalker);
        in.putExtra("headImgUrl",headPic);
        in.putExtra("userName",userName);

        context.sendBroadcast(in);


    }




    public void action_getWechatFriends(final Context context, Intent intent)
    {


       // Toast.makeText(context,"微信收到广播:"+Constance.action_getWechatFriends,Toast.LENGTH_LONG).show();
        final ClassLoader classLoader=context.getClassLoader();
        final WechatEntity create = WechatEntityFactory.create(CommonHook.wechatVersionName);
        String name = intent.getStringExtra("name");
        String content = intent.getStringExtra("content");
        final String type = intent.getStringExtra("type");
        final String circleText = intent.getStringExtra("circleText");
        final String fodderUrl = intent.getStringExtra("fodderUrl");
        String circleType = intent.getStringExtra("circleType");
        String addWxid = intent.getStringExtra("addWxid");
        String addMsg = intent.getStringExtra("addMsg");
        String addType = intent.getStringExtra("addType");
        String deleFriend = intent.getStringExtra("deleFriend");
        String addRemark = intent.getStringExtra("addRemark");


        XposedBridge.log("name:::"+name);
        XposedBridge.log("typetypetypetype:::"+type);
        if (name!=null) {
            try {
                 final String path = Environment.getExternalStorageDirectory().toString() + "/shidoe/";

                if (type.equals("1")) {

                    SendMesUtil.sendTxt(classLoader, create, name, content, 1, 1);

                }else if (type.equals("3")){
                    downLoad(content, "",classLoader,create,name,path,type,context);

                }else if (type.equals("34")){   //发送语音
                    downLoad(content, "",classLoader,create,name,path,type,context);

                }else if (type.equals("43")){  //发送视频
                    downLoad(content, "",classLoader,create,name,path,type,context);
                }else if(type.equals("49")){   //文章 文件
                    downLoad(content, "",classLoader,create,name,path,type,context);
                }else if (type.equals("200")){
                    //发朋友圈
                    if ("0".equals(circleType)){
                        XposedBridge.log("path:"+path);
                        XposedBridge.log("fodderUrl:"+fodderUrl);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<String> listMonentPic = downLoadPicMonet(fodderUrl, circleText, classLoader, create, "", path, type, context);
                                XposedBridge.log("listMonentPic+"+listMonentPic.toString());
                                JSONArray array= JSONArray.parseArray(JSON.toJSONString(listMonentPic));
                                MomentUtil.sendPicMoment9(classLoader,create,circleText,0,null,array);
                            }
                        }).start();

                    }else if ("2".equals(circleType)){
                        MomentUtil.sendTxtMoment(classLoader,create,circleText,0,null);
                    }else if ("1".equals(circleType)){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<String> listMonentVideo = downLoadPicMonet(fodderUrl, circleText, classLoader, create, "", path, type, context);
                                XposedBridge.log("发视频朋友圈：："+listMonentVideo.toString());


                                MediaMetadataRetriever media = new MediaMetadataRetriever();
                                media.setDataSource(listMonentVideo.get(0));// videoPath 本地视频的路径
                                Bitmap bitmap  = media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC );
                                String picPath = BitmapPathUtil.saveBitmap(context, bitmap);
                                XposedBridge.log("picPathpicPath::::::++"+picPath);

                                MomentUtil.sendVideoMoment(classLoader,create,circleText,0,null,picPath,listMonentVideo.get(0));
                            }
                        }).start();

                    }else if("3".equals(circleType)){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Set<String> labelNameSet = new HashSet();
                                List<String> linkMonent = downLoadLickMonet(fodderUrl, circleText, classLoader, create, "", path, type, context);
                                XposedBridge.log("发链接朋友圈：："+linkMonent.get(0)+"::"+linkMonent.get(1)+"::"+linkMonent.get(2));
                                MomentUtil.sendLinkMoment(classLoader,create,circleText,0,labelNameSet,"",linkMonent.get(0),linkMonent.get(1),linkMonent.get(2));
                            }
                        }).start();

                    }

                }else if ("118".equals(type)){
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_verify_friend);
                    in.putExtra("verifyType",content);
                    context.sendBroadcast(in);

                } else if (type.equals("119")){  //是否自动抢红包
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_returnRoom);
                    in.putExtra("momyType",content);
                    context.sendBroadcast(in);

                }else if (type.equals("115")){
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_settings);
                    in.putExtra("settingType",content);
                    context.sendBroadcast(in);
                }


                else if ("116".equals(type)){  //是否能看微信号
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_canseewxno);
                    in.putExtra("canSeewxType",content);
                    context.sendBroadcast(in);
                }else if ("117".equals(type)){   //是否能打开扫一扫
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_saoyisao);
                    in.putExtra("saoyisaoType",content);
                    context.sendBroadcast(in);
                } else if (type.equals("201")){   //201代表加好友
                    String addFriendList = MyFileUtil.readFromFile(AppConfig.APP_FILE + "/addFriendList");
                    List<AddFriendEntity> wechatIdList = new Gson().fromJson(addFriendList, new TypeToken<List<AddFriendEntity>>() {
                    }.getType());
                    if (wechatIdList.size()>0){
                        boolean phoneNumber = PatternUtil.isPhoneNumber(wechatIdList.get(0).getAddNo());
                        XposedBridge.log("phoneNumberphoneNumber::"+phoneNumber);
                        if (phoneNumber){
                            FriendUtil.searchFriend(classLoader,create,0,wechatIdList.get(0).getMsg(),wechatIdList.get(0).getAddNo(),wechatIdList.get(0).getRemark(),15,wechatIdList,wechatIdList.get(0).getId());
                        }else{
                            FriendUtil.searchFriend(classLoader,create,0,wechatIdList.get(0).getMsg(),wechatIdList.get(0).getAddNo(),wechatIdList.get(0).getRemark(),3,wechatIdList,wechatIdList.get(0).getId());
                        }

                    }else{
                        Toast.makeText(context,"需要添加的好友列表为空",Toast.LENGTH_LONG).show();
                    }


                }else if ("210".equals(type)){  //清除僵持粉检测
                    XposedBridge.log("开始清理僵尸粉");
                    ArrayList<FriendBean> friends = WechatDb.getInstance().selectContactTree();
                    String friendsIdListJson = new Gson().toJson(friends);
                    MyFileUtil.writeToNewFile(AppConfig.APP_FILE+"/clearList",friendsIdListJson);
                    ChatroomHook.createChatroom(classLoader,context,create);

                }else if ("211".equals(type)){  //因为要创建群才能检测僵尸粉
                    ChatroomHook.createChatroom(classLoader,context,create);

                }else if("101".equals(type)){
                    if (deleFriend!=null){
                        ArrayList arrayList = new Gson().fromJson(deleFriend, ArrayList.class);
                        for (int i = 0; i < arrayList.size(); i++) {
                            XposedBridge.log("deleFriend:"+ arrayList.get(i).toString());
                            FriendUtil.deleteFriend(classLoader, create, arrayList.get(i).toString());     //删除指定好友
                        }
                        UserEntity userEntity = WechatDb.getInstance().selectSelf();
                        String userName = userEntity.getUserTalker();
                        String alias = userEntity.getAlias();  //微信号
                        if(StringUtils.isBlank(alias)){
                            alias=userName;
                        }
                        List<FriendBean> friendBeans = WechatDb.getInstance().selectAfterDeleteFriend();
                        for (int i = 0; i < friendBeans.size(); i++) {
                            FriendBean friendBean = friendBeans.get(i);
                            XposedBridge.log(friendBean.getWxid()+":"+friendBean.getWxno());
                        }

                        Intent in=new Intent();
                        in.setClassName(Constance.packageName_me,Constance.receiver_my);
                        in.setAction(Constance.action_reconnenct_wx);
                        context.sendBroadcast(in);

                    }else {
                        Toast.makeText(context,"删除好友列表为空",Toast.LENGTH_SHORT).show();
                    }


                }else if ("206".equals(type)){  //设置好友备注
                    RemarkUtil.updateContactRemark(classLoader, create, name, content);    // 修改好友备注
                }else if ("207".equals(type)){  //设置好友电话号码
                    RemarkUtil.updateContactPhone(classLoader, create, name, content);
                }else if ("111".equals(type)){ //是否显示电话号码
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_canseephone);
                    in.putExtra("canSeePhoneType",content);
                    context.sendBroadcast(in);
                } else if ("112".equals(type)){  //是否能领红包
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_receiveluckmoney);
                    in.putExtra("receivemomyType",content);
                    context.sendBroadcast(in);
                }else if ("113".equals(type)){  //是否能删除好友
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_candeletefriend);
                    in.putExtra("deletefriendType",content);
                    context.sendBroadcast(in);
                }else if ("114".equals(type)){  //是否能删除好友聊天记录
                    Intent in=new Intent();
                    in.setClassName(Constance.packageName_me,Constance.receiver_my);
                    in.setAction(Constance.action_candeletefriendchat);
                    in.putExtra("deletefriendchatType",content);
                    context.sendBroadcast(in);
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Intent in=new Intent();
            in.setClassName(Constance.packageName_me,Constance.receiver_my);
            in.setAction(Constance.action_getWechatFriends);


            UserEntity userEntity = WechatDb.getInstance().selectSelf();
            String userName = userEntity.getUserName();
            String userTalker = userEntity.getUserTalker();
            String headPic = userEntity.getHeadPic();
            String alias = userEntity.getAlias();  //微信号
            XposedBridge.log(alias+userName+userTalker+headPic);     //获取数据库里面的历史数据
            in.putExtra("wxno",alias);
            in.putExtra("wxid",userTalker);
            in.putExtra("headImgUrl",headPic);
            context.sendBroadcast(in);
        }

    }

    /**
     * 从服务器下载文件
     * @param path 下载文件的地址
     * @param
     */
    public void downLoad(final String path, final String cirletext, final ClassLoader classLoader, final WechatEntity create, final String name, final String picpach, final String type, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

//                    String[] split = path.split(",");

//                    String suffixes="avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|txt|html|htm|java|doc|amr";
                    String file=path.substring(path.lastIndexOf('/')+1);//截取url最后的数据
                    substring=file;
                    URL url = new URL(path);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == 200) {
                        InputStream is = con.getInputStream();//获取输入流
                        FileOutputStream fileOutputStream = null;//文件输出流
                        if (is != null) {
                            FileUtils fileUtils = new FileUtils();
                            fileOutputStream = new FileOutputStream(fileUtils.createFile(substring));//指定文件保存路径，代码看下一步
                            byte[] buf = new byte[1024];
                            int ch;
                            while ((ch = is.read(buf)) != -1) {
                                fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();

                            if (type.equals("3")){
                                File file1=new File(picpach+substring);
                                boolean a = file1.exists();
                                if (a){
                                    boolean b = SendMesUtil.sendPic(classLoader, create, "", name, picpach+substring, 1);
                                }else {
                                    XposedBridge.log("992746034"+"图片还没下载好");
                                }

                            }/*else if (type.equals("34")){
                                if (activity!=null){
                                    SendMesUtil.sendAmr9(classLoader,create, activity,name,picpach+substring,1,1);
                                }else {
                                    Toast.makeText(context,"发送语音文件失败，请重连微信后再发送",Toast.LENGTH_SHORT).show();
                                    XposedBridge.log("activity为空");
                                }

                            }*/else if (type.equals("49")||type.equals("43")||type.equals("34")){
                                SendMesUtil.sendFile(classLoader, create, name, picpach+substring, 1);
                            }else if (type.equals("200")){
                                List<String> list = new ArrayList<String>();
                                list.add(picpach+substring);
                                JSONArray array= JSONArray.parseArray(JSON.toJSONString(list));
                                XposedBridge.log("array:"+array);
                                XposedBridge.log("cirletext:"+cirletext);
                                MomentUtil.sendPicMoment9(classLoader,create,cirletext,0,null,array);
                            }


                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }


    /**
     * 从服务器下载文件
     * @param path 下载文件的地址
     * @param
     */
    public  List<String> downLoadPicMonet(final String path, final String cirletext, final ClassLoader classLoader, final WechatEntity create, final String name, final String picpach, final String type, final Context context) {
        final List<String> listPicMonet = new ArrayList<String>();
                try {

                    String[] split = path.split(",");

                    for (int i = 0; i < split.length; i++) {
                        XposedBridge.log("split::"+split[i]);

                        int index = split[i].lastIndexOf("/");
                        String substrings = split[i].substring(index + 1);


                        URL url = new URL(split[i]);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setReadTimeout(5000);
                        con.setConnectTimeout(5000);
                        con.setRequestProperty("Charset", "UTF-8");
                        con.setRequestMethod("GET");
                        if (con.getResponseCode() == 200) {
                            InputStream is = con.getInputStream();//获取输入流
                            FileOutputStream fileOutputStream = null;//文件输出流
                            if (is != null) {
                                FileUtils fileUtils = new FileUtils();
                                fileOutputStream = new FileOutputStream(fileUtils.createFile(substrings));//指定文件保存路径，代码看下一步
                                byte[] buf = new byte[1024];
                                int ch;
                                while ((ch = is.read(buf)) != -1) {
                                    fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                                }
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.flush();
                                fileOutputStream.close();

                                listPicMonet.add(picpach + substrings);

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    XposedBridge.log("下载图片错误：："+e.toString());
                }

        return listPicMonet;

    }


    /**
     * 从服务器下载文件
     * @param path 下载文件的地址
     * @param
     */
    public  List<String> downLoadLickMonet(final String path, final String cirletext, final ClassLoader classLoader, final WechatEntity create, final String name, final String picpach, final String type, final Context context) {
        final List<String> listPicMonet = new ArrayList<String>();
        try {


            String[] split = path.split(",");

            for (int i = 0; i < 1; i++) {
                int index = split[i].lastIndexOf("/");
                String substrings = split[i].substring(index + 1);
                URL url = new URL(split[i]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);
                con.setRequestProperty("Charset", "UTF-8");
                con.setRequestMethod("GET");
                if (con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();//获取输入流
                    FileOutputStream fileOutputStream = null;//文件输出流
                    if (is != null) {
                        FileUtils fileUtils = new FileUtils();
                        fileOutputStream = new FileOutputStream(fileUtils.createFile(substrings));//指定文件保存路径，代码看下一步
                        byte[] buf = new byte[1024];
                        int ch;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        listPicMonet.add(picpach + substrings);
                    }
                }
            }
                listPicMonet.add(split[1]);
                int splitLength=split.length;
                String urlContent="";
            for (int i = 2; i <splitLength ; i++) {
                if(i==2){
                    urlContent=split[i];
                }else{
                    urlContent=urlContent+","+split[i];
                }
            }
            listPicMonet.add(urlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listPicMonet;

    }




    public class FileUtils {
        private String path = Environment.getExternalStorageDirectory().toString() + "/shidoe";

        public FileUtils() {
            File file = new File(path);
            /**
             *如果文件夹不存在就创建
             */
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        /**
         * 创建一个文件
         *
         * @param FileName 文件名
         * @return
         */
        public File createFile(String FileName) {
            return new File(path, FileName);
        }
    }




    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     *         <code>false</code> otherwise
     */
    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                XposedBridge.log("--Method--"+"copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                XposedBridge.log("--Method--\", \"copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                XposedBridge.log("--Method--\", \"copyFile:  oldFile cannot read.");
                return false;
            }


            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public  void sendFriendList(String wxToken, List<FriendBean> friendList, final boolean onSvc) {
        Gson gson = new Gson();
        String listStr = gson.toJson(friendList);
        XposedBridge.log("111"+ "sendFriendList---ip" + AppConfig.OUT_NETWORK);
        try {
            OkGo.post(AppConfig.OUT_NETWORK + NetApi.syncFriend + "/" + wxToken).headers("Content-Type", "application/json").upJson(listStr).execute(new StringCallback() {
                @Override
                public void onSuccess(String s, Call call, Response response) {
                    try {
                        HttpBean bean = new Gson().fromJson(s, HttpBean.class);
                        if (bean.isSuccess()) {
                            //showNotice(onSvc, "同步成功");
                            XposedBridge.log("同步成功");
                        } else {
                        }
                    } catch (Exception e) {
                        XposedBridge.log("错误：：" + e.toString());
                    }
                }

                @Override
                public void onError(Call call, Response response, Exception e) {
                    super.onError(call, response, e);
                }
            });
        } catch (Exception e) {
            XposedBridge.log("错误：：" + e.toString());
        }

    }

}
