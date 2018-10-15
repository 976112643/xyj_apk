package com.mikuwxc.autoreply.callrecorder;

import android.content.Context;

import com.mikuwxc.autoreply.callrecorder.sources.CallRecord;
import com.mikuwxc.autoreply.callrecorder.sources.receiver.CallRecordReceiver;
import com.mikuwxc.autoreply.common.util.ToastUtil;

import java.util.Date;

public class MyCallRecordReceiver extends CallRecordReceiver {

    public MyCallRecordReceiver(CallRecord callRecord) {
        super(callRecord);

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //callRecord.disableSaveFile();
        super.onOutgoingCallStarted(ctx, number, start);
        ToastUtil.showLongToast(number+start);
    }
}
