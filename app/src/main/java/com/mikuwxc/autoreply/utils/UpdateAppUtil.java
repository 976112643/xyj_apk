package com.mikuwxc.autoreply.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.R;
import com.mikuwxc.autoreply.activity.DesktopActivity;
import com.mikuwxc.autoreply.activity.RunningActivity;
import com.mikuwxc.autoreply.bean.AppHotVersionBean;
import com.mikuwxc.autoreply.common.VersionInfo;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.Constants;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.modle.AppVersionBean;
import com.mikuwxc.autoreply.modle.HttpImeiBean;
import com.taobao.sophix.SophixManager;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

import okhttp3.Call;

public class UpdateAppUtil {
    private static ProgressDialog progressDialog ;
    public static void getAppVersion(final Context context){
        OkGo.get(AppConfig.OUT_NETWORK + NetApi.upDateAppVersion).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                Log.e("111","result:" + s);
                try {
                    HttpImeiBean<AppVersionBean> bean = new Gson().fromJson(s, new TypeToken<HttpImeiBean<AppVersionBean>>(){}.getType());
                    if (bean.isSuccess()) {
                        //获取当前app版本号与后台对比后台版本号大于本地app版本号时进行更新
                        if(bean.getResult().getVersionCode() > getAppVersionCode(context)){
                            ToastUtil.showShortToast("当前软件不是最新版本");
                        }
                        Log.e("111", "获取App版本信息成功:"+bean.getResult());
                    }else {
                        Log.e("111", "获取App版本信息失败:");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("111", "获取App版本信息失败:"+e.toString());
                }
            }
            @Override
            public void onError(Call call, okhttp3.Response response, Exception e) {
                super.onError(call, response, e);
                Log.e("111", "获取App版本信息失败:");
            }
        });
    }


    public static void getAppVersionDownLoad(final Context context){
        OkGo.get(AppConfig.OUT_NETWORK + NetApi.upDateAppVersion).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                Log.e("111","result:" + s);
                try {
                    HttpImeiBean<AppVersionBean> bean = new Gson().fromJson(s, new TypeToken<HttpImeiBean<AppVersionBean>>(){}.getType());
                    if (bean.isSuccess()) {
                        //获取当前app版本号与后台对比后台版本号大于本地app版本号时进行更新
                        if(bean.getResult().getVersionCode() > getAppVersionCode(context)){
                            String url=bean.getResult().getUrl();
                            showUpdateDialog(context,url);
                        }else{
                            Toast.makeText(context,"没有新的版本",Toast.LENGTH_LONG).show();
                        }
                        Log.e("111", "获取App版本信息成功:"+bean.getResult());
                    }else {
                        Log.e("111", "获取App版本信息失败:");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("111", "获取App版本信息失败:"+e.toString());
                }
            }
            @Override
            public void onError(Call call, okhttp3.Response response, Exception e) {
                super.onError(call, response, e);
                Log.e("111", "获取App版本信息失败:");
            }
        });
    }





    public static void getAppVersionState(final Context context, final TextView textView){
        OkGo.get(AppConfig.OUT_NETWORK + NetApi.upDateAppVersion).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                Log.e("111","result:" + s);
                try {
                    HttpImeiBean<AppVersionBean> bean = new Gson().fromJson(s, new TypeToken<HttpImeiBean<AppVersionBean>>(){}.getType());
                    if (bean.isSuccess()) {
                        //获取当前app版本号与后台对比后台版本号大于本地app版本号时进行更新
                             textView.setText("最新软件版本: "+bean.getResult().getVersion());
                        Log.e("111", "获取App版本信息成功:"+bean.getResult());
                    }else {
                        Log.e("111", "获取App版本信息失败:");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("111", "获取App版本信息失败:"+e.toString());
                }
            }
            @Override
            public void onError(Call call, okhttp3.Response response, Exception e) {
                super.onError(call, response, e);
                Log.e("111", "获取App版本信息失败:");
            }
        });
    }





    public static void getHotAppVersion(final Context context){
        int appVersionCode = getAppVersionCode(context);

        OkGo.get(AppConfig.OUT_NETWORK + NetApi.upDateHot+"?"+"applyTo="+appVersionCode).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                Log.e("111","result:" + s);
                try {
                    HttpImeiBean<AppHotVersionBean> bean = new Gson().fromJson(s, new TypeToken<HttpImeiBean<AppHotVersionBean>>(){}.getType());
                    if (bean.isSuccess()) {
                        //获取当前app版本号与后台对比后台版本号大于本地app版本号时进行更新
                        if(bean.getResult()!=null&&bean.getResult().getVersion()>(VersionInfo.versionCode)){
                         /*queryAndLoadNewPatch不可放在attachBaseContext 中，
                           否则无网络权限，建议放在后面任意时刻，如onCreate中*/
                            //去阿里看是否有补丁包
                            //Toast.makeText(context,"需要补丁更新",Toast.LENGTH_LONG).show();
                            SophixManager.getInstance().queryAndLoadNewPatch();

                        }else{
                           // Toast.makeText(context,"不需要补丁更新",Toast.LENGTH_LONG).show();
                        }

                        Log.e("111", "获取App版本信息成功:"+bean.getResult());
                    }else {
                        Log.e("111", "获取App版本信息失败:");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("111", "获取App版本信息失败:"+e.toString());
                }
            }
            @Override
            public void onError(Call call, okhttp3.Response response, Exception e) {
                super.onError(call, response, e);
                Log.e("111", "获取App版本信息失败:");
            }
        });
    }


    private static int getAppVersionCode(Context context){

           PackageInfo packageInfo = null;
        try {
            packageInfo = context.getApplicationContext()
                          .getPackageManager()
                          .getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Integer version = Integer.valueOf(packageInfo.versionCode);

        return packageInfo.versionCode;

    }


    private static String getAppVersionName(Context context){

        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Integer version = Integer.valueOf(packageInfo.versionCode);

        return packageInfo.versionName;

    }


    // 弹出更新的对话框
    public static void showUpdateDialog(final Context context, final String url) {
        // 1 构建AlertDialog.Buidler的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                downLoadApk(context,url);
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


    // 弹出更新的对话框
    public static void showDialog(final Context context) {
        // 1 构建AlertDialog.Buidler的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);// 点击外面，对话不可以取消
        // 2 设置标题
        builder.setTitle("请输入管理员密码");
        View view = LayoutInflater.from(context).inflate(R.layout.dialog, null);
        final EditText password = (EditText)view.findViewById(R.id.password);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String b = password.getText().toString().trim();
                //    将输入的用户名和密码打印出来
                if (b.equals("admin")){
                    MyFileUtil.writeProperties(Constants.PACKAGESTAUE_PUT,"false");
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        builder.show();
        // 6 细节
    }



    public static void downLoadApk(final Context context,String url) {
        File file=new File(AppConfig.APP_FILE);
        if (!file.exists()){
            file.mkdir();
        }

        try{
            RequestParams params = new RequestParams(url);
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
                    progressDialog = new ProgressDialog(context);
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


    public static void removeApk(Context context){
        File file=new File(AppConfig.APP_FILEAPK);
        if (file.exists()){
            file.delete();
        }
    }

}
