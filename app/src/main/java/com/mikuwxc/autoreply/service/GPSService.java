package com.mikuwxc.autoreply.service;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

public class GPSService extends Service {

	private LocationManager mLm;
	private MLocationListener mListener;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("444","GPSService启动");
		mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// * 2 请求监听（ requestLocationUpdates(String provider, long minTime, float
		// minDistance,
		// LocationListener listener)）
		Log.e("444","MLocationListenerMLocationListener");
		mListener = new MLocationListener();
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			return;
		}
		mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 5, mListener);

	}

	public class MLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.e("444", "location:");
			// 获取经度
			double longitude = location.getLongitude();
			// 获取纬度
			double latitude = location.getLatitude();
			Log.e("444", "longitude:" + longitude + ";latitude:" + latitude);
			// 获取安全号码
			// 发送经纬度给安全号码
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.e("444", "location:");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.e("444", "location:");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.e("444", "location:");
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消定位监听
		if (mListener != null) {
			mLm.removeUpdates(mListener);
		}
	}

}
