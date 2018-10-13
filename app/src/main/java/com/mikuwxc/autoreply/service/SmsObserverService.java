package com.mikuwxc.autoreply.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.mikuwxc.autoreply.bean.SmsObserverBean;


/**
 *
 * create by : 喻敏航
 * create time : 8018-10-13
 * description : 短信监听服务
 *
 * **/
public class SmsObserverService extends Service {


    private static final int SUCCESS_OK = 200;

    /**
    * 短信 url监听路径

    全部短信：content://sms/
    收件箱：content://sms/inbox
    发件箱：content://sms/sent
    草稿箱：content://sms/draft
    */

    private Uri SMS_INBOX = Uri.parse("content://sms/");
    private SmsObserver smsObserver;
    private SmsObserverBean sendBean;
    private SmsObserverBean receiverBean;
    private Handler smsHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
           smsHandler = new Handler() {
            //这里可以进行回调的操作
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 200:
                        SmsObserverBean smsObserverBean = (SmsObserverBean) msg.obj;
                        uploadSmsMessage(smsObserverBean);
                        break;
                    default:
                        break;
                }
            }
        };
        init();

    }

    private void init() {
        sendBean = new SmsObserverBean("","","",0L);
        receiverBean = new SmsObserverBean("","","",0L);
        smsObserver = new SmsObserver(this, smsHandler);
        getContentResolver().registerContentObserver(SMS_INBOX, true, smsObserver);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class SmsObserver extends ContentObserver {

        public SmsObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //每当有新短信到来时，使用我们获取短消息的方法
            getSmsFromPhone();

        }
    }


    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"body", "address", "type", "date"};
        String where = " date >  " + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
        if (null == cur) return;
        if (cur.moveToFirst()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String body = cur.getString(cur.getColumnIndex("body"));
            String type = cur.getString(cur.getColumnIndex("type"));
            long date = cur.getLong(cur.getColumnIndex("date"));
            //TODO 这里是具体处理逻辑
            if ("2".equals(type)) {
                //发出的短信
                if (!sendBean.getContent().equals(body) || !sendBean.getPhoneNum().equals(number) || !sendBean.getType().equals(type) || !sendBean.getTime().equals(date)) {
                    sendBean.setContent(body);
                    sendBean.setPhoneNum(number);
                    sendBean.setType(type);
                    sendBean.setTime(date);
                    smsHandler.obtainMessage(SUCCESS_OK,sendBean);
                    Message message=new Message();
                    message.what=SUCCESS_OK;
                    message.obj=sendBean;
                    smsHandler.sendMessage(message);
                    //Toast.makeText(this, "发出短信:::::::"+number + body + type + "..." + date, Toast.LENGTH_SHORT).show();
                }

            } else if("1".equals(type)){
                //收到的短信
                if (!receiverBean.getContent().equals(body) || !receiverBean.getPhoneNum().equals(number) || !receiverBean.getType().equals(type) || !receiverBean.getTime().equals(date)) {
                    receiverBean.setContent(body);
                    receiverBean.setPhoneNum(number);
                    receiverBean.setType(type);
                    receiverBean.setTime(date);
                    Message message=new Message();
                    message.what=SUCCESS_OK;
                    message.obj=receiverBean;
                    smsHandler.sendMessage(message);
                   //Toast.makeText(this, "收到短信:::::::"+number + body + type + "..." + date, Toast.LENGTH_SHORT).show();
                }

            }

        }
    }




    private void uploadSmsMessage(SmsObserverBean obj) {

        Toast.makeText(this, obj.toString(), Toast.LENGTH_SHORT).show();
    }
}
