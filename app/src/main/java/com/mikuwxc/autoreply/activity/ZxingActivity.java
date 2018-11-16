package com.mikuwxc.autoreply.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mikuwxc.autoreply.R;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;


public class ZxingActivity extends  PermissionsActivity{
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_zxing);

        //new IntentIntegrator(this).initiateScan();
        Intent intent=new Intent(ZxingActivity.this,CaptureActivity.class);
        startActivityForResult(intent,REQUEST_CODE);
    }
    //回调获取扫描得到的条码值
    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "扫码取消！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "扫描成功，条码值: " + result.getContents(), Toast.LENGTH_LONG).show();
                MyFileUtil.writeToNewFile(AppConfig.APP_FILE + "/tenantConfig",result.getContents());
                goBackHome();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public final void goBackHome() {
        startActivity(new Intent(this,DesktopActivity.class));
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=new Intent(ZxingActivity.this,CaptureActivity.class);
        startActivityForResult(intent,REQUEST_CODE);
    }
}
