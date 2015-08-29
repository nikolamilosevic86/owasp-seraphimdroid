package org.owasp.seraphimdroid.receiver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MobileDataStateReceiver extends BroadcastReceiver {

	static Boolean wasOn = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(
				"SELECT * FROM services WHERE service_name=\'Mobile Network Data"
						+ "\'", null);
		if(!cursor.moveToNext()) {
			return;
		}
		NetworkInfo networkInfo =  intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		if (networkInfo.getType()==ConnectivityManager.TYPE_MOBILE && !networkInfo.isConnected() && wasOn==true) {
			try {
				changeMobileDataStatus(context, true);
			} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException
					| NoSuchMethodException | InvocationTargetException e) {
				e.printStackTrace();
			}
			showLock(context, "mobile", 1);
		}
		else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected() && wasOn==false) {
			try {
				changeMobileDataStatus(context, false);
			} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException
					| NoSuchMethodException | InvocationTargetException e) {
				e.printStackTrace();
			}
			showLock(context, "mobile", 0);
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
	
	public static void setStatus(Boolean status) {
		wasOn = status;
	}
	
	public static void changeMobileDataStatus(Context context, Boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		ConnectivityManager manager = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    Class managerClass = Class.forName(manager.getClass().getName());
	    Field connectivityManagerField = managerClass.getDeclaredField("mService");
	    connectivityManagerField.setAccessible(true);
	    Object connectivityManager = connectivityManagerField.get(manager);
	    final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
	    Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    setMobileDataEnabledMethod.setAccessible(true);

	    setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
	}
	
}
