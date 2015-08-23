package org.owasp.seraphimdroid.receiver;

import org.owasp.seraphimdroid.services.SIMCheckService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(arg1.getAction())) {
			//Alarm Manager for Settings Check
			alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, SettingsCheckAlarmReceiver.class);
			alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			SharedPreferences defaults = PreferenceManager
					.getDefaultSharedPreferences(context);
			
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			//Run at Midnight
			calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
			calendar.set(java.util.Calendar.MINUTE, 0);

			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
			        defaults.getInt("settings_interval", 24*60*60*1000), alarmIntent);
			
			//SIM Card Service
			context.startService(new Intent(context, SIMCheckService.class));
        }
	}

}
