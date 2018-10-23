package com.mikuwxc.autoreply.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.mikuwxc.autoreply.wxmoment.MomentDBTask;

public class MomentReceiver extends BroadcastReceiver {

    public static Handler runHandle=new Handler();

    @Override
    public void onReceive(Context context, Intent intent) {

        String source = intent.getStringExtra("source");
        if("进入了当前朋友圈".equals(source)){
            Toast.makeText(context, source, Toast.LENGTH_SHORT).show();
            runHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MomentDBTask.run();
                }
            },5*1000);
        }
    }



}
