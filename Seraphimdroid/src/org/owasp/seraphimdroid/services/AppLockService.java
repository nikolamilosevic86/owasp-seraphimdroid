package org.owasp.seraphimdroid.services;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class AppLockService extends Service {

	private Handler handler;
	private Context context;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		handler = new Handler(getMainLooper());
		context = getApplicationContext();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Thread launchChecker = new CheckAppLauchThread(handler, context);
		launchChecker.start();
		return START_STICKY;
	}

}

class CheckAppLauchThread extends Thread {

	private Context context;
	private Handler handler;
	private ActivityManager actMan;

	public CheckAppLauchThread(Handler mainHandler, Context context) {
		this.context = context;
		this.handler = mainHandler;
		actMan = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
	}

	@Override
	public void run() {
		Looper.prepare();
		List<RunningTaskInfo> prevTasks;
		List<RunningTaskInfo> recentTasks = actMan.getRunningTasks(1);

		prevTasks = recentTasks;

		while (true) {
			try {
				Log.d("Thread", "Inside Thread");
				recentTasks = actMan.getRunningTasks(1);
				Thread.sleep(1000);
				if (recentTasks.get(0).topActivity.getPackageName().equals(
						prevTasks.get(0).topActivity.getPackageName())) {
					// do nothing
				} else {
					String appName = recentTasks.get(0).topActivity
							.getPackageName();
					Toast.makeText(context, appName, Toast.LENGTH_SHORT).show();
					handler.post(new ToastRunnable(appName));
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			prevTasks = recentTasks;
			Looper.loop();
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

}
