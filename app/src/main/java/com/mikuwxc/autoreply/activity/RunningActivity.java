package com.mikuwxc.autoreply.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.AlarmReceiver;
import com.mikuwxc.autoreply.AutoReplyService;
import com.mikuwxc.autoreply.R;
import com.mikuwxc.autoreply.StaticData;
import com.mikuwxc.autoreply.adapter.TaskRecordAdapter;
import com.mikuwxc.autoreply.bean.AutoReplyStateMent;
import com.mikuwxc.autoreply.bean.BindWXRespon;
import com.mikuwxc.autoreply.bean.ContactData;
import com.mikuwxc.autoreply.bean.FriendBean;
import com.mikuwxc.autoreply.bean.Phone;
import com.mikuwxc.autoreply.bean.TaskJob;
import com.mikuwxc.autoreply.bean.TaskRecord;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.UI;
import com.mikuwxc.autoreply.common.VersionInfo;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.Constants;
import com.mikuwxc.autoreply.common.util.LogUtils;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.db.PhoneUser;
import com.mikuwxc.autoreply.db.PhoneUserHelper;
import com.mikuwxc.autoreply.modle.HttpImeiBean;
import com.mikuwxc.autoreply.receiver.Constance;
import com.mikuwxc.autoreply.utils.ContactsAccessPublic;
import com.mikuwxc.autoreply.utils.FriendCircleShare;
import com.mikuwxc.autoreply.utils.GetImeiUtil;
import com.mikuwxc.autoreply.utils.HttpUtils;
import com.mikuwxc.autoreply.utils.LogToFile;
import com.mikuwxc.autoreply.utils.MJSONObjectRequest;
import com.mikuwxc.autoreply.utils.PreferenceUtil;
import com.mikuwxc.autoreply.utils.UpdateAppUtil;
import com.mikuwxc.autoreply.widget.MainProgressBar;
import com.mikuwxc.autoreply.widget.UrlCircleImageView;
import com.mikuwxc.autoreply.wxid.WxIdUtil;



import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import okhttp3.Call;


public class RunningActivity extends Activity implements AutoReplyService.ControlFinish {

