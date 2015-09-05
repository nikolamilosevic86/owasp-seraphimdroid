package org.owasp.seraphimdroid.receiver;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SettingsCheckAlarmReceiver extends BroadcastReceiver{

	String TAG = "SettingsCheckReceiver";
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		runCheck(context);
	}
	
	private void runCheck(Context context) {
		Log.d(TAG, "Scanned");
		Boolean isSafe = true;
		//Check for USB DEbugging
		if(Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
			isSafe = false;
		}
		//Check for Unknown Sources
		if(Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1) {
			isSafe = false;
		}
		//Fire notification
		if(isSafe==false) {
			fireNotification(context);
		}
	}
	
	private void fireNotification(Context context) {
	    Intent notificationIntent = new Intent(context, MainActivity.class);
	    //Open Settings Check Fragment
	    notificationIntent.putExtra("FRAGMENT_NO", 1);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
	            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

	    PendingIntent intent = PendingIntent.getActivity(context, 1,
	            notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
	    Notification notification;
	    notification= new NotificationCompat.Builder(context)
                .setContentTitle("Settings Check")
                .setContentText(
                        "Vulnerable Device Settings Detected. Click to Fix")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent).setWhen(0).setAutoCancel(true)
                .build();
		// Display Notification
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(1, notification);
	}
	
	
}
