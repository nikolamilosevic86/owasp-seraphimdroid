package org.owasp.seraphimdroid.services;

import java.util.List;

import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class AppLockService extends Service {

	private Handler handler;
	private Context context;
	private Thread launchChecker;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		handler = new Handler(getMainLooper());
		context = getApplicationContext();
		launchChecker = new CheckAppLauchThread(handler, context);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (!launchChecker.isAlive())
			launchChecker.start();
		return START_STICKY;
	}
}

class CheckAppLauchThread extends Thread {

	private Context context;
	private Handler handler;
	private ActivityManager actMan;
	private int timer = 100;
	private String lastUnlocked;

	public CheckAppLauchThread(Handler mainHandler, Context context) {
		this.context = context;
		this.handler = mainHandler;
		actMan = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		this.setPriority(MAX_PRIORITY);
	}

	@Override
	public void run() {
		Looper.prepare();
		List<RunningTaskInfo> prevTasks;
		List<RunningTaskInfo> recentTasks = actMan.getRunningTasks(1);

		prevTasks = recentTasks;
		Log.d("Thread", "Inside Thread");
		while (true) {
			try {

				recentTasks = actMan.getRunningTasks(1);
				Thread.sleep(timer);
				if (recentTasks.get(0).topActivity.getPackageName().equals(
						prevTasks.get(0).topActivity.getPackageName())) {
					// do nothing
				} else {
					if (isAppLocked(recentTasks.get(0).topActivity
							.getPackageName())) {
						String pkgName = recentTasks.get(0).topActivity
								.getPackageName();
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
		if(PasswordActivity.lastUnlocked != null){
			lastUnlocked = PasswordActivity.lastUnlocked;
		}
		if (packageName.equals(lastUnlocked)) {
			return false;
		}
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