    private static final String TAG = RunningActivity.class.getSimpleName();
    private MainProgressBar progressBar;
    private ListView lv;
    private TaskRecordAdapter taskRecordAdapter;
    public static PackageManager packageManager;
    private TextView startRun;
    private UrlCircleImageView uiv;
    private TextView serviceTv;
    private TextView userName, wxVersion;
    private TextView bindInfo;
    private long nowTime;
    private int type;
    private Button startChat;
    public static TextView wxState;
    private Button news;
    private boolean permission_grant = false;
    private final int REQUEST_PHONE_PERMISSIONS = 0;
    private final String SDcardPath = "/storage/emulated/0/";
    private String[] search = {
            //  "input keyevent 3",// 返回到主界面，数值与按键的对应关系可查阅KeyEvent
            // "sleep 1",// 等待1秒
            // 打开微信的启动界面，am命令的用法可自行百度、Google// 等待3秒
            "am force-stop com.tencent.mm",
            "am start -a com.tencent.mm.action.BIZSHORTCUT -f 67108864"
            // "am  start  service  com.mikuwxc.autoreply.AutoReplyService"// 打开微信的搜索
            // 像搜索框中输入123，但是input不支持中文，蛋疼，而且这边没做输入法处理，默认会自动弹出输入法
    };
    private EditText et_ip;
    private Object appVersionName;
    private TextView tvVersion;
    public static TextView tv3;
    public static TextView tv2;
    private Button bt_updateapp;
    private TextView tvLastVersion;
    private TextView tvHotVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     /*   getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        setContentView(R.layout.activity_running);

        //是否弹出更新弹窗
        //showUpdateDialog();

        //设置极光推送的别名
        setTagAndAlias(this);

        //权限申请
        requestPermission(this);
        //获取好友列表的广播
        sendReceiverGetwechat();

        packageManager = getPackageManager();
        Log.d("RunningActivity>>>", "onCreate: ");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        StaticData.isDoJob = false;
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        //得到键盘锁管理器对象
        kl = km.newKeyguardLock("unLock");
        uiv = (UrlCircleImageView) findViewById(R.id.head_iv);
        serviceTv = (TextView) findViewById(R.id.service_tv);
        userName = (TextView) findViewById(R.id.user_num);
        wxVersion = (TextView) findViewById(R.id.wxVersion);
        bindInfo = (TextView) findViewById(R.id.bindInfo);
        startChat = (Button) findViewById(R.id.startChat);
        wxState = (TextView) findViewById(R.id.wxState);
        tv2 = (TextView) findViewById(R.id.tv2);  //Xpose连接状态
        tv3 = (TextView) findViewById(R.id.tv3);  //服务器连接状态
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        et_ip = (EditText) findViewById(R.id.et_ip);
        bt_updateapp = (Button) findViewById(R.id.bt_updateapp);
        tvLastVersion = (TextView) findViewById(R.id.tvLastVersion);
        tvHotVersion = (TextView) findViewById(R.id.tvHotVersion);
        TextView tvImei =(TextView) findViewById(R.id.tvImei);


        try {
            tvImei.setText("设备IMEI："+GetImeiUtil.getOnlyIdentification(this));
        } catch (Exception e) {
            e.printStackTrace();
        }


        tvHotVersion.setText("当前补丁版本："+VersionInfo.versionCode);
        UpdateAppUtil.getAppVersionState(this,tvLastVersion);
        news = (Button) findViewById(R.id.news);
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.READ_PHONE_STATE);
            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionsList.size() == 0) {
                permission_grant = true;
                initVersion();
            } else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_PHONE_PERMISSIONS);
            }
        } else {
            permission_grant = true;
            initVersion();
        }

        bt_updateapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAppUtil.getAppVersionDownLoad(RunningActivity.this);
            }
        });

        findViewById(R.id.news).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNews();
            }
        });

        findViewById(R.id.newss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewss();
            }
        });

        findViewById(R.id.bt_ip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = et_ip.getText().toString();
                String ipnew = "http://" + ip + "/";
                AppConfig.OUT_NETWORK = ipnew;
                AppConfig.setHost(ipnew);
            }
        });

        findViewById(R.id.exit_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginout();
            }
        });
        uiv.loadUrl(PreferenceUtil.getHeadimgurl(this));
        userName.setText(PreferenceUtil.getNickname(this));
        if (!TextUtils.isEmpty(PreferenceUtil.getOpenid(this))) {
            bindInfo.setText("已绑定");
        } else {
            bindInfo.setText("未绑定");
        }
        findViewById(R.id.setbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        findViewById(R.id.bindwechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApp.api.isWXAppInstalled()) {
                } else {
                    Toast.makeText(RunningActivity.this, "没安装有微信哦", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.getwechatInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceIsRunning()) {
//                if (isAccessibilitySettingsOn(RunningActivity.this)) {
                    StaticData.isGetWxInfo = true;
                    Intent intent = new Intent();
                    intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                    startActivity(intent);
                } else {
                    Toast.makeText(RunningActivity.this, "辅助服务还未开启！", Toast.LENGTH_LONG).show();
                }
            }
        });
        startRun = (TextView) findViewById(R.id.start_run);
        findViewById(R.id.start_run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StaticData.isStartTask) {
                    StaticData.isStartTask = false;
                } else {
                    StaticData.isStartTask = true;
                }
                showInfo(true);
                StaticData.currentTaskId = "";
                checkJob("1");
            }
        });
        progressBar = (MainProgressBar) findViewById(R.id.main_progress_bar);
        lv = (ListView) findViewById(R.id.lv);
        taskRecordAdapter = new TaskRecordAdapter(this);
        lv.setAdapter(taskRecordAdapter);
        showInfo(false);
        loadJboRecord();
        if (getScreenOffTime() < 60000)
            setScreenOffTime(60000);
        Log.d("getScreenOffTime", "onCreate: " + getScreenOffTime());
        //获取app版本
        getAppVersionName();
        isModuleActive("插件状态：true");

    }

    private void getNewss() {
        ToastUtil.showLongToast("关闭所有权限");
        MyFileUtil.writeProperties(Constants.TEST_PUT,"false");
        // 获取Runtime对象  获取root权限
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        search[1] = chineseToUnicode(search[1]);
        execShell(search);
    }

    private void sendReceiverGetwechat() {
        Intent intent = new Intent();
        intent.setAction(Constance.action_getWechatFriends);
        intent.setClassName(Constance.packageName_wechat, Constance.receiver_wechat);
        sendBroadcast(intent);
        // Toast.makeText(this, "发送广播:" + Constance.action_getWechatFriends, Toast.LENGTH_LONG).show();
    }

    private void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

            }
        }
    }

    //重连微信按钮
    private void getNews() {
      // ImeiLogin();
       // ToastUtil.showLongToast("开启所有权限");
        MyFileUtil.writeProperties(Constants.TEST_PUT,"true");

        // 获取Runtime对象  获取root权限
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        search[1] = chineseToUnicode(search[1]);
        execShell(search);
    }

    /**
     * 获得休眠时间 毫秒
     */
    private int getScreenOffTime() {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {

        }
        return screenOffTime;
    }

    /**
     * 设置休眠时间 毫秒
     */
    private void setScreenOffTime(int paramInt) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                    paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private String appendParameter(String url, Map<String, String> params) {
        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().getQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wxVersion.setText("微信版本：" + PreferenceUtil.getWxNumVersion(this));
        showInfo(false);
        Log.d("RunningActivity>>>", "onResume: ");
        StaticData.isDoJob = false;
    }

    private void showInfo(boolean isClickStart) {
        if (serviceIsRunning()) {
//        if (isAccessibilitySettingsOn(RunningActivity.this)) {
            if (StaticData.isStartTask) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                startRun.setText(R.string.run_stop);
                progressBar.statusTv.setText("运行中~");
                progressBar.startThisAnimation();
                setWindowBrightness(0);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                startRun.setText(R.string.run_start);
                progressBar.statusTv.setText("未运行~");
                progressBar.stopThisAnimation();
                setWindowBrightness(150);
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            StaticData.isStartTask = false;
            if (isClickStart) {
                if (TextUtils.isEmpty(PreferenceUtil.getOpenid(this)))
                    Toast.makeText(this, "请先绑定微信！", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "辅助服务还未开启！", Toast.LENGTH_LONG).show();
            }
            startRun.setText(R.string.run_start);
            progressBar.statusTv.setText("未运行~");
            progressBar.stopThisAnimation();
        }
    }

