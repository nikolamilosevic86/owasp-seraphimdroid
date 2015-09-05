package org.owasp.seraphimdroid.services;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.owasp.seraphimdroid.LogDetailActivity;
import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.CallRecepter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class OutGoingSmsRecepter extends Service {

	private String TAG = "SMSRecepter";
	private static final String CONTENT_SMS = "content://sms/";
	static String messageId = "";

	private MyContentObserver observer;
	private ContentResolver contentResolver;

	private static List<String> SMSAppWhiteList = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Handler handler = new Handler(getMainLooper());
		observer = new MyContentObserver(handler);
		contentResolver = getContentResolver();
		contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS), true,
				observer);
		if (SMSAppWhiteList == null)
			SMSAppWhiteList = new ArrayList<String>();
		SMSAppWhiteList.clear();
		SMSAppWhiteList.add("com.android.mms");
		SMSAppWhiteList.add("com.google.android.talk");

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		contentResolver.unregisterContentObserver(observer);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	private class MyContentObserver extends ContentObserver {

		public MyContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Uri uriSmsUri = Uri.parse(CONTENT_SMS);
			Cursor cursor = getApplicationContext().getContentResolver().query(
					uriSmsUri, null, null, null, null);
			cursor.moveToFirst();

			String type = cursor.getString(cursor.getColumnIndex("type"));
			String message_id = cursor.getString(cursor.getColumnIndex("_id"));
			String content = cursor.getString(cursor.getColumnIndex("body"));

			if (type.equals("2") && !message_id.equals(messageId)) {

				messageId = message_id;

				// Gettings current running task
				String topPackageName = "";
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { 
				    UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");                       
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
					ActivityManager am = (ActivityManager) getApplicationContext()
							.getSystemService(ACTIVITY_SERVICE);
					topPackageName = am.getRunningAppProcesses().get(0).processName;
				}
				

				// Check whether the messenger is in foreground or not.
				if (!SMSAppWhiteList.contains(topPackageName)) {

					Log.d(TAG, topPackageName);
					PackageManager pm = getPackageManager();
					ApplicationInfo appInfo = null;
					String appName = "";
					try {
						appInfo = pm.getApplicationInfo(
								topPackageName,
								PackageManager.GET_META_DATA);
						appName = (String) appInfo.loadLabel(pm);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}

					NotificationCompat.Builder builder = new NotificationCompat.Builder(
							getApplicationContext());
					builder.setAutoCancel(true)
							.setSmallIcon(R.drawable.ic_launcher)
							.setContentTitle("Suspicious SMS");
					if (appName.equals(""))
						appName = "unknown application";
					builder.setContentText("A suspicious SMS has been sent by "
							+ appName);

					DatabaseHelper dbHelper = new DatabaseHelper(
							getApplicationContext());
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					ContentValues cv = new ContentValues();
					cv.put("phone_number",
							cursor.getString(cursor.getColumnIndex("address")));
					cv.put("reason", "SMS sent by " + appName
							+ " without users notices or permission");
					cv.put("time", CallRecepter.getTime());
					cv.put("type", LogDetailActivity.SMS_OUT);
					cv.put("content", content);
					db.insert(DatabaseHelper.TABLE_SMS_LOGS, null, cv);
					db.close();
					dbHelper.close();

					Intent logIntent = new Intent(OutGoingSmsRecepter.this,
							MainActivity.class);
					logIntent.putExtra("FRAGMENT_NO", 1);
					logIntent.putExtra("TAB_NO", 1);

					PendingIntent pLogIntent = PendingIntent.getActivity(
							getApplicationContext(), 0, logIntent,
							PendingIntent.FLAG_CANCEL_CURRENT);
					builder.setContentIntent(pLogIntent);

					// Display Notification
					((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
							.notify(0, builder.build());

				}

			}
		}
	}

}
