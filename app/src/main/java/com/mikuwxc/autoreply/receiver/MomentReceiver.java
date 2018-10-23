package com.mikuwxc.autoreply.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MomentReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        String source = intent.getStringExtra("source");
        Toast.makeText(context, source, Toast.LENGTH_SHORT).show();
    }
}
