package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.WifiStateReceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class ServicesLockService extends Service{

	static BroadcastReceiver wifiReceiver = null;
	BroadcastReceiver dataReceiver;
	BroadcastReceiver bluetoothReceiver;
	static IntentFilter filter;
	static Context context;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		context = getBaseContext();
		//Init Wifi listener
		Cursor cursor = db.rawQuery(
				"SELECT * FROM services WHERE service_name=\'" + labels[0]
						+ "\'", null);
		if(cursor.moveToNext()) {
			registerWifi();
		}
		return START_STICKY;
	}
	
	public static void registerWifi() {
		wifiReceiver = new WifiStateReceiver();
        filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED_ACTION");
		context.registerReceiver(wifiReceiver, filter);
		WifiStateReceiver.setStatus(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled());
	}
	
	public static void unregisterWifi() {
		if(wifiReceiver!=null) {
			context.unregisterReceiver(wifiReceiver);
			wifiReceiver = null;
		}
	}
	
	String[] labels = {
			"WiFi",
			"Bluetooth",
			"Mobile Network Data"
	};
	
	@Override
	public void onDestroy() {
		if(wifiReceiver!=null) {
			unregisterReceiver(wifiReceiver);
			wifiReceiver = null;
		}
		super.onDestroy();
	}
	
}
