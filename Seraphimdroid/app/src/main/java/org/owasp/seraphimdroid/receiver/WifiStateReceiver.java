package org.owasp.seraphimdroid.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;

import java.util.BitSet;

public class WifiStateReceiver extends BroadcastReceiver {
	
	WifiManager wifiManager;
	static Boolean wasOn = false;
	
	public WifiStateReceiver() {}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Thread.sleep(10000);
		}catch (Exception ex)
		{
			ex.printStackTrace();
		}
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

		if(cm != null) {

			NetworkInfo info = cm.getActiveNetworkInfo();
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();

			for(int i=0;i<netInfo.length;i++)
			{
				if(netInfo[i].isConnected())
				{
					info = netInfo[i];
				}
			}


			if (info != null) {
				if (info.isConnected()) {
					// Do your work.

					// e.g. To check the Network Name or other info:
					WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					String ssid = wifiInfo.getSSID();
					WifiConfiguration activeConfig = null;
					for (WifiConfiguration conn : wifiManager.getConfiguredNetworks()) {
						if (conn.status == WifiConfiguration.Status.CURRENT) {
							activeConfig = conn;
							break;
						}
					}

					boolean zeroProtocol = activeConfig.allowedProtocols.get(0);
					boolean oneProtocol = activeConfig.allowedProtocols.get(1);
					if (zeroProtocol) {
						Log.w("Seraphimdroid", "Network is WPA");
						Toast.makeText(context, "Network is WPA", Toast.LENGTH_LONG);
						System.out.print("Network is WPA");
						Intent smsLogIntent = new Intent(context, MainActivity.class);
						smsLogIntent.putExtra("FRAGMENT_NO", 2);
						smsLogIntent.putExtra("TAB_NO", 1);
						PendingIntent pSmsLogIntent = PendingIntent.getActivity(
								context, 3, smsLogIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);
						Notification smsNoti = new NotificationCompat.Builder(context)
								.setContentIntent(pSmsLogIntent)
								.setContentTitle("Unsecure Wifi network")
								.setAutoCancel(true)
								.setSmallIcon(R.drawable.ic_launcher).build();
						((NotificationManager) context
								.getSystemService(Context.NOTIFICATION_SERVICE))
								.notify(6, smsNoti);

					}
					if (oneProtocol) {
						Log.w("Seraphimdroid", "Network is RSN");
						Toast.makeText(context, "Network is RSN", Toast.LENGTH_LONG);
						System.out.print("Network is RSN");

//						Intent smsLogIntent = new Intent(context, MainActivity.class);
//						smsLogIntent.putExtra("FRAGMENT_NO", 2);
//						smsLogIntent.putExtra("TAB_NO", 1);
//						PendingIntent pSmsLogIntent = PendingIntent.getActivity(
//								context, 3, smsLogIntent,
//								PendingIntent.FLAG_UPDATE_CURRENT);
//						Notification smsNoti = new NotificationCompat.Builder(context)
//								.setContentIntent(pSmsLogIntent)
//								.setContentTitle("Secure Wifi network")
//								.setAutoCancel(true)
//								.setSmallIcon(R.drawable.ic_launcher).build();
//						((NotificationManager) context
//								.getSystemService(Context.NOTIFICATION_SERVICE))
//								.notify(6, smsNoti);

					}
				}
			}
		}


		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(
				"SELECT * FROM services WHERE service_name=\'WiFi"
						+ "\'", null);
		if (!cursor.moveToNext()) {
			return;
		}
		int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
		switch (extraWifiState) {
			case android.net.wifi.WifiManager.WIFI_STATE_ENABLED:
				if (wasOn == false) {
					wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(false);
					showLock(context, "wifi", 0);
				}
				break;
			case android.net.wifi.WifiManager.WIFI_STATE_DISABLED:
				if (wasOn == true) {
					wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(true);
					showLock(context, "wifi", 1);
				}
				break;
			default:
				break;
		}


	}
	private void showLock(final Context context, final String service, final int state) {
		Intent passwordAct = new Intent(context, PasswordActivity.class);
		passwordAct.putExtra("PACKAGE_NAME", "");
		passwordAct.putExtra("service", service);
		passwordAct.putExtra("state", state + "");
		passwordAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(passwordAct);
	}

	public static int bitSetToInt(BitSet bitSet)
	{
		int bitInteger = 0;
		for(int i = 0 ; i < 32; i++)
			if(bitSet.get(i))
				bitInteger |= (1 << i);
		return bitInteger;
	}
	
	public static void setStatus(Boolean status) {
		wasOn = status;
	}


	
}
