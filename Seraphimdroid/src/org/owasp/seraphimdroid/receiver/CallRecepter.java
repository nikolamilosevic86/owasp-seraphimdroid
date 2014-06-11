package org.owasp.seraphimdroid.receiver;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.Toast;

public class CallRecepter extends BroadcastReceiver {

	private DatabaseHelper dbHelper;
	private final String ussdLog = "ussd_logs";
	private final String callLog = "call_logs";

	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {

		// Initializing required objects.
		dbHelper = new DatabaseHelper(context);
		this.context = context;

		String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

		// Check if the call is made to USSD
		if (phoneNumber.startsWith("*") || phoneNumber.endsWith("#")) {

			// Check if the code is harmful
			if (isUnsafeUssd(phoneNumber)) {
				// disable the code
				setResult(Activity.RESULT_CANCELED, null, null);

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				ContentValues cv = new ContentValues();
				cv.put("phone_number", phoneNumber);
				cv.put("time", getTime());
				cv.put("reason", getReason(phoneNumber));

				db.insert(ussdLog, null, cv);

				db.close();

				Toast.makeText(context, phoneNumber, Toast.LENGTH_LONG).show();
			} else {
				// Allow the code to run
				setResult(Activity.RESULT_OK, phoneNumber, null);
			}
		}
		// Check if the call is blocked.
		else if (isCallBlocked(phoneNumber)) {

			// Block the call
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

	private String getTime() {
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

		if (harmfulCodes.contains(number)) {
			reason = "Potential Factory Reset";
		} else  if (!contactExists(context, number)) {
			reason = "Contact not found in user's contacts list";
		}
		return reason;
	}

	private boolean isCallBlocked(String phoneNumber) {

		if (contactExists(context, phoneNumber)) {
			return false;
		}
		return true;
	}

	// Check if the phoneNumber exists in contact
	public boolean contactExists(Context context, String number) {
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

	private static List<String> harmfulCodes = Arrays.asList("*#7780#",
			"*#7780%23",// Factory Reset
			"*2767*3855#", "*2767*3855%23", // Full Factory Reset
			"*#*#7780#*#*", // Factory data reset
			"*1198#");

	public static boolean isUnsafeUssd(String number) {
		return harmfulCodes.contains(number) ? true : false;
	}

}
