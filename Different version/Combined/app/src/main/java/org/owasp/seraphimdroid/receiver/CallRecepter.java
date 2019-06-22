package org.owasp.seraphimdroid.receiver;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.PasswordActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.helper.DatabaseHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CallRecepter extends BroadcastReceiver {

	private static DatabaseHelper dbHelper;
	private final String ussdLog = "ussd_logs";
	private final String callLog = "call_logs";

	private Context context;
	private boolean isIncoming, isOutgoing;

	private final String TAG = "CallRecepter";

	@Override
	public void onReceive(Context context, Intent intent) {

		isIncoming = false;
		isOutgoing = false;

		isOutgoing = intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL);

		if (intent.getBooleanExtra("CALL_ALLOWED", false)) {
			Log.d(TAG, "true");
		} else
			Log.d(TAG, "false");

		if (isOutgoing) {
			if (MainActivity.shouldReceive)
				Log.d(TAG, "true");
			else {
				Log.d(TAG, "false");
				MainActivity.shouldReceive = true;
				return;
			}
		}

		final Bundle extras = intent.getExtras();
		if (extras != null && !isOutgoing) {
			isIncoming = extras.getString(TelephonyManager.EXTRA_STATE).equals(
					TelephonyManager.EXTRA_STATE_RINGING);
		}

		// Initializing required objects.
		dbHelper = new DatabaseHelper(context);
		this.context = context;

		String phoneNumber = "";
		if (isOutgoing)
			phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		else if (isIncoming)
			phoneNumber = extras
					.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

		// Check if the call is made to USSD
		if (phoneNumber.startsWith("*") || phoneNumber.endsWith("#")) {

			// Check if the code is harmful
			if (isUnsafeUssd(phoneNumber)) {
				// disable the code
				setResult(Activity.RESULT_CANCELED, phoneNumber, null);
				dropCall();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues cv = new ContentValues();
				cv.put("phone_number", phoneNumber);
				cv.put("time", getTime());
				cv.put("reason", getReason(phoneNumber));

				db.insert(ussdLog, null, cv);

				db.close();

				Toast.makeText(context, getReason(phoneNumber),
						Toast.LENGTH_LONG).show();
			} else {
				// Allow the code to run
				setResult(Activity.RESULT_OK, phoneNumber, null);
			}
		}
		// Check if the call is blocked.
		else if (!phoneNumber.equals("") && isCallBlocked(phoneNumber)) {

			// Block the call
			dropCall();
			showNotification(phoneNumber);
			setResult(Activity.RESULT_CANCELED, null, null);

			ContentValues cv = new ContentValues();
			cv.put("phone_number", phoneNumber);
			cv.put("time", getTime());
			cv.put("reason", getReason(phoneNumber));

			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.insert(callLog, null, cv);
			db.close();

		}

	}

	// private class WriteUSSDLog extends AsyncTask<String, Void, Void> {
	//
	// @Override
	// protected Void doInBackground(String... params) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// }

	public static String getTime() {
		String time = "";

		Calendar current = Calendar.getInstance();
		time = current.get(Calendar.YEAR) + "-"
				+ (current.get(Calendar.MONTH) + 1) + "-"
				+ current.get(Calendar.DAY_OF_MONTH) + " "
				+ current.get(Calendar.HOUR_OF_DAY) + ":"
				+ current.get(Calendar.MINUTE) + ":"
				+ current.get(Calendar.SECOND);

		return time;
	}

	private String getReason(String number) {
		String reason = null;

		SharedPreferences defaultPref = PreferenceManager
				.getDefaultSharedPreferences(context);

		int blockedCall = Integer.valueOf(defaultPref.getString(
				"blocked_calls", "0"));

		switch (blockedCall) {
		case 8:
			reason = "User Blocked all outgoing calls";
			return reason;
		case 9:
			reason = "User Blocked all incoming calls";
			return reason;
		case 10:
			reason = "User Blocked the number";
			return reason;
		default:
			break;
		}
		
		if (harmfulCodes.contains(number)) {
			reason = "Potential Factory Reset";
		} else if (isNumberBlacklisted(number)) {
			reason = "Number is blacklisted by user";
			if (isIncoming)
				reason = reason + ", Incoming";
			if (isOutgoing)
				reason = reason + ", Outgoing";
		} else if (!contactExists(context, number)) {
			reason = "Contact not found in user's contacts list";
		}
		return reason;
	}

	private boolean isCallBlocked(String phoneNumber) {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String blockedCalls = sharedPreferences.getString("blocked_calls", "0");

		int blockedCallsValue = Integer.valueOf(blockedCalls);
		switch (blockedCallsValue) {
		case 0:
			return false;
		case 1:
			if (!contactExists(context, phoneNumber) && isOutgoing) {
				return true;
			}
			return false;
		case 2:
			// Outgoing
			if (isNumberBlacklisted(phoneNumber) && isOutgoing)
				return true;
			return false;
		case 3:
			// Incoming
			if (isNumberBlacklisted(phoneNumber) && isIncoming)
				return true;
			return false;
		case 4:
			// Outgoing
			if (!contactExists(context, phoneNumber)
					|| (isNumberBlacklisted(phoneNumber) && isOutgoing)) {
				return true;
			}
			return false;
		case 5:
			// Incoming
			if (!contactExists(context, phoneNumber)
					|| (isNumberBlacklisted(phoneNumber) && isIncoming)) {
				return true;
			}
			return false;
		case 6:
			// Incoming and Outgoing Blacklist
			if (isNumberBlacklisted(phoneNumber))
				return true;
			return false;
		case 7:
			// Block unsaved and Both blacklist numbers
			if (!contactExists(context, phoneNumber)
					|| isNumberBlacklisted(phoneNumber)) {
				return true;
			}
			return false;
		case 8:
			// Block all outgoing calls
			return isOutgoing;
		case 9:
			// Block all Incoming calls
			return isIncoming;
		case 10:
			// Block all calls
			return true;
		default:
			break;
		}

		return false;
	}

	// Check if the phoneNumber exists in contact
	public static boolean contactExists(Context context, String number) {
		// / number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,
				PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri,
				mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				return true;
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		return false;
	}

	public boolean isNumberBlacklisted(String number) {
		if (number.charAt(0) == '0') {
			number = number.substring(1, number.length());
		}
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.TABLE_BLACKLIST, null);
		while (cursor.moveToNext()) {
			String blackNumber = cursor.getString(1);
			if (number.contains(blackNumber))
				return true;
			if (blackNumber.contains(number))
				return true;
		}
		return false;
	}

	private static List<String> harmfulCodes = new ArrayList<String>();

	public static boolean isUnsafeUssd(String number) {
		harmfulCodes = new ArrayList<String>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql = "SELECT * from " + DatabaseHelper.TABLE_BLOCKED_USSD
				+ " ORDER BY _id";
		Cursor cursor = db.rawQuery(sql, null);
	
        if (cursor.moveToFirst()) {
            do {
                harmfulCodes.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
		return harmfulCodes.contains(number) ? true : false;
	}

	private void dropCall() {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			Class<?> classTelephony;
			classTelephony = Class.forName(telephonyManager.getClass()
					.getName());
			Method methodGetITelephony = classTelephony
					.getDeclaredMethod("getITelephony");

			methodGetITelephony.setAccessible(true);

			Object telephonyInterface = methodGetITelephony
					.invoke(telephonyManager);

			Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface
					.getClass().getName());
			Method methodEndCall = telephonyInterfaceClass
					.getDeclaredMethod("endCall");

			methodEndCall.invoke(telephonyInterface);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void showNotification(String number) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean notify = sharedPreferences.getBoolean(
				"call_blocked_notification", false);
		if (notify) {
			Intent logIntent = new Intent(context, MainActivity.class);
			logIntent.putExtra("FRAGMENT_NO", 1);
			logIntent.putExtra("TAB_NO", 0);

			Intent callIntent = new Intent(context, PasswordActivity.class);
			callIntent.putExtra("PACKAGE_NAME", context.getPackageName());
			callIntent.putExtra("PHONE_NUMBER", number);
			callIntent.putExtra("MAKE_CALL", true);

			PendingIntent pLogIntent = PendingIntent.getActivity(context, 0,
					logIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent pCallIntent = PendingIntent.getActivity(context, 0,
					callIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context).setContentTitle("Call blocked")
					.setContentText(getReason(number)).setAutoCancel(true)
					.setContentIntent(pLogIntent)
					.setVibrate(new long[]{300, 500, 300})
					.setLights(Color.RED, 2000, 3000)
					.setSmallIcon(R.drawable.ic_launcher)
					.addAction(R.drawable.ic_launcher, "redial", pCallIntent);

			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(0, builder.build());
		}

	}
}
