package org.owasp.seraphimdroid.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

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
		launchChecker = new CheckAppLaunchThread(handler, context);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		while (true) {
			if (!launchChecker.isAlive())
				launchChecker.start();
			return START_STICKY;

		}
	}
}

//class CheckAppLaunchThread extends Thread {
//
//	private Context context;
//	private Handler handler;
//	private ActivityManager actMan;
//	private int timer = 100;
//
//	// private String lastUnlocked;
//
//	public CheckAppLaunchThread(Handler mainHandler, Context context) {
//		this.context = context;
//		this.handler = mainHandler;
//		actMan = (ActivityManager) context
//				.getSystemService(Context.ACTIVITY_SERVICE);
//		this.setPriority(MAX_PRIORITY);
//	}
//
//	@Override
//	public void run() {
//		context.startService(new Intent(context, AppLockService.class));
//		Looper.prepare();
//		List<RunningTaskInfo> prevTasks;
//		List<RunningTaskInfo> recentTasks = actMan.getRunningTasks(1);
//
//		prevTasks = recentTasks;
//		Log.d("Thread", "Inside Thread");
//		while (true) {
//			try {
//
//				recentTasks = actMan.getRunningTasks(1);
//				Thread.sleep(timer);
//				if (recentTasks.get(0).topActivity.getPackageName().equals(
//						prevTasks.get(0).topActivity.getPackageName())) {
//					// do nothing
//				} else {
//					if (isAppLocked(recentTasks.get(0).topActivity
//							.getPackageName())) {
//						String pkgName = recentTasks.get(0).topActivity
//								.getPackageName();
//						// timer = 10000;
//						// Toast.makeText(context, appName,
//						// Toast.LENGTH_SHORT).show();
//						// handler.post(new ToastRunnable(pkgName));
//						handler.post(new RequestPassword(context, pkgName));
//
//					}
//				}
//
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			prevTasks = recentTasks;
//			// Looper.loop();
//		}
//
//	}
//
//	class ToastRunnable implements Runnable {
//
//		String message;
//
//		public ToastRunnable(String text) {
//			message = text;
//		}
//
//		@Override
//		public void run() {
//			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
//
//		}
//	}
//
//	class RequestPassword implements Runnable {
//
//		private Context mContext;
//		private String pkgName;
//
//		public RequestPassword(Context mContext, String pkgName) {
//			this.mContext = mContext;
//			this.pkgName = pkgName;
//		}
//
//		@Override
//		public void run() {
//
//			Intent passwordAct = new Intent(context, PasswordActivity.class);
//			passwordAct.putExtra("PACKAGE_NAME", pkgName);
//			passwordAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			this.mContext.startActivity(passwordAct);
//
//		}
//
//	}
//
//	private boolean isAppLocked(String packageName) {
//		// if(packageName.equals(context.getPackageName())){
//		// return false;
//		// }
//		if (packageName.equals(PasswordActivity.lastUnlocked)) {
//			return false;
//		}
//		// if (PasswordActivity.lastUnlocked != null) {
//		//
//		// if (PasswordActivity.lastUnlocked.equals(packageName)) {
//		// // lastUnlocked = PasswordActivity.lastUnlocked;
//		// PasswordActivity.lastUnlocked = null;
//		// return false;
//		// }
//		// }
//		// if (packageName.equals(lastUnlocked)) {
//		// return false;
//		// }
//		PasswordActivity.lastUnlocked = null;
//		DatabaseHelper dbHelper = new DatabaseHelper(context);
//		SQLiteDatabase db = dbHelper.getReadableDatabase();
//		Cursor cursor = db.rawQuery("SELECT * FROM locks WHERE package_name=\'"
//				+ packageName + "\'", null);
//		boolean isLocked = false;
//		if (cursor.moveToNext()) {
//			isLocked = true;
//		}
//
//		cursor.close();
//		db.close();
//		dbHelper.close();
//		return isLocked;
//	}
//
//}
