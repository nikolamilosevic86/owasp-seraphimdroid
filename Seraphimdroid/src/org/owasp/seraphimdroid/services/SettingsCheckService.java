package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SettingsCheckService extends Service {

	private Handler handler;
	private Runnable runnable;
	//Interval at which service checks
	private int interval;
	String TAG = "SettingsCheckService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Started");
		interval = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()).getInt("interval", 24*60*60*1000);
		handler = new Handler();
		runnable = new Runnable() {
			
			@Override
			public void run() {
				runCheck();
				handler.postDelayed(runnable, interval);
			}
		};
		handler.post(runnable);
		return START_STICKY;
	}

	private void runCheck() {
		Log.d(TAG, "Scanned");
		Boolean isSafe = true;
		//Check for USB DEbugging
		if(Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
			isSafe = false;
		}
		//Check for Unknown Sources
		if(Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1) {
			isSafe = false;
		}
		//Fire notification
		if(isSafe==false) {
			fireNotification();
		}
	}
	
	private void fireNotification() {
	    Intent notificationIntent = new Intent(getBaseContext(), MainActivity.class);
	    //Open Settings Check Fragment
	    notificationIntent.putExtra("FRAGMENT_NO", 1);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

	    PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
	            notificationIntent, 0);
	    Notification notification;
	    notification= new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Settings Check")
                .setContentText(
                        "Vulnerable Device Settings Detected. Click to Fix").setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent).setWhen(0).setAutoCancel(true)
                .build();
		// Display Notification
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.notify(0, notification);
	}
	
}
