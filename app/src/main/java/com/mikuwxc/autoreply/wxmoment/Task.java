package com.mikuwxc.autoreply.wxmoment;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mikuwxc.autoreply.utils.ShellUtils;
import com.mikuwxc.autoreply.wcutil.FileIoUtil;
import com.mikuwxc.autoreply.wcutil.GlobalUtil;
import com.mikuwxc.autoreply.wxmoment.model.SnsInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * Created by chiontang on 2/17/16.
 */
public class Task {

    protected Context context = null;
    public SnsReader snsReader = null;

    public Task(Context context) {
        this.context = context;
        this.makeExtDir();
    }

    public void restartWeChat() throws Throwable {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
        int pid = -1;
        for (int i = 0; i < pids.size(); i++) {
            ActivityManager.RunningAppProcessInfo info = pids.get(i);
            if (info.processName.equalsIgnoreCase(Config.WECHAT_PACKAGE)) {
                pid = info.pid;
            }
        }
        if (pid != -1) {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("kill " + pid + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();
        }
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(Config.WECHAT_PACKAGE);
        context.startActivity(launchIntent);

    }

    public void copySnsDB() throws Throwable {

        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        String destDir = Config.EXT_DIR;//sd卡目标路径WeChatMomentStat
        Process su = Runtime.getRuntime().exec("su");//执行超级管理员root
        String uniqueName = FileIoUtil.getValueFromPath(GlobalUtil.WX_UNIQUENAME_SAVE_PATH);//微信随机生成的  6416+54165+4111文件夹名称
//        ParseMoment.copySnsMicroMsgDB();
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes("mount -o remount,rw " + dataDir + "\n");//挂载
        outputStream.writeBytes("chmod 777 " + "data/data/com.tencent.mm" + "/MicroMsg\n");//增加微信朋友圈数据库文件夹的权限
        outputStream.writeBytes("chmod 777 " + "data/data/com.tencent.mm/MicroMsg/" +uniqueName+"/SnsMicroMsg.db \n");//增加微信朋友圈数据库文件夹的权限
        outputStream.writeBytes("cd " + dataDir + "/data/" + Config.WECHAT_PACKAGE + "/MicroMsg\n");//进入微信数据库文件夹
        outputStream.writeBytes("ls | while read line; do cp "+uniqueName+"/SnsMicroMsg.db " + destDir + "/ ; done \n");//查找并从微信数据库复制一份到sd卡下面
        outputStream.writeBytes("sleep 1\n");//睡眠
        outputStream.writeBytes("chmod 777 " + destDir + "/SnsMicroMsg.db\n");//修改WeChatMomentStat的执行权限
        outputStream.writeBytes("exit\n");//退出命令
        outputStream.flush();
        outputStream.close();
        Thread.sleep(1000);
    }

    public void testRoot() {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Toast.makeText(context, "设备未开启root权限,读取朋友圈数据失败", Toast.LENGTH_LONG).show();
        }
    }

