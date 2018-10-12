package com.mikuwxc.autoreply.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * 全局公共类, 封装：屏幕宽高获取，单位转换，主线程运行等
 * 
 * @author WJQ
 */
public class Global {
	
	public static Context mContext;
	
	public static float mDensity;
	public static float mScreenWidth;
	public static float mScreenHeight;

	public static void init(Context context) {
		mContext = context;
		initScreenSize();
	}

	private static void initScreenSize() {
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		mDensity = dm.density;
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
	}
	
	public static int dp2px(int dp) {
		return (int) (dp * mDensity);
	}
	
	public static View inflate(int layoutResID, ViewGroup parent) {
		return LayoutInflater.from(mContext).inflate(layoutResID, parent, false);
	}
	
	public static View inflate(int layoutResID) {
		return inflate(layoutResID, null);
	}
	
	private static Handler mHandler = new Handler(Looper.getMainLooper());
	
	public static Handler getMainHandler() {
		return mHandler;
	}
	
	/**
	 * 判断当前线程是否是主线程
	 * @return true表示当前是在主线程中运行
	 */
	public static boolean isUIThread() {
		return Looper.getMainLooper() == Looper.myLooper();
	}
	
	public static void runOnUIThread(Runnable run) {
		if (isUIThread()) {
			run.run();
		} else {
			mHandler.post(run);
		}
	}
	
	private static Toast mToast;

	/**
	 * 可以在子线程中调用
	 * @param msg toast内容
	 */
	public static void showToast(final String msg) {
		runOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (mToast == null) {
					mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
				}
				mToast.setText(msg);
				mToast.show();
			}
		});
	}

	public static String getString(int stringId) {
		return mContext.getResources().getString(stringId);
	}

	public static int getColor(int colorId) {
		return mContext.getResources().getColor(colorId);
	}


	//=============沉侵式==(begin)=================
	private static View mStatusBarView;

	/** 设置全屏沉侵式效果 */
	public static void setNoStatusBarFullMode(Activity activity) {
		// sdk 4.4
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			Window window = activity.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

			if (mStatusBarView != null) {
				ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
				root.removeView(mStatusBarView);
			}
			return;
		}

		// sdk 5.x
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.setStatusBarColor(Color.TRANSPARENT);
			return;
		}
	}

	/** 设置控件的paddingTop, 使它不被StatusBar覆盖 */
	public static void setStatusBarPadding(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// marginTop： 状态栏的高度
			int marginTop = getStatusBarHeight(view.getContext());
			view.setPadding(view.getPaddingLeft(), marginTop,
					view.getPaddingRight(), view.getPaddingBottom());
			return;
		}
	}

	/**
	 * 通过反射的方式获取状态栏高度，
	 * 一般为24dp，有些可能较特殊，所以需要反射动态获取
	 */
	private static int getStatusBarHeight(Context context) {
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object obj = clazz.newInstance();
			Field field = clazz.getField("status_bar_height");
			int id = Integer.parseInt(field.get(obj).toString());
			return context.getResources().getDimensionPixelSize(id);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("-------无法获取到状态栏高度");
		}
		return dp2px(24);
	}

	public static void setStatusBarColor(Activity activity, int statusColor) {
		// sdk 4.4
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
				if (mStatusBarView == null) {
					mStatusBarView = new View(activity);
					mStatusBarView.setBackgroundColor(statusColor);
				} else {
					// 先解除父子控件关系，否则重复把一个控件多次
					// 添加到其它父控件中会出错
					ViewParent parent = mStatusBarView.getParent();
					if (parent != null) {
						ViewGroup viewGroup = (ViewGroup) parent;
						if (viewGroup != null)
							viewGroup.removeView(mStatusBarView);
					}
				}
				ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						getStatusBarHeight(activity));
				root.addView(mStatusBarView, param);
			}
			return;
		}

		// sdk 5.x
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(statusColor);
			return;
		}
	}
	//=============沉侵式==(end)=================
}
