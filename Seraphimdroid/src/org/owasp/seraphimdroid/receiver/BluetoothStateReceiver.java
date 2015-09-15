package org.owasp.seraphimdroid.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.database.DatabaseHelper;

public class BluetoothStateReceiver extends BroadcastReceiver {

	static Boolean wasOn = false;
	
	public BluetoothStateReceiver() {}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(
				"SELECT * FROM services WHERE service_name=\'Bluetooth"
						+ "\'", null);
		if(!cursor.moveToNext()) {
			return;
		}
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF && wasOn==true) {
            	BluetoothAdapter.getDefaultAdapter().enable();
            	showLock(context, "bluetooth", 1);
            }
            else if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON && wasOn==false){
            	BluetoothAdapter.getDefaultAdapter().disable();
            	showLock(context, "bluetooth", 0);
            }
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
}
