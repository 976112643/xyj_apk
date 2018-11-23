package com.mikuwxc.autoreply.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;


/**
 *
 * create by : 喻敏航
 * create time : 2018-11-23
 * description : 本地数据库
 *
 * **/
public class MyDBHelper extends SQLiteOpenHelper {


    public static final String DBNAME = "mydb.db";
    public static final int VERSION = 1;
    public static final String TABLE_SMS = "tb_sms";


    public MyDBHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }


    private void createTable(SQLiteDatabase db){
        db.execSQL("create table " + TABLE_SMS + " (" +
                "id integer primary key autoincrement," +
                "content text," +
                "type text," +
                "phoneNum text," +
                "time text," +
                "imei text" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