    public String getWeChatVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(Config.WECHAT_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("wechatmomentstat", e.getMessage());
            return null;
        }
        String wechatVersion = "";
        if (pInfo != null) {
            wechatVersion = pInfo.versionName;
            Config.initWeChatVersion(wechatVersion);
            return wechatVersion;
        }
        return null;
    }

    public void makeExtDir() {
        File extDir = new File(Config.EXT_DIR);
        if (!extDir.exists()) {
            extDir.mkdir();
        }
    }

    public void copyAPKFromAssets() {
        InputStream assetInputStream = null;
        File outputAPKFile = new File(Config.EXT_DIR + "/wechat.apk");
        if (outputAPKFile.exists())
            outputAPKFile.delete();
        byte[] buf = new byte[1024];
        try {
            outputAPKFile.createNewFile();
            assetInputStream = context.getAssets().open("wechat.apk");
            FileOutputStream outAPKStream = new FileOutputStream(outputAPKFile);
            int read;
            while((read = assetInputStream.read(buf)) != -1) {
                outAPKStream.write(buf, 0, read);
            }
            assetInputStream.close();
            outAPKStream.close();
        } catch (Exception e) {
            Log.e("wechatmomentstat", "exception", e);
        }
    }

    public void initSnsReader() {
        File outputAPKFile = new File(Config.EXT_DIR + "/wechat.apk");
        if (!outputAPKFile.exists())
            copyAPKFromAssets();

        try {

            Config.initWeChatVersion("6.3.13.64_r4488992");
            DexClassLoader cl = new DexClassLoader(
                    outputAPKFile.getAbsolutePath(),
                    context.getDir("outdex", 0).getAbsolutePath(),
                    null,
                    ClassLoader.getSystemClassLoader());

            Class SnsDetailParser = null;
            Class SnsDetail = null;
            Class SnsObject = null;
            SnsDetailParser = cl.loadClass(Config.SNS_XML_GENERATOR_CLASS);
            SnsDetail = cl.loadClass(Config.PROTOCAL_SNS_DETAIL_CLASS);
            SnsObject = cl.loadClass(Config.PROTOCAL_SNS_OBJECT_CLASS);
            snsReader = new SnsReader(SnsDetail, SnsDetailParser, SnsObject);
        } catch (Throwable e) {
            Log.e("wechatmomentstat", "exception", e);
        }
    }

    /**
     * 量将数据写入到json文件
     * **/
    public static void saveToJSONFile(ArrayList<SnsInfo> snsList, String fileName, boolean onlySelected) {
        //测试===============数据库改造
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(new File(Config.EXT_DIR + "/moment.db"), null);
        database.execSQL("create table if not exists moment(id integer primary key autoincrement," +
                "authorId text, authorName text,comments text,content text,isCurrentUser text," +
                "likes text,mediaList text,snsId text,sourceType text,timestamp text," +
                "uploadsuccess text )");
        database.execSQL("create table if not exists media(id integer primary key autoincrement,address text,yunaddress text,uploadsuccess text,tomcatuploadsuccess text,snsId text)");//一对多(多的这张表)
        //判断是否插入
        for (int i = 0; i < snsList.size(); i++) {
            SnsInfo snsInfo = snsList.get(i);
            if (snsInfo.isCurrentUser) {
                Cursor cursor = database.rawQuery("select * from moment where snsId = ? and timestamp=?", new String[]{snsInfo.id, String.valueOf(snsInfo.timestamp)});
                int count = cursor.getCount();
                if(count==0){
                    ContentValues cv=new ContentValues();
                    cv.put("authorId",snsInfo.authorId);
                    cv.put("authorName",snsInfo.authorName);
                    cv.put("comments",JSON.toJSONString(snsInfo.comments));
                    cv.put("content",snsInfo.content);
                    cv.put("isCurrentUser",String.valueOf(snsInfo.isCurrentUser));
                    cv.put("likes",JSON.toJSONString(snsInfo.likes));
                    cv.put("mediaList",JSON.toJSONString(snsInfo.mediaList));
                    cv.put("snsId",snsInfo.id);
                    cv.put("sourceType",snsInfo.sourceType);
                    cv.put("timestamp",String.valueOf(snsInfo.timestamp));
                    cv.put("uploadsuccess","false");
                    database.insert("moment",null,cv);

                    ArrayList<String> mediaList = snsInfo.mediaList;
                    for (String address : mediaList) {
                        ContentValues c=new ContentValues();
                        c.put("address",address);
                        c.put("uploadsuccess","false");
                        c.put("tomcatuploadsuccess","false");
                        c.put("snsId",snsInfo.id);//逻辑外键
                        database.insert("media",null,c);
                    }
                }
                cursor.close();
            }
        }

        database.close();

        //上传基本

        //处理下载图片任务
        MomentPicUpload.handleDatas2();

        //测试===============数据库改造
       /* List<SnsInfo> snsInfos=null;
        String json = getFileFromSD(Config.EXT_DIR+"/all_sns.json");//所有的数据
        if("".equals(json)){
            snsInfos = JSON.parseArray("[]", SnsInfo.class);//解析所有的数据
        }else{

            snsInfos = JSON.parseArray(json, SnsInfo.class);//解析所有的数据
        }


        JSONArray snsListJSON = new JSONArray();

        for (int snsIndex=0; snsIndex<snsList.size(); snsIndex++) {
            SnsInfo currentSns = snsList.get(snsIndex);
            //排除==============================================================================================================================
            String id = currentSns.id;
            boolean flag=false;
            for (SnsInfo snsInfo : snsInfos) {
                if(id.equals(snsInfo.getSnsId())){
                    flag=true;
                }
            }
            if(flag){
                continue;
            }
            if(!currentSns.isCurrentUser){//不是当前用户
                continue;
            }
            //排除==============================================================================================================================
          *//*  if (!currentSns.ready) {
                continue;
            }
            if (onlySelected && !currentSns.selected) {
                continue;
            }*//*
            JSONObject snsJSON = new JSONObject();
            JSONArray commentsJSON = new JSONArray();
            JSONArray likesJSON = new JSONArray();
            JSONArray mediaListJSON = new JSONArray();
            try {
                snsJSON.put("isCurrentUser", currentSns.isCurrentUser);
                snsJSON.put("snsId", currentSns.id);
                snsJSON.put("authorName", currentSns.authorName);
                snsJSON.put("authorId", currentSns.authorId);
                snsJSON.put("content", currentSns.content);
                for (int i = 0; i < currentSns.comments.size(); i++) {
                    JSONObject commentJSON = new JSONObject();
                    commentJSON.put("isCurrentUser", currentSns.comments.get(i).isCurrentUser);
                    commentJSON.put("authorName", currentSns.comments.get(i).authorName);
                    commentJSON.put("authorId", currentSns.comments.get(i).authorId);
                    commentJSON.put("content", currentSns.comments.get(i).content);
                    commentJSON.put("toUserName", currentSns.comments.get(i).toUser);
                    commentJSON.put("toUserId", currentSns.comments.get(i).toUserId);
                    commentsJSON.put(commentJSON);
                }
                snsJSON.put("comments", commentsJSON);
                for (int i = 0; i < currentSns.likes.size(); i++) {
                    JSONObject likeJSON = new JSONObject();
                    likeJSON.put("isCurrentUser", currentSns.likes.get(i).isCurrentUser);
                    likeJSON.put("userName", currentSns.likes.get(i).userName);
                    likeJSON.put("userId", currentSns.likes.get(i).userId);
                    likesJSON.put(likeJSON);
                }
                snsJSON.put("likes", likesJSON);
                for (int i = 0; i < currentSns.mediaList.size(); i++) {
                    mediaListJSON.put(currentSns.mediaList.get(i));
                }
                snsJSON.put("mediaList", mediaListJSON);
                snsJSON.put("rawXML", currentSns.rawXML);
                snsJSON.put("timestamp", currentSns.timestamp);

                snsListJSON.put(snsJSON);

            } catch (Exception exception) {
                Log.e("wechatmomentstat", "exception", exception);
            }
        }

        File jsonFile = new File(fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                Log.e("wechatmomentstat", "exception", e);
            }
        }

        try {
            FileWriter fw = new FileWriter(jsonFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            String str = snsListJSON.toString();
            List<SnsInfo> saveList = JSON.parseArray(str, SnsInfo.class);//需要添加到的
            if(saveList!=null&&saveList.size()>0){
                snsInfos.addAll(saveList);
//            bw.write(str);//写入json
                bw.write(JSON.toJSONString(snsInfos));//写入json
            }

            bw.close();
            //准备上传
            MomentPicUpload.handleDatas();
        } catch (IOException e) {
            Log.e("wechatmomentstat", "exception", e);
        }*/
    }

    /**从sd卡获取json**/
    private static String getFileFromSD(String path) {
        String result = "";

        try {
            FileInputStream f = new FileInputStream(path);
            BufferedReader bis = new BufferedReader(new InputStreamReader(f));
            String line = "";
            while ((line = bis.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }


}
