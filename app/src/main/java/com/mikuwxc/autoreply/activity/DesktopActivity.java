package com.mikuwxc.autoreply.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.R;
import com.mikuwxc.autoreply.basereclyview.BaseOnRecycleClickListener;
import com.mikuwxc.autoreply.basereclyview.RecycleHomeAdapter;
import com.mikuwxc.autoreply.bean.ApphttpBean;
import com.mikuwxc.autoreply.bean.PermissionBean;
import com.mikuwxc.autoreply.bean.SystemBean;
import com.mikuwxc.autoreply.callrecorder.sources.CallRecord;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.common.util.SPHelper;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.receiver.MomentReceiver;
import com.mikuwxc.autoreply.receiver.NetworkChangeReceiver;
import com.mikuwxc.autoreply.service.MyReceiver;
import com.mikuwxc.autoreply.service.SmsObserverService;
import com.mikuwxc.autoreply.utils.GetImeiUtil;
import com.mikuwxc.autoreply.utils.Global;
import com.mikuwxc.autoreply.utils.PreferenceUtil;
import com.mikuwxc.autoreply.utils.SystemUtil;
import com.mikuwxc.autoreply.utils.UpdateAppUtil;
import com.mikuwxc.autoreply.wxmoment.MomentDBTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.richinfo.dualsim.TelephonyManagement;
import okhttp3.Call;
import okhttp3.Response;


public class DesktopActivity extends PermissionsActivity implements BaseOnRecycleClickListener, MyReceiver.BRInteraction {
    private RecycleHomeAdapter adapter;
    private RecyclerView recycleV;
    private ArrayList<ApphttpBean.ResultBean> resultBean;
    private ArrayList<ApphttpBean.ResultBean> newBean;
    private String tac;

    private TelephonyManagement.TelephonyInfo telephonyInfo;


