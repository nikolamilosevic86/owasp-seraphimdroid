package org.owasp.seraphimdroid.receiver;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.services.GPSTracker;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SMSRecepter extends BroadcastReceiver {

	private GPSTracker gpsTracker;
	public static boolean isRunning = false;
	private SharedPreferences defaultPrefs;
	private DevicePolicyManager dpm;
	private ComponentName component;

	private Location currentLocation = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		// Getting settings.
		String phoneNumber = defaultPrefs.getString(
				"geo_location_number_primary", "0");
		String secretCode = defaultPrefs.getString("remote_secret_code", "0");

		boolean remoteLock, remoteWipe, remoteLocation;
		remoteLock = defaultPrefs.getBoolean("remote_lock", false);
		remoteWipe = defaultPrefs.getBoolean("remote_wipe", false);
		remoteLocation = defaultPrefs.getBoolean("remote_location", false);

		dpm = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		component = new ComponentName(context, GeoFencingAdminReceiver.class);

		// Get the message received.
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String message = "";
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				message += msgs[i].getMessageBody().toString();
			}
		}
		// Check for potential harmful numbers.
		String harmfulNumber = containsNumber(message);
		if (harmfulNumber != null) {
			if (!CallRecepter.contactExists(context, harmfulNumber)) {
				DatabaseHelper dbHelper = new DatabaseHelper(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues cv = new ContentValues();
				String receiverNumber = intent
						.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				cv.put("phone_numer", receiverNumber);
				cv.put("reason", "Contains potentially harmful number "
						+ harmfulNumber);
				cv.put("time", CallRecepter.getTime());
				db.insert(DatabaseHelper.TABLE_SMS_LOGS, null, cv);
				db.close();
				dbHelper.close();
			}

		}

		// Check for remote services.
		if (secretCode != null && !secretCode.equals("0")) {
			if (message.contains(secretCode)) {
				if (remoteLock) {
					dpm.lockNow();
				}
				if (remoteWipe) {
					dpm.wipeData(0);
				}

				if (remoteLocation) {
					gpsTracker = new GPSTracker(context);
					// Send Location
					while (currentLocation == null) {
						currentLocation = gpsTracker.getLocation();
					}
					if (phoneNumber != null && !phoneNumber.equals("0")
							&& currentLocation != null) {
						String locationMessage = "Latitude = "
								+ currentLocation.getLatitude()
								+ "\nLongitude = "
								+ currentLocation.getLongitude();
						// Intent intnt = new Intent(context,
						// MainActivity.class);
						PendingIntent sentIntent = PendingIntent.getActivity(
								context, 0, null, 0);
						SmsManager sms = SmsManager.getDefault();
						sms.sendTextMessage(phoneNumber, null, locationMessage,
								sentIntent, null);
					}
				}

			}
		}
	}

	private String containsNumber(String message) {
		int count = 0;
		StringBuffer number = new StringBuffer();
		StringBuffer noSpaceMessage = new StringBuffer();
		for (char c : message.toCharArray()) {
			if (c != ' ' || c != '.' || c != ',' || c != '-')
				noSpaceMessage.append(c);
		}
		for (char c : noSpaceMessage.toString().toCharArray()) {
			if (c <= '9' && c >= '0') {
				number.append(c);
				count++;
			} else if (count < 5) {
				number.delete(0, number.length());
				count = 0;
			}
		}
		if (count >= 5)
			return number.toString();
		return null;
	}

}