    //获取系统正在运行的服务列表来判断辅助服务是否开启
    private boolean serviceIsRunning() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> lists = am.getRunningServices(150);//获取数量可适当调整
        for (ActivityManager.RunningServiceInfo info : lists) {
            if (info.service.getClassName().equals("com.mikuwxc.autoreply.AutoReplyService")) {
                serviceTv.setText(R.string.service_run);
                return true;
            }
        }
        serviceTv.setText(R.string.service_stop);
        return false;
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        // TestService为对应的服务
        final String service = getPackageName() + "/" + AutoReplyService.class.getCanonicalName();
        Log.i(TAG, "service:" + service);
        // com.z.buildingaccessibilityservices/android.accessibilityservice.AccessibilityService
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            // com.z.buildingaccessibilityservices/com.z.buildingaccessibilityservices.TestService
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    private void loadJboRecord() {
        Map<String, String> map = new HashMap<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.put("deviceInfo", MyApp.tm.getDeviceId());
        String params = appendParameter(HttpUtils.JOB_RECORD_URL, map);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        MJSONObjectRequest jsonObjectRequest = new MJSONObjectRequest(HttpUtils.JOB_RECORD_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                TaskRecord taskRecord = JSON.parseObject(response.toString(), TaskRecord.class);
                taskRecordAdapter.setData(taskRecord.getResult().getRecordList());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "onErrorResponse: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mHeaders = new HashMap<String, String>(1);
                Log.d("Cookie", "getHeaders: " + MyApp.manager.getCookieStore().getCookies().toString());
                mHeaders.put("Cookie", MyApp.manager.getCookieStore().getCookies().toString());
                return mHeaders;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };




    BindWXRespon bindWXRespon;
    List<AutoReplyStateMent> statement;
    private boolean isGetTask = false;
    String[] friendCirclePic;
    String friendCircleContent = "";

    private void checkJob(String flag) {
        if (!StaticData.isStartTask || !serviceIsRunning()) {//停止运行后，退出请求任务队列
            return;
        }
//        if (!StaticData.isStartTask || !isAccessibilitySettingsOn(RunningActivity.this)) {//停止运行后，退出请求任务队列
//            return;
//        }
        WxIdUtil.initId(this);
        Log.d("查询任务》》", "checkJob: ");
        Map<String, String> map = new HashMap<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.put("deviceInfo", MyApp.tm.getDeviceId());
        map.put("tflag", flag);
        String params = appendParameter(HttpUtils.JOB_LIST_URL, map);
        MJSONObjectRequest jsonObjectRequest = new MJSONObjectRequest(HttpUtils.JOB_LIST_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("isDoJob>>>", response.toString());
                if (!StaticData.isDoJob) {//没有在执行任务，任务Id清空
                    StaticData.currentTaskId = "";
                }
                try {
                    bindWXRespon = JSON.parseObject(response.toString(), BindWXRespon.class);
                    if (bindWXRespon.getStatus() == 0) {
                        Toast.makeText(RunningActivity.this, "登陆超时，请重新登陆~", Toast.LENGTH_SHORT).show();
                        PreferenceUtil.setLoginStatus(RunningActivity.this, false);
                        startActivity(new Intent(RunningActivity.this, LoginActivity.class));
                        RunningActivity.this.finish();
                        MyApp.manager.getCookieStore().removeAll();
                        StaticData.isStartTask = false;
                        return;
                    }
                    if (bindWXRespon.getResult() != null) {
                        List<Phone> phoneList = bindWXRespon.getResult().getPhoneList();
                        if (phoneList != null && !phoneList.isEmpty()) {//保存手机号码到数据库
                            PhoneUserHelper.savePhoneList(phoneList);
                            StringBuilder ids = new StringBuilder();
                            for (int i = 0; i < phoneList.size(); i++) {
                                ids.append(String.valueOf(phoneList.get(i).getId()) + (i == phoneList.size() - 1 ? "" : ";"));
                            }
                            StaticData.isCanAddFriend = true;
                            cancelGetJob(ids.toString(), null);
                        }
                        nowTime = bindWXRespon.getResult().getNowTime();
                        List<TaskJob> job = bindWXRespon.getResult().getJob();
                        if (job != null && job.size() > 0) {//判断返回的任务列表有无新任务
                            Log.d("isDoJob>>>", "onResponse: 有新任务，刷新任务列表");
                            if (StaticData.jobList.size() != job.size() && StaticData.isDoJob) {//有新任务，判断当前是否有执行任务
                                for (int i = 0; i < job.size(); i++) {
                                    //返回的任务列表里面有正在执行的任务，则删除此任务
                                    if (StaticData.currentTaskId.equals(String.valueOf(job.get(i).getId()))) {
                                        job.remove(i);
                                        Log.d("isDoJob>>>", "onResponse: 去除重复任务Id:" + StaticData.currentTaskId);
                                    }
                                }
                            }
                            for (int i = 0; i < StaticData.jobList.size(); i++) {
                                int type = StaticData.jobList.get(i).getType();
                                if (type == 500) {
                                    TaskJob taskJob = new TaskJob();
                                    taskJob.setType(500);
                                    job.add(taskJob);
                                }
                                if (type == 600) {
                                    TaskJob taskJob = new TaskJob();
                                    taskJob.setType(600);
                                    job.add(taskJob);
                                }
                            }
                            StaticData.jobList = job;//赋值
                            cancelGetJob("", null);//不再获取旧的任务列表
                        }
                        /**
                         * 任务列表不为空，也没有正在执行任务，则执行任务
                         */
                        if (!StaticData.jobList.isEmpty() && !StaticData.autoIng && !StaticData.isDoJob) {
                            type = 0;
                            friendCirclePic = new String[0];
                            friendCircleContent = "";
                            //初始化任务属性
                            boolean b = doTaskJob(0);
                            if (b) {
                                wakeAndUnlock(true);
                                switch (type) {
                                    case 1://获取微信用户列表
                                        Log.d("isDoJob>>>", "onResponse: 获取微信通讯录");
                                        StaticData.isGetFriendInfo = true;
                                        StaticData.isGetFriendInfoStart = true;
                                        StaticData.isDoJob = true;
                                        StaticData.wechatAuto = false;
                                        Intent intent = new Intent();
                                        intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
                                        RunningActivity.this.startActivity(intent);
                                        break;
                                    case 2://发朋友圈
                                        Log.d("isDoJob>>>", "onResponse: 发朋友圈");
                                        StaticData.isSendFriendCircle = true;
                                        StaticData.isDoJob = true;
                                        StaticData.wechatAuto = false;
                                        StaticData.isFriendCircleSuccess = false;
                                        StaticData.IMGID = 0;//分享图片Id 重置为0
                                        FriendCircleShare friendCircleShare = new FriendCircleShare(RunningActivity.this);
                                        friendCircleShare.newStartShare(friendCircleContent, friendCirclePic);
                                        //10秒后下载图片失败，则朋友圈发送失败
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!StaticData.isFriendCircleSuccess) {
                                                    StaticData.isFriendCircleSuccess = true;
                                                    doOneTaskStatus(-1);//执行朋友圈失败
                                                }
                                            }
                                        }, 10000);
                                        break;
                                    case 3://淘宝任务
                                        doOneTaskStatus(-1);//任务异常
                                        break;
                                    case 4://其他
                                        doOneTaskStatus(-1);//任务异常
                                        break;
                                    case 500://通过好友添加请求
                                        StaticData.isCheckFriend = true;
                                        if (!StaticData.intentList.isEmpty())
                                            try {
                                                StaticData.intentList.get(0).send();
                                            } catch (PendingIntent.CanceledException e) {
                                                e.printStackTrace();
                                            }
                                        break;
                                    case 600://开始互聊或者加好友
                                        if (StaticData.isStartTask) {
                                            startHuliao();
                                        }
                                        break;
                                }
                            } else if (StaticData.isCanAddFriend) {
                                jumpToAddFriend();
                            }
                        } else if (StaticData.isCanAddFriend && !StaticData.isDoJob) {
                            jumpToAddFriend();
                        }
                        if (bindWXRespon.getResult().getTalkFlag() == 1) {
                            if (bindWXRespon.getResult().getWxListstr() != null && !bindWXRespon.getResult().getWxListstr().isEmpty()) {
                                LogToFile.d("互聊中", "初始化互聊人数：" + bindWXRespon.getResult().getWxListstr());
                                String[] split = bindWXRespon.getResult().getWxListstr().split(";");
                                StaticData.friendBeanList.clear();
                                for (int i = 0; i < split.length; i++) {
                                    String currWxNum = PreferenceUtil.getCurrentWxNum(RunningActivity.this);
                                    if (!currWxNum.equals(split[i])) { //要过滤掉自己. 后台把自己也发送过来了! by temper
                                        FriendBean friendBean = new FriendBean();
                                        friendBean.setWechatNum(split[i]);
                                        StaticData.friendBeanList.add(friendBean);
                                    }
                                }
                                cancelGetJob("", 1);//不再获取旧的任务列表
                                chat();
                            }
                        } else { //取消互聊
                            Intent intent = new Intent(RunningActivity.this, AlarmReceiver.class);
                            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            for (int i = 0; i < 4; i++) {
                                PendingIntent sender = PendingIntent.getBroadcast(RunningActivity.this, i, intent, 0);
                                manager.cancel(sender);
                            }
                        }
                        // 处理statment,自动回复 by Temper
                        StaticData.autoReplyMap.clear();
                        if (bindWXRespon.getResult().getStatement() != null) {
                            if (bindWXRespon.getResult().getStatement().size() != 0) {
                                StaticData.autoReply = true;
                                for (int i = 0; i < bindWXRespon.getResult().getStatement().size(); i++) {
                                    String key = bindWXRespon.getResult().getStatement().get(i).getKeyword();
                                    String replyContent = bindWXRespon.getResult().getStatement().get(i).getReplyContent();
                                    StaticData.autoReplyMap.put(key, replyContent);
//                                    LogToFile.i(TAG,"需要自动回复:"+key);
                                }
                            } else {
                                StaticData.autoReply = false;
                            }
                        } else {
                            StaticData.autoReply = false;
                        }
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkJob("-1");
                        }
                    }, 10000);
                    wakeAndUnlock(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "onErrorResponse: " + error.getMessage());
                Toast.makeText(RunningActivity.this, "网络繁忙！请稍后再试！", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        if (!StaticData.isDoJob)
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkJob("-1");
                            }
                        }, 10000);
                    }
                }, 10000);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mHeaders = new HashMap<String, String>(1);
                Log.d("Cookie", "getHeaders: " + MyApp.manager.getCookieStore().getCookies().toString());
                mHeaders.put("Cookie", MyApp.manager.getCookieStore().getCookies().toString());
                return mHeaders;
            }
        };
        MyApp.requestQueue.add(jsonObjectRequest);
    }

    private void startHuliao() {
        StaticData.isChatOthers = true;

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-mm-ss");
        String format = simpleDateFormat.format(date);
        String[] split = format.split("-");
        int hh = Integer.valueOf(split[0]);
        int mm = Integer.valueOf(split[1]);
//        int max = ((mm + 2)>=60)?60:(mm+2);
        int min;
        if ((mm + 3) >= 60) {
            hh = hh + 1;
            mm = 1;
//            max = 2;
        } else {
            mm = mm + 2;
        }
        String timeStr = "";
        if (hh < 10) {
            timeStr = "0";
        }
        timeStr = timeStr + hh + ":";
        if (mm < 10) {
            timeStr = timeStr + "0";
        }
        timeStr = timeStr + mm;
        LogToFile.d("互聊中", timeStr + "将会进行聊天,人数：" + StaticData.friendBeanList.size());
        Toast.makeText(RunningActivity.this, hh + ":" + mm + "将会开始进行聊天", Toast.LENGTH_LONG).show();
        setAlarmTime(0, mm, hh);

        String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis()));
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        Log.d("打招呼时间", "onCreate: " + hour);
        if (hour <= 10) {
            StaticData.chatContent = "早上好啊！现在时间是：" + time;
        } else if (hour <= 2) {
            StaticData.chatContent = "中午好啊！现在时间是：" + time;
        } else if (hour <= 18) {
            StaticData.chatContent = "下午好啊！现在时间是：" + time;
        } else if (hour <= 24) {
            StaticData.chatContent = "晚上好啊！现在时间是：" + time;
        }
        Intent mIntent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
        startActivity(mIntent);
    }

    private void waitStopTask() {
        if (!StaticData.isDoJob) {
            checkJob("-1");
        } else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitStopTask();
        }
    }

    private boolean doTaskJob(int i) {
        if (StaticData.jobList.get(i).getStartTime() != null) {//任务是定时任务
            String format = new SimpleDateFormat("yyyy-MM-dd HH:ss").format(new Date(StaticData.jobList.get(i).getStartTime()));
            long time = nowTime - StaticData.jobList.get(i).getStartTime();
            Log.d("isDoJob>>>", "onResponse: 定时任务》》》" + time + "<<开始时间>>" + format);
            if (time >= 0 && time <= 300000) {
                Log.d("isDoJob>>>", "onResponse: 执行定时任务》》》" + StaticData.jobList.get(i).getTaskName());
                type = StaticData.jobList.get(i).getType();
                if (TextUtils.isEmpty(StaticData.jobList.get(i).getTaskPic())) {
                    friendCirclePic = null;
                } else
                    friendCirclePic = StaticData.jobList.get(i).getTaskPic().split(";");
                friendCircleContent = StaticData.jobList.get(i).getTaskValue();
                StaticData.friendCircleCommentContent = StaticData.jobList.get(i).getComment();
                Log.e("111", StaticData.jobList.get(i).getComment() + ".............................0000000000000000000000000");
                StaticData.currentTaskId = String.valueOf(StaticData.jobList.get(i).getId());
                StaticData.friendCircleAddress = StaticData.jobList.get(i).getAddress();
                StaticData.jobList.remove(i);
                return true;
            } else if (time > 300000) {
                StaticData.currentTaskId = String.valueOf(StaticData.jobList.get(i).getId());
                StaticData.jobList.remove(i);
                doOneTaskStatus(-1);//任务失败
                return false;
            } else {
                if (i + 1 < StaticData.jobList.size())
                    doTaskJob(i + 1);
            }
        } else {//非定时任务 立即执行
            Log.d("isDoJob>>>", "onResponse: 执行即时任务》》》" + StaticData.jobList.get(i).getTaskName());
            type = StaticData.jobList.get(i).getType();
            if (TextUtils.isEmpty(StaticData.jobList.get(i).getTaskPic())) {
                friendCirclePic = null;
            } else
                friendCirclePic = StaticData.jobList.get(i).getTaskPic().split(";");
            friendCircleContent = StaticData.jobList.get(i).getTaskValue();
            StaticData.friendCircleCommentContent = StaticData.jobList.get(i).getComment();
            Log.e("111", friendCirclePic + ".............................3333333333333333333333333333");
            Log.e("111", friendCircleContent + ".............................111111111111111111111111111");


            //获取sharedPreferences对象
            SharedPreferences sharedPreferences = getSharedPreferences("zjl", Context.MODE_PRIVATE);
            //获取editor对象
            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
            //存储键值对
            editor.putString("friendCircleContent", friendCircleContent);
            //提交
            editor.commit();//提交修改

            Log.e("111", StaticData.jobList.get(i).getComment() + ".............................");
            StaticData.friendCircleAddress = StaticData.jobList.get(i).getAddress();
            StaticData.currentTaskId = String.valueOf(StaticData.jobList.get(i).getId());
            StaticData.jobList.remove(i);
        }
        return true;
    }

    /**
     * 退出登录
     */
    private void loginout() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        MJSONObjectRequest jsonObjectRequest = new MJSONObjectRequest(HttpUtils.LOGIN_OUT_URL, "", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("", "onResponse:>>> " + response.toString());
                PreferenceUtil.setLoginStatus(RunningActivity.this, false);
                startActivity(new Intent(RunningActivity.this, LoginActivity.class));
                RunningActivity.this.finish();
                MyApp.manager.getCookieStore().removeAll();
                StaticData.isStartTask = false;
                Log.d("cookies>>>", "onResponse: " + MyApp.manager.getCookieStore().getCookies().toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "onErrorResponse: " + error.getMessage());
                Toast.makeText(RunningActivity.this, "网络繁忙！请稍后再试！", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mHeaders = new HashMap<String, String>(1);
                Log.d("Cookie", "getHeaders: " + MyApp.manager.getCookieStore().getCookies().toString());
                mHeaders.put("Cookie", MyApp.manager.getCookieStore().getCookies().toString());
                return mHeaders;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * 执行任务完成回调
     */
    @Override
    public void onCompleted() {
        Log.d("互聊完成》》", "互聊完成。。");
    }

    /**
     * 设置手机屏幕亮度
     *
     * @param activity
     * @param brightness
     */
    public void saveBrightness(Activity activity, int brightness) {
        setScrennManualMode();
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        getContentResolver().notifyChange(uri, null);
    }

    public void setScrennManualMode() {
        ContentResolver contentResolver = getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置当前页屏幕亮度
     *
     * @param brightness
     */
    private void setWindowBrightness(int brightness) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private boolean enableKeyguard = true;//默认有屏幕锁

    /**
     * 唤醒和解锁相关
     */
    private void wakeAndUnlock(boolean unLock) {
        if (unLock) {
            if (!pm.isScreenOn()) {
                //获取电源管理器对象
                wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
                //点亮屏幕
                wl.acquire();
                Log.i("demo", "亮屏");
            }
            if (km.inKeyguardRestrictedInputMode()) {
                //解锁
                enableKeyguard = false;
                //kl.reenableKeyguard();
                kl.disableKeyguard();
                Log.i("demo", "解锁");
            }
        } else {
            if (!enableKeyguard) {
                //锁屏
                kl.reenableKeyguard();
                Log.i("demo", "加锁");
            }
            if (wl != null) {
                //释放wakeLock，关灯
                wl.release();
                wl = null;
                Log.i("demo", "关灯");
            }
        }
    }

    private void cancelGetJob(String ids, Integer wxListFlag) {
        Map<String, String> map = new HashMap<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.put("deviceInfo", MyApp.tm.getDeviceId());
        if (!ids.isEmpty())
            map.put("ids", ids);
        if (wxListFlag != null)
            map.put("wxListFlag", String.valueOf(wxListFlag));
        String params = appendParameter(HttpUtils.CANCEL_JOB_LIST_URL, map);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        MJSONObjectRequest jsonObjectRequest = new MJSONObjectRequest(HttpUtils.CANCEL_JOB_LIST_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("", "onResponse:>>> " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "onErrorResponse: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mHeaders = new HashMap<String, String>(1);
                Log.d("Cookie", "getHeaders: " + MyApp.manager.getCookieStore().getCookies().toString());
                mHeaders.put("Cookie", MyApp.manager.getCookieStore().getCookies().toString());
                return mHeaders;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    /**
     * 更新任务执行状态
     *
     * @param status -1失败 0=未开始 1成功 2异常 -2删掉
     */
    private void doOneTaskStatus(final int status) {
        Log.d("isDoJob>>>", "doOneTaskStatus: RunningActivity更新任务状态" + StaticData.currentTaskId);
        if (StaticData.currentTaskId.isEmpty()) {
            Toast.makeText(this, "任务Id为空哦", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest jsonRequest = new StringRequest(Request.Method.POST, HttpUtils.UPDATE_TASK_STATUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("更新任务状态》》", "response -> " + response);
                        StaticData.isDoJob = false;
                        StaticData.currentTaskId = "";
                        Log.d("isDoJob>>>", "执行任务结束》》》>>执行状态" + StaticData.isDoJob);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ffff", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("taskId", StaticData.currentTaskId);
                map.put("status", String.valueOf(status));
                return map;
            }
        };
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonRequest);
    }

    public void jumpToAddFriend(View view) {
        jumpToAddFriend();
    }

    public void jumpToAddFriend() {
        List<PhoneUser> phoneUsers = PhoneUserHelper.selectPhoneByTime();//今天操作的微信号
        int searchNum = 0; //搜索加好友个数
        int addressBookNum = 0;//通讯录加好友个数
        for (int i = 0; i < phoneUsers.size(); i++) {
            int addType = phoneUsers.get(i).getAddType();
            if (addType == 1) {//搜索加好友
                searchNum++;
            } else if (addType == 2)//通讯录加好友
                addressBookNum++;
        }
        if (searchNum < 20) {//可搜索添加的个数
            if (20 - searchNum >= 2) {
                searchNum = 2;//一次最多搜索添加五个
            } else {
                searchNum = 20 - searchNum;
            }
        } else
            searchNum = 0;
        if (addressBookNum < 50) {//可通讯录添加的个数
            if (50 - addressBookNum >= 10)
                addressBookNum = 10;//一次最多通讯录添加十个
            else
                addressBookNum = 50 - addressBookNum;
        } else
            addressBookNum = 0;
        List<PhoneUser> unphoneUsers = PhoneUserHelper.selectPhoneByStatus(0);//未操作过的微信号
        for (int i = 0; i < unphoneUsers.size(); i++) {
            if (i < searchNum + addressBookNum) {
                PhoneUser phoneUser = unphoneUsers.get(i);
                StaticData.thePhoneUsers.add(phoneUser);
                if (i >= searchNum) {//前五个搜索加好友，后面的加入通讯录
                    ContactData contactData = new ContactData();
                    contactData.setContactName(phoneUser.getPhoneNum());
                    contactData.setNumber(phoneUser.getPhoneNum());
                    ContactsAccessPublic.insertPhoneContact(RunningActivity.this, contactData);//插入电话本
                }
            }
        }
        StaticData.isCanAddFriend = false;
        StaticData.isAddFriend = true;
        StaticData.isDoJob = true;
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        RunningActivity.this.startActivity(intent);
    }

    private TelephonyManager telephonyManager;

    /**
     * 获取电话号码
     */
/*
    public String getNativePhoneNumber() {
        String NativePhoneNumber = null;
        NativePhoneNumber = telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }
*/

    public void changeSql(View view) {
        //
        PhoneUserHelper.updateStatus((long) 2824, 1);
        PhoneUserHelper.updateTime((long) 2824);
    }

    /**
     * 更新添加好友成功的状态
     */
    private void addSuccess() {
        Log.d("isDoJob>>>", "添加成功");
        final long id = PhoneUserHelper.selectPhoneByName("简单 ");
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest jsonRequest = new StringRequest(Request.Method.POST, HttpUtils.ADD_SUCCESS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("更新电话状态", "response -> " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ffff", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("wxName", "简单 ");
                map.put("ids", id + "#" + "简单 ");
                return map;
            }
        };
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonRequest);
    }

    public void add(View view) {
//        addSuccess();
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-mm-ss");
        String format = simpleDateFormat.format(date);
        String[] split = format.split("-");
        Log.d(TAG, "add: " + format + "<<split>>" + split[0] + "<<>>" + split[1]);
    }

    /**
     * 开启互聊,设置几个闹钟
     *
     * @param
     */
    public void chat() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setWindowBrightness(0);
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-mm-ss");
        String format = simpleDateFormat.format(date);
        String[] split = format.split("-");
//        int hh = Integer.valueOf(split[0]);
//        int mm = Integer.valueOf(split[1]);
//        int max = ((mm + 15)>=60)?60:(mm+15);
//        int min;
//        if ((mm + 5)>=60){
//            hh = hh+1;
//            min = 0;
//            max = 15;
//        }else {
//            min = mm+15;
//        }
//        Random random = new Random();
//        int s = random.nextInt(max) % (max - min + 1) + min;
        int hh = Integer.valueOf(split[0]);
        int mm = Integer.valueOf(split[1]);
//        int max = ((mm + 2)>=60)?60:(mm+2);
        int min;
        if ((mm + 3) >= 60) {
            hh = hh + 1;
            mm = 1;
//            max = 2;
        } else {
            mm = mm + 2;
        }
        String timeStr = "";
        if (hh < 10) {
            timeStr = "0";
        }
        timeStr = timeStr + hh + ":";
        if (mm < 10) {
            timeStr = timeStr + "0";
        }
        timeStr = timeStr + mm;
        LogToFile.d("互聊中", timeStr + "将会进行聊天,人数：" + StaticData.friendBeanList.size());
        setAlarmTime(0, mm, hh);
    }

    public void tongguo(View view) {
        StaticData.isCheckFriend = true;
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        startActivity(intent);
    }

    public static final long DAY = 1000L * 60 * 60 * 24;

    /**
     * 设置闹钟
     *
     * @param alarmId 闹钟Id
     * @param minute  分钟
     * @param hour    小时
     */
    public void setAlarmTime(int alarmId, int minute, int hour) {

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, alarmId, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();    // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 选择的每天定时时间
        long selectTime = calendar.getTimeInMillis();

        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            //设置的时间小于当前时间
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }

        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;

        // 进行闹铃注册
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, DAY, sender);
        Log.i(TAG, "time ==== " + time + ", selectTime ===== "
                + selectTime + ", systemTime ==== " + systemTime + ", firstTime === " + firstTime + "设置简单闹铃成功! " + alarmId + "<<hour>>" + hour + "<<minute>>" + minute);
    }

    public void getFriend() {
        StaticData.friendBeanList.clear();
        StaticData.isGetFriendInfo = true;
        StaticData.isGetFriendInfoStart = true;
        StaticData.isDoJob = true;
        StaticData.wechatAuto = false;
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        RunningActivity.this.startActivity(intent);
    }

    public void getwxInfo(View view) {
        StaticData.isGetWxInfo = true;
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        RunningActivity.this.startActivity(intent);
    }

    public void getNumInfo(View view) {
        StaticData.isGetNum = true;
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        RunningActivity.this.startActivity(intent);
    }

    public void guanbi(View view) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        am.killBackgroundProcesses("com.tencent.mm");
    }







    ProgressDialog progressDialog ;
    /**
     *
     * @Title: showProcessDialoig
     * @Description:
     * @param msg void
     *
     */
    public void showProcessDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        if (!TextUtils.isEmpty(msg)){
//            progressDialog.setMessage(msg);
            progressDialog.setTitle(msg);
        }else{
            progressDialog.setMessage("正在加载...");
        }

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }



    /**
     * 获取手机号码
     */
    private void getPhoneAndIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        /**
         * 获取手机号码
         */
        @SuppressLint("MissingPermission") String phoneNumber1 = telephonyManager.getLine1Number();
        /**
         * 获取手机IMEI号
         */
        @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
        Constants.phone = phoneNumber1;
        Constants.imei = imei;
        Log.e("111",phoneNumber1+"----"+imei);

        getWxInfo();
    }



    private void getWxInfo() {
        LogUtils.d(TAG, "跳转去微信");
        Constants.isGetWxInfo = true;
        try {
            Intent intent;
            intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showLongToast("检查到您手机没有安装微信，请安装后使用该功能");
            e.printStackTrace();
        }
    }

    private void initVersion() {
        String version = MyFileUtil.readFromFile(AppConfig.APP_FOLDER + "/version");
        LogUtils.w(TAG, "version:" + version + " regId:" + AppConfig.Registration_Id);
        if (version == null) {
            version = UI.WX_VERSION;
        }
        UI.initUIVersion(version);
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






    /**
     * 设置标签与别名
     */
    private void setTagAndAlias(Context context) {
        /**
         *这里设置了别名，在这里获取的用户登录的信息
         *并且此时已经获取了用户的userId,然后就可以用用户的userId来设置别名了
         **/
        //false状态为未设置标签与别名成功
        //if (UserUtils.getTagAlias(getHoldingActivity()) == false) {
        Set<String> tags = new HashSet<String>();
        //这里可以设置你要推送的人，一般是用户uid 不为空在设置进去 可同时添加多个
        String DEVICE_ID  = null;
        try {
            DEVICE_ID = GetImeiUtil.getOnlyIdentification(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("111",DEVICE_ID);
        if (!TextUtils.isEmpty(DEVICE_ID)){
            tags.add(DEVICE_ID);//设置tag
        }
        //上下文、别名【Sting行】、标签【Set型】、回调
        JPushInterface.setAliasAndTags(this,DEVICE_ID, tags,
                mAliasCallback);

    }



    /**
     * /**
     * TagAliasCallback类是JPush开发包jar中的类，用于
     * 设置别名和标签的回调接口，成功与否都会回调该方法
     * 同时给定回调的代码。如果code=0,说明别名设置成功。
     * /**
     * 6001   无效的设置，tag/alias 不应参数都为 null
     * 6002   设置超时    建议重试
     * 6003   alias 字符串不合法    有效的别名、标签组成：字母（区分大小写）、数字、下划线、汉字。
     * 6004   alias超长。最多 40个字节    中文 UTF-8 是 3 个字节
     * 6005   某一个 tag 字符串不合法  有效的别名、标签组成：字母（区分大小写）、数字、下划线、汉字。
     * 6006   某一个 tag 超长。一个 tag 最多 40个字节  中文 UTF-8 是 3 个字节
     * 6007   tags 数量超出限制。最多 100个 这是一台设备的限制。一个应用全局的标签数量无限制。
     * 6008   tag/alias 超出总长度限制。总长度最多 1K 字节
     * 6011   10s内设置tag或alias大于3次 短时间内操作过于频繁
     **/
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    //这里可以往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    //UserUtils.saveTagAlias(getHoldingActivity(), true);
                    logs = "Set tag and alias success极光推送别名设置成功";
                    Log.e("TAG", logs);
                    break;
                case 6002:
                    //极低的可能设置失败 我设置过几百回 出现3次失败 不放心的话可以失败后继续调用上面那个方面 重连3次即可 记得return 不要进入死循环了...
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.极光推送别名设置失败，60秒后重试";
                    Log.e("TAG", logs);
                    break;
                default:
                    logs = "极光推送设置失败，Failed with errorCode = " + code;
                    Log.e("TAG", logs);
                    break;
            }
        }
    };


    public void getAppVersionName() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            tvVersion.setText("当前软件版本："+ packageInfo.versionName+"");
            //ToastUtil.showLongToast(VersionInfo.versionCode+"");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Integer version = Integer.valueOf(packageInfo.versionCode);*/
           /* tvVersion.setText("当前软件版本："+ VersionInfo.versionName+"----");
            ToastUtil.showLongToast(VersionInfo.versionName+"");*/
    }


    //检验Xpose是否勾上
    public void  isModuleActive(String s) {

        tv2.setText(s);
    }


    protected void downLoadApk(final Context context) {
        File file=new File(AppConfig.APP_FILE);
        if (!file.exists()){
            file.mkdir();
        }

     try{
         RequestParams params = new RequestParams("http://upyun.ijucaimao.cn/fileforapp/zsp343757500/app-release.apk");
         params.setAutoRename(true);//断点下载

         params.setSaveFilePath(AppConfig.APP_FILEAPK);
         x.http().get(params, new Callback.ProgressCallback<File>() {
             @Override
             public void onCancelled(CancelledException cex) {

             }

             @Override
             public void onError(Throwable ex, boolean isOnCallback) {
                 Log.e("333",ex.toString());
                 if(progressDialog!=null && progressDialog.isShowing()){
                     progressDialog.dismiss();
                 }
                 ToastUtil.showShortToast("更新失败");
             }


             @Override
             public void onFinished() {

             }

             @Override
             public void onSuccess(File result) {
                 // TODO Auto-generated method stub
                 if(progressDialog!=null && progressDialog.isShowing()){
                     progressDialog.dismiss();
                 }

                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                     intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                     Uri contentUri = FileProvider.getUriForFile(context, "com.mikuwxc.autoreply.fileProvider", new File(AppConfig.APP_FILEAPK));
                     intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                     intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                 } else {
                     intent.setDataAndType(Uri.fromFile(new File(Environment
                             .getExternalStorageDirectory(), "hsl.apk")), "application/vnd.android.package-archive");
                     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 }
                 context.startActivity(intent);
             }


             @Override
             public void onLoading(long total, long current, boolean isDownloading) {
                 progressDialog.setMax((int)total);
                 progressDialog.setProgress((int)current);
             }

             @Override
             public void onStarted() {
                 // TODO Auto-generated method stub
                 System.out.println("开始下载");
                 progressDialog = new ProgressDialog(RunningActivity.this);
                 progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置为水平进行条
                 progressDialog.setMessage("正在下载中...");
                 progressDialog.setProgress(0);
                 progressDialog.show();
             }

             @Override
             public void onWaiting() {

             }
         });
     }catch (Exception e){
         Log.e("111",e.toString());
     }



    }



    // 弹出更新的对话框
    protected void showUpdateDialog() {
        // 1 构建AlertDialog.Buidler的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);// 点击外面，对话不可以取消
        // 2 设置标题
        builder.setTitle("是否进行更新");
        // 3 设置消息（内容）
        builder.setMessage("---");
        // 4 设置按钮（正负）
        builder.setPositiveButton("升级", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 升级逻辑：下载apk，安装
                downLoadApk(RunningActivity.this);
            }
        });
        builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // loadMain();
                //loadDelayedMain();
            }
        });
        // 5 显示
        builder.show();
        // 6 细节
    }
}


