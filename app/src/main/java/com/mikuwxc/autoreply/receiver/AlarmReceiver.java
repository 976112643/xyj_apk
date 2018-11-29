package com.mikuwxc.autoreply.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.service.LoopService;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;

public class AlarmReceiver extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {
        //context.getApplicationContext().startService(new Intent(context.getApplicationContext(), LoopUploadService.class));
        context.getApplicationContext().startService(new Intent(context.getApplicationContext(), LoopService.class));
        if (intent!=null) {
            if ("restartService".equals(intent.getAction())) {
                Toast.makeText(context, "重启LoospService", Toast.LENGTH_SHORT).show();
                context.getApplicationContext().startService(new Intent(context.getApplicationContext(), LoopService.class));
            }
        }else{
            Log.e("TAG","intent为null");
        }

    }


}
