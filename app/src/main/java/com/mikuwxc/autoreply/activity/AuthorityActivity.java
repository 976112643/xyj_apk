package com.mikuwxc.autoreply.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.mikuwxc.autoreply.R;

public class AuthorityActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        TextView tvAuthor = (TextView) findViewById(R.id.tvAuthor);

    }


    public void close(View v){
        goToHome(this);
        finish();

    }

    @SuppressLint("WrongConstant")
    public static void goToHome(Context context) {
        Intent home = new Intent("android.intent.action.MAIN");
        home.setFlags(67108864);
        home.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        home.addCategory("android.intent.category.HOME");
        context.startActivity(home);
    }

}
