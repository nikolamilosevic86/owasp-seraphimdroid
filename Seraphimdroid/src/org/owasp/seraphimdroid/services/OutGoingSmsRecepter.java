package org.owasp.seraphimdroid.services;

import java.util.List;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.CallRecepter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class OutGoingSmsRecepter extends Service {

	private static final String CONTENT_SMS = "content://sms/";
	static String messageId = "";

	private MyContentObserver observer;
	private ContentResolver contentResolver;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Handler handler = new Handler(getMainLooper());
		observer = new MyContentObserver(handler);
		contentResolver = getContentResolver();
		contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS), true,
				observer);
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
			// TODO Auto-generated constructor stub
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

			if (type.equals("2") && !message_id.equals(messageId)) {

				messageId = message_id;
				
				// Gettings current running task
				ActivityManager am = (ActivityManager) getApplicationContext()
						.getSystemService(ACTIVITY_SERVICE);
				final List<RunningTaskInfo> tasks = am
						.getRunningTasks(Integer.MAX_VALUE);

				// Check whether the messenger is in foreground or not.
				if (!tasks.get(0).topActivity.getPackageName().equals(
						"com.android.mms")) {
					PackageManager pm = getPackageManager();
					ApplicationInfo appInfo = null;
					String appName = "";
					try {
						appInfo = pm.getApplicationInfo(
								tasks.get(0).topActivity.getPackageName(),
								PackageManager.GET_META_DATA);
						appName = (String) appInfo.loadLabel(pm);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
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