    private String[] search = {
            //  "input keyevent 3",// 返回到主界面，数值与按键的对应关系可查阅KeyEvent
            // "sleep 1",// 等待1秒
            // 打开微信的启动界面，am命令的用法可自行百度、Google// 等待3秒
            "pm disable " + "com.android.settings",
            // "am  start  service  com.mikuwxc.autoreply.AutoReplyService"// 打开微信的搜索
            // 像搜索框中输入123，但是input不支持中文，蛋疼，而且这边没做输入法处理，默认会自动弹出输入法
    };
    Intent smsObserverIntent;
    private CallRecord callRecord;
    private IntentFilter netWorkIntentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private MomentReceiver momentReceiver;
    private IntentFilter momentDBFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_desktop);
        EventBus.getDefault().register(this);
        ImageView iv = (ImageView) findViewById(R.id.iv);
        recycleV = (RecyclerView) findViewById(R.id.recycleV);
        Button btAdmin=(Button) findViewById(R.id.bt_admin);
        // 状态栏透明
        Global.setNoStatusBarFullMode(this);
        // 状态栏设为黑包
        // Global.setStatusBarColor(this, Color.BLUE);
        //设置极光推送的别名
        setTagAndAlias(this);
        //注册广播收到极光推送的时候可以回调接口更新请求桌面
        IntentFilter intentFilter = new IntentFilter();
        MyReceiver dianLiangBR = new MyReceiver();
        registerReceiver(dianLiangBR, intentFilter);
        dianLiangBR.setBRInteractionListener(this);
        getAppList(this);
        SPHelper.init(this);
        smsObserverIntent = new Intent(this, SmsObserverService.class);
        startService(smsObserverIntent);//短信监听
        startRecordService();//电话监听
        startNetWorkBroadcastReceiver();//断网重连后短信 短话上传
        startMomentDBReceiver();//开启朋友圈列表数据库的监听
        String lngAndLat = getLngAndLat(this);
        UpdateAppUtil.removeApk(this);
    }

    private void startMomentDBReceiver() {
        momentReceiver = new MomentReceiver();
        momentDBFilter = new IntentFilter();
        momentDBFilter.addAction("moment");
        registerReceiver(momentReceiver,momentDBFilter);
    }


    @Override
    public void getApplistPackage() {
        super.getApplistPackage();
        getAppList(this);
    }

    private void startNetWorkBroadcastReceiver() {
        netWorkIntentFilter = new IntentFilter();
        netWorkIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, netWorkIntentFilter);
    }

    private void startRecordService() {
        callRecord = new CallRecord.Builder(this)
                .setRecordFileName("CallRecorderTestFile")
                .setRecordDirName("CallRecorderTest")
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setShowSeed(true)
                .build();
        callRecord.startCallReceiver();
    }


    // app启动时禁用返回键以防闪屏处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void OnRecycleItemClick(int position) {
        try {
            String packageName1 = newBean.get(position).getPackageName();
            if ("com.mikuwxc.autoreply".equals(packageName1)) {
                Intent intent = new Intent(this, RunningActivity.class);
                this.startActivity(intent);
            } else {
                String packageName = newBean.get(position).getPackageName();
                if ("com.android.phone".equals(packageName)) {
                    Intent touchDialIntent = new Intent("com.android.phone.action.TOUCH_DIALER");
                    startActivity(touchDialIntent);
                } else {
                    Intent resolveIntent = getPackageManager().getLaunchIntentForPackage(packageName);// 这里的packname就是从上面得到的目标apk的包名
                    startActivity(resolveIntent);// 启动目标应用
                }
            }

        } catch (Exception e) {
            Log.e("111", e.toString());
        }

    }


    public void getAppList(final Context context) {
        try{
            if (Build.VERSION.SDK_INT >= 23) {
                telephonyInfo = TelephonyManagement.getInstance().updateTelephonyInfo(this).getTelephonyInfo(this);
            } else {
                isDualSimOrNot();

            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }

            String DEVICE_ID = GetImeiUtil.getOnlyIdentification(context);
            //登录IM
            //String DEVICE_ID1 = telephonyInfo.getImeiSIM1();
            Toast.makeText(this,DEVICE_ID,Toast.LENGTH_LONG).show();
            Log.e("111", "DEVICE_IDDEVICE_IDDEVICE_IDDEVICE_ID" + DEVICE_ID);
            if (DEVICE_ID != null) {
                OkGo.get(AppConfig.OUT_NETWORK + NetApi.getAppList + DEVICE_ID).execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, okhttp3.Response response) {
                        Log.e("111", "result:" + s);
                        ApphttpBean apphttpBean = new Gson().fromJson(s, ApphttpBean.class);
                        if (apphttpBean != null) {
                            resultBean = (ArrayList<ApphttpBean.ResultBean>) apphttpBean.getResult();
                            PreferenceUtil.setWxAccessToken(context, s);
                            newBean = new ArrayList<>();
                            if (resultBean != null) {
                                //newBean.add(resultBean.get(0));
                                for (int i = 0; i < resultBean.size(); i++) {
                                    PackageManager pm = getApplicationContext().getPackageManager();
                                    try {
                                        ApplicationInfo appInfo = pm.getApplicationInfo(resultBean.get(i).getPackageName(), PackageManager.GET_META_DATA);
                                        Drawable appIcon = pm.getApplicationIcon(appInfo);
                                        newBean.add(resultBean.get(i));
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            adapter = new RecycleHomeAdapter(getApplicationContext(), newBean);
                            recycleV.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
                            recycleV.setAdapter(adapter);
                            adapter.setClickListener((BaseOnRecycleClickListener) context);
                        }
                    }

                    @Override
                    public void onError(Call call, okhttp3.Response response, Exception e) {
                        super.onError(call, response, e);
                        Log.e("111", "e" + e.toString());
                        String wxAccessToken = PreferenceUtil.getWxAccessToken(context);
                        ApphttpBean apphttpBean = new Gson().fromJson(wxAccessToken, ApphttpBean.class);
                        if (apphttpBean != null) {
                            resultBean = (ArrayList<ApphttpBean.ResultBean>) apphttpBean.getResult();
                            PreferenceUtil.setWxAccessToken(context, wxAccessToken);
                            newBean = new ArrayList<>();
                            if (resultBean != null) {
                                for (int i = 0; i < resultBean.size(); i++) {
                                    PackageManager pm = getApplicationContext().getPackageManager();
                                    try {
                                        ApplicationInfo appInfo = pm.getApplicationInfo(resultBean.get(i).getPackageName(), PackageManager.GET_META_DATA);
                                        Drawable appIcon = pm.getApplicationIcon(appInfo);
                                        newBean.add(resultBean.get(i));
                                    } catch (PackageManager.NameNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                            adapter = new RecycleHomeAdapter(getApplicationContext(), newBean);
                            recycleV.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
                            recycleV.setAdapter(adapter);
                            adapter.setClickListener((BaseOnRecycleClickListener) context);
                        }
                    }
                });



                OkGo.get(AppConfig.OUT_NETWORK + NetApi.appPermission + DEVICE_ID).execute(new StringCallback() {
                    @Override
                    public void onSuccess(String result, Call call, Response response) {
                        Log.e("111","设置权限"+result.toString());
                        PermissionBean permissionBean = new Gson().fromJson(result, PermissionBean.class);
                        if (permissionBean.getResult().isSetting()){  //系统设置权限开启
                            MyFileUtil.writeProperties("systemstting_put","true");
                        }else{
                            MyFileUtil.writeProperties("systemstting_put","false");
                        }


                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        Log.e("111","设置权限22"+e.toString());
                    }
                });

            }
        }catch (Exception e){
            Log.e("111",e.toString());
        }

    }

    @Override
    public void setText(String content) {
        getAppList(this);
    }


    /**
     * 设置标签与别名
     */
    private void setTagAndAlias(Context context) {
        /**
         *这里设置了别名，在这里获取的用户登录的信息
         *并且此时已经获取了用户的userId,然后就可以用用户的userId来设置别名了
         **/
        Set<String> tags = new HashSet<String>();
        String DEVICE_ID = null;
        try {
            DEVICE_ID = GetImeiUtil.getOnlyIdentification(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("111", DEVICE_ID);
        if (!TextUtils.isEmpty(DEVICE_ID)) {
            tags.add(DEVICE_ID);//设置tag
        }
        //上下文、别名【Sting行】、标签【Set型】、回调
        JPushInterface.setAliasAndTags(this, DEVICE_ID, tags,
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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(smsObserverIntent);
        callRecord.stopCallReceiver();
        unregisterReceiver(networkChangeReceiver);
        unregisterReceiver(momentReceiver);
        MomentReceiver.runHandle.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
    }


    private void isDualSimOrNot() {
        Log.e("TAG", "---------------------------------------");
//        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
//        UMCTelephonyManagement utm = UMCTelephonyManagement.getInstance();
//        telephonyInfo = utm.updateTelephonyInfo(this).getTelephonyInfo(this);
        String imeiSIM1 = telephonyInfo.getImeiSIM1();
        String imeiSIM2 = telephonyInfo.getImeiSIM2();

        String imsiSIM1 = telephonyInfo.getImsiSIM1();
        String imsiSIM2 = telephonyInfo.getImsiSIM2();

        int isSIM1Ready = telephonyInfo.getStateSIM1();
        int isSIM2Ready = telephonyInfo.getStateSIM2();

        boolean isDualSIM = telephonyInfo.isDualSIM();
//        int networkState = telephonyInfo.getNetworkState();
        int slotId = telephonyInfo.getDefaultDataSlotId();
        Log.e("TAG", "---------------------------------------" + imeiSIM1);
    }


    private String getLngAndLat(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  //从gps获取经纬度
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return "";
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {//当GPS信号弱没获取到位置的时候又从网络获取
                return getLngAndLatWithNetwork();
            }
        } else {    //从网络获取经纬度
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return "";
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
        return longitude + "," + latitude;
    }

    //从网络获取经纬度
    public String getLngAndLatWithNetwork() {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return "";
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        return longitude + "," + latitude;
    }


    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
        }
    };


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void alertSmsDialog(String str){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("短信敏感词提醒")
                .setMessage("短信敏感词包含"+str)
                .create();
        alertDialog.show();
    }


    public void click(View v){
        if (v.getId()==R.id.bt_admin){
            UpdateAppUtil.showDialog(this);
        }
    }



}

