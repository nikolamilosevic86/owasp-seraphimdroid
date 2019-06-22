package org.owasp.seraphimdroid.services;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.helper.DatabaseHelper;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class CheckAppLaunchThread extends Thread {

	private Context context;
	private Handler handler;
	private ActivityManager actMan;
	private int timer = 100;
	public static final String TAG = "App Thread";
	public static String lastUnlocked;
	
	// private String lastUnlocked;

	public CheckAppLaunchThread(Handler mainHandler, Context context) {
		this.context = context;
		this.handler = mainHandler;
		actMan = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		this.setPriority(MAX_PRIORITY);
	}

	@Override
	public void run() {
		context.startService(new Intent(context, AppLockService.class));
		Looper.prepare();
		String prevTasks;
		String recentTasks = "";
		
		prevTasks = recentTasks;
		Log.d("Thread", "Inside Thread");
		while (true) {
			try {
				String topPackageName = "";
				if(Build.VERSION.SDK_INT >= 21) {
				    UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");                       
				    long time = System.currentTimeMillis(); 
				    // We get usage stats for the last 10 seconds
				    List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*5, time);                                    
				    if(stats != null) {
				        SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
				        for (UsageStats usageStats : stats) {
				            mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
				        }                    
				        if(mySortedMap != null && !mySortedMap.isEmpty()) {
				            topPackageName =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();                                   
				        }                                       
				    }
				}
				else {
					topPackageName = actMan.getRunningAppProcesses().get(0).processName;
				}
				recentTasks = topPackageName;
				Thread.sleep(timer);
				if (recentTasks.length()==0 || recentTasks.equals(
						prevTasks)) {
				} else {
					if (isAppLocked(recentTasks)) {
						Log.d(TAG, "Locked " + recentTasks);
						handler.post(new RequestPassword(context, recentTasks));
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			prevTasks = recentTasks;
			
		}

	}

	class ToastRunnable implements Runnable {

		String message;

		public ToastRunnable(String text) {
			message = text;
		}

		@Override
		public void run() {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

		}
	}

	class RequestPassword implements Runnable {

		private Context mContext;
		private String pkgName;

		public RequestPassword(Context mContext, String pkgName) {
			this.mContext = mContext;
			this.pkgName = pkgName;
		}

		@Override
		public void run() {

			Intent passwordAct = new Intent(context, PasswordActivity.class);
			passwordAct.putExtra("PACKAGE_NAME", pkgName);
			passwordAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			this.mContext.startActivity(passwordAct);

		}

	}

	private boolean isAppLocked(String packageName) {
		if (packageName.equals(PasswordActivity.lastUnlocked)) {
			return false;
		}
		PasswordActivity.lastUnlocked = null;
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM locks WHERE package_name=\'"
				+ packageName + "\'", null);
		boolean isLocked = false;
		if (cursor.moveToNext()) {
			isLocked = true;
		}
		
		cursor.close();
		db.close();
		dbHelper.close();
		return isLocked;
	}

}
 