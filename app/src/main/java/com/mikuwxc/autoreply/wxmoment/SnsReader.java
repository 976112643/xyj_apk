package com.mikuwxc.autoreply.wxmoment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.wxmoment.model.SnsInfo;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by chiontang on 2/12/16.
 */
public class SnsReader {

    Class SnsDetail = null;
    Class SnsDetailParser = null;
    Class SnsObject = null;
    Parser parser = null;
    ArrayList<SnsInfo> snsList = new ArrayList<SnsInfo>();
    String currentUserId = "";

    public SnsReader(Class SnsDetail, Class SnsDetailParser, Class SnsObject){
        this.SnsDetail = SnsDetail;
        this.SnsDetailParser = SnsDetailParser;
        this.SnsObject = SnsObject;
        this.parser = new Parser(SnsDetail, SnsDetailParser, SnsObject);
    }

    public void run() throws Throwable {
        Log.d("wechatmomentstat", "Querying Sns database.");
        queryDatabase();
        Task.saveToJSONFile(this.snsList, Config.EXT_DIR + "/all_sns.json", false);
    }

    public ArrayList<SnsInfo> getSnsList() {
        return this.snsList;
    }

    protected void queryDatabase() throws Throwable {
        String dbPath = Config.EXT_DIR + "/SnsMicroMsg.db";
//        String dbPath = Environment.getExternalStorageDirectory()+"/snsDir" +"/SnsMicroMsg.db";
        if (!new File(dbPath).exists()) {
            Log.e("wechatmomentstat", "DB file not found");
            throw new Exception("DB file not found");
        }
        snsList.clear();
        SQLiteDatabase database = SQLiteDatabase.openDatabase(dbPath, null, 0);
        getCurrentUserIdFromDatabase(database);
// 查询所有的
// Cursor cursor = database.query("SnsInfo", new String[]{"SnsId", "userName", "createTime", "content", "attrBuf","sourceType","subType"} ,"", new String[]{},"","","createTime DESC","");
        //只查询本人的
        String wxid = MyFileUtil.readFromFile(AppConfig.APP_USERNAME).split(",")[1];
        Cursor cursor = database.query("SnsInfo", new String[]{"SnsId", "userName", "createTime", "content", "attrBuf","sourceType","subType"} ,"userName=?", new String[]{wxid},"","","createTime DESC","");
        while (cursor.moveToNext()) {
            addSnsInfoFromCursor(cursor);
        }
        cursor.close();
        database.close();
    }

    protected void getCurrentUserIdFromDatabase(SQLiteDatabase database) throws Throwable {
        Cursor cursor = database.query("snsExtInfo3", new String[]{"userName"}, "ROWID=?", new String[]{"1"}, "", "", "", "1");
        if (cursor.moveToNext()) {
            this.currentUserId = cursor.getString(cursor.getColumnIndex("userName"));
        }
        cursor.close();
        Log.d("wechatmomentstat", "Current userID=" + this.currentUserId);
    }

    protected void addSnsInfoFromCursor(Cursor cursor) throws Throwable {
        byte[] snsDetailBin = cursor.getBlob(cursor.getColumnIndex("content"));
        byte[] snsObjectBin = cursor.getBlob(cursor.getColumnIndex("attrBuf"));
        String sourceType = cursor.getString(cursor.getColumnIndex("sourceType"));//朋友圈类型
        String subType = cursor.getString(cursor.getColumnIndex("subType"));//朋友圈上传成功与否(自己定义的,原字段不知什么含义)
        SnsInfo newSns = parser.parseSnsAllFromBin(snsDetailBin, snsObjectBin);
        newSns.setSourceType(sourceType);
        for (int i=0;i<snsList.size();i++) {
            if (snsList.get(i).id.equals(newSns.id)) {
                return;
            }
        }

        if (newSns.authorId.equals(this.currentUserId)) {
            newSns.isCurrentUser = true;
        }

        for (int i=0;i<newSns.comments.size();i++) {
            if (newSns.comments.get(i).authorId.equals(this.currentUserId)) {
                newSns.comments.get(i).isCurrentUser = true;
            }
        }

        for (int i=0;i<newSns.likes.size();i++) {
            if (newSns.likes.get(i).userId.equals(this.currentUserId)) {
                newSns.likes.get(i).isCurrentUser = true;
            }
        }

        snsList.add(newSns);
        //newSns.print();
    }



}
