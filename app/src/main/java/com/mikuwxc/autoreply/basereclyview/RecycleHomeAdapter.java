package com.mikuwxc.autoreply.basereclyview;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.mikuwxc.autoreply.R;
import com.mikuwxc.autoreply.bean.ApphttpBean;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class RecycleHomeAdapter extends BaseRecycleAdapter {


    private Context mContent;
    private ArrayList<ApphttpBean.ResultBean> homePages;
    private ImageView imageView;

    public RecycleHomeAdapter(Context mContent, ArrayList<ApphttpBean.ResultBean> homePages) {
        this.mContent = mContent;
        this.homePages = homePages;
    }

    private BaseOnRecycleClickListener clickListener;

    public BaseOnRecycleClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(BaseOnRecycleClickListener clickListener) {
        this.clickListener = clickListener;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(View.inflate(mContent, R.layout.recycle_home_child, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).instanceView(position);
    }

    @Override
    public int getItemCount() {
        return homePages.size();
    }

    class Holder extends BaseHolder implements BaseRecycleHolder.RecycleViewItemClickListener, View.OnClickListener {
        public Holder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }
        TextView mTv;

        public void assignViews() {
            mTv = (TextView) findViewById(R.id.tv_child);
            imageView = (ImageView) findViewById(R.id.iv_child);
        }


        @Override
        public void instanceView(final int position) {
            String name = homePages.get(position).getName();
            PackageManager pm = mContent.getPackageManager();
            try {

                ApplicationInfo appInfo = pm.getApplicationInfo(homePages.get(position).getPackageName(), PackageManager.GET_META_DATA);
                //appNameTv.setText(pm.getApplicationLabel(appInfo));
                Drawable appIcon = pm.getApplicationIcon(appInfo);

               /* if (0==position){
                    ApplicationInfo appInfo1 = pm.getApplicationInfo("com.mikuwxc.autoreply", PackageManager.GET_META_DATA);
                    Drawable appIcon1 = pm.getApplicationIcon(appInfo1);
                    imageView.setImageDrawable(appIcon1);
                    mTv.setText("" + "设置");
                }else {
                    imageView.setImageDrawable(appIcon);
                    mTv.setText("" + name);
                }*/

               if ("com.mikuwxc.autoreply".equals(homePages.get(position).getPackageName())){
                   ApplicationInfo appInfo1 = pm.getApplicationInfo("com.mikuwxc.autoreply", PackageManager.GET_META_DATA);
                   Drawable appIcon1 = pm.getApplicationIcon(appInfo1);
                   imageView.setImageDrawable(appIcon1);
                   mTv.setText("" + "设置");
               }else{
                   imageView.setImageDrawable(appIcon);
                   mTv.setText("" + name);
               }


            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }


        @Override
        public void OnRecycleItemClick(View v, int position) {
            getClickListener().OnRecycleItemClick(getPosition());
        }

        @Override
        public void onClick(View view) {
            getClickListener().OnRecycleItemClick(getPosition());
        }
    }



}
