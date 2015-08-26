package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.BluetoothStateReceiver;
import org.owasp.seraphimdroid.receiver.MobileDataStateReceiver;
import org.owasp.seraphimdroid.receiver.WifiStateReceiver;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import weka.classifiers.functions.IsotonicRegression;

public class ServicesLockService extends Service{

	static BroadcastReceiver wifiReceiver = null;
	static BroadcastReceiver mobileDataReceiver;
	static BroadcastReceiver bluetoothReceiver;
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
			registerWifiListener();
		}
		//Init Bluetooth listener
		cursor = db.rawQuery(
				"SELECT * FROM services WHERE service_name=\'" + labels[1]
						+ "\'", null);
		if(cursor.moveToNext()) {
			registerBluetoothListener();
		}
		//Init Mobile Data listener
		cursor = db.rawQuery(
				"SELECT * FROM services WHERE service_name=\'" + labels[2]
						+ "\'", null);
		if(cursor.moveToNext()) {
			registerMobileDataListener();
		}
		return START_STICKY;
	}
	
	public static void registerWifiListener() {
		wifiReceiver = new WifiStateReceiver();
        filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED_ACTION");
		context.registerReceiver(wifiReceiver, filter);
		WifiStateReceiver.setStatus(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled());
	}
	
	public static void unregisterWifiListener() {
		if(wifiReceiver!=null) {
			context.unregisterReceiver(wifiReceiver);
			wifiReceiver = null;
		}
	}
	
	public static void registerBluetoothListener() {
		bluetoothReceiver = new BluetoothStateReceiver();
		context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		BluetoothStateReceiver.setStatus(BluetoothAdapter.getDefaultAdapter().isEnabled());
	}
	
	public static void unregisterBluetoothListener() {
		if(bluetoothReceiver!=null) {
			context.unregisterReceiver(bluetoothReceiver);
			bluetoothReceiver = null;
		}
	}
	
	public static void registerMobileDataListener() {
		mobileDataReceiver = new MobileDataStateReceiver();
		context.registerReceiver(mobileDataReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		MobileDataStateReceiver.setStatus(isNetworkAvailable());
	}
	
	private static boolean isNetworkAvailable() {
        boolean status = false;
        try {
            final NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((netInfo != null) && (netInfo.getState() == NetworkInfo.State.CONNECTED)) {
                status = true;
            }
        } catch (final Exception e) {
            return false;
        }
        return status;
    }
	
	public static void unregisterMobileDataListener() {
		if(mobileDataReceiver!=null) {
			context.unregisterReceiver(mobileDataReceiver);
			mobileDataReceiver = null;
		}
	}
	
	String[] labels = {
			"WiFi",
			"Bluetooth",
			"Mobile Network Data"
	};
	
	@Override
	public void onDestroy() {
		unregisterWifiListener();
		unregisterBluetoothListener();
		unregisterMobileDataListener();
		super.onDestroy();
	}
	
}
