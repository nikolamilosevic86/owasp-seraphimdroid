package org.owasp.seraphimdroid.services;

import java.util.List;

import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CheckAppLaunchThread extends Thread {

	private Context context;
	private Handler handler;
	private ActivityManager actMan;
	private int timer = 100;
	public static final String TAG = "App Thread";
	
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
		List<RunningAppProcessInfo> prevTasks;
		List<RunningAppProcessInfo> recentTasks = actMan.getRunningAppProcesses();
		
		prevTasks = recentTasks;
		Log.d("Thread", "Inside Thread");
		while (true) {
			try {

				recentTasks = actMan.getRunningAppProcesses();
				Thread.sleep(timer);
				if (recentTasks.get(0).processName.equals(
						prevTasks.get(0).processName)) {
					Log.d(TAG, "Do nothing " + recentTasks.get(0).processName);
				} else {
					if (isAppLocked(recentTasks.get(0).processName)) {
						Log.d(TAG, "Locked " + recentTasks.get(0).processName);
						String pkgName = recentTasks.get(0).processName;
						// timer = 10000;
						// Toast.makeText(context, appName,
						// Toast.LENGTH_SHORT).show();
						// handler.post(new ToastRunnable(pkgName));
						handler.post(new RequestPassword(context, pkgName));

					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			prevTasks = recentTasks;
			// Looper.loop();
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
			passwordAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.mContext.startActivity(passwordAct);

		}

	}

	private boolean isAppLocked(String packageName) {
		// if(packageName.equals(context.getPackageName())){
		// return false;
		// }
		if (packageName.equals(PasswordActivity.lastUnlocked)) {
			return false;
		}
//		if(packageName.equals(context.getPackageName()))
//			return true;
		// if (PasswordActivity.lastUnlocked != null) {
		//
		// if (PasswordActivity.lastUnlocked.equals(packageName)) {
		// // lastUnlocked = PasswordActivity.lastUnlocked;
		// PasswordActivity.lastUnlocked = null;
		// return false;
		// }
		// }
		// if (packageName.equals(lastUnlocked)) {
		// return false;
		// }
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
 