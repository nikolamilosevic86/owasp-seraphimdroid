package org.owasp.seraphimdroid.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import org.owasp.seraphimdroid.LogDetailActivity;
import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.services.GPSTracker;

import java.util.ArrayList;
import java.util.List;

public class SMSRecepter extends BroadcastReceiver {

	public static final String TAG = "SMSRecepter";

	private GPSTracker gpsTracker;
	public static boolean isRunning = false;
	private SharedPreferences defaultPrefs;
	private DevicePolicyManager dpm;
	// private ComponentName component;

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
		// component = new ComponentName(context,
		// GeoFencingAdminReceiver.class);

		// Get the message received.
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String message = "";
		String receiverNumber = null;
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				message += msgs[i].getMessageBody().toString();
				if (receiverNumber == null)
					receiverNumber = msgs[i].getOriginatingAddress();
			}
		}
		// Check for potential harmful numbers.
		List<String> harmfulNumbers = containsNumber(message);
		Log.d(TAG, "" + harmfulNumbers.size());
		String reason = "Contains potentially harmful number";
		boolean isMalicious = false;
		if (harmfulNumbers != null && !harmfulNumbers.isEmpty()) {
			for (String harmfulNumber : harmfulNumbers) {

				if (!CallRecepter.contactExists(context, harmfulNumber)) {
					isMalicious = true;
					reason += " " + harmfulNumber;
				}
			}
			if (isMalicious) {
				DatabaseHelper dbHelper = new DatabaseHelper(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues cv = new ContentValues();

				cv.put("phone_number", receiverNumber);
				cv.put("reason", reason);
				cv.put("time", CallRecepter.getTime());
				cv.put("content", message);
				cv.put("type", LogDetailActivity.SMS_IN);
				db.insert(DatabaseHelper.TABLE_SMS_LOGS, null, cv);
				db.close();
				dbHelper.close();

				// Putting a notification
				Intent smsLogIntent = new Intent(context, MainActivity.class);
				smsLogIntent.putExtra("FRAGMENT_NO", 2);
				smsLogIntent.putExtra("TAB_NO", 1);
				PendingIntent pSmsLogIntent = PendingIntent.getActivity(
						context, 3, smsLogIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				Notification smsNoti = new NotificationCompat.Builder(context)
						.setContentIntent(pSmsLogIntent)
						.setContentTitle("Potential Malicious SMS")
						.setAutoCancel(true)
						.setSmallIcon(R.drawable.ic_launcher).build();
				((NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE))
						.notify(3, smsNoti);

			}

		}

		// Check for remote org.owasp.seraphimdroid.services.
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
						Intent intnt = new Intent(context, MainActivity.class);
						PendingIntent sentIntent = PendingIntent.getActivity(
								context, 0, intnt, 0);
						SmsManager sms = SmsManager.getDefault();
						sms.sendTextMessage(phoneNumber, null, locationMessage,
								sentIntent, null);
						gpsTracker.stopUsingGPS();
					}
				}

			}
		}
	}

	private List<String> containsNumber(String message) {
		int count = 0;
		List<String> numbers = new ArrayList<String>(2);
		StringBuffer noSpaceMessage = new StringBuffer();
		for (char c : message.toCharArray()) {
			if (c == ' ' || c == '.' || c == '-')
				continue;
			noSpaceMessage.append(c);
		}
		// int i = 0;
		StringBuffer number = new StringBuffer();
		boolean add = false;
		for (int i = 0; i < noSpaceMessage.length(); i++) {
			char c = noSpaceMessage.charAt(i);
			if (c <= '9' && c >= '0') {
				number.append(c);
				count++;
				add = false;
				if (i == noSpaceMessage.length() - 1)
					add = true;
			} else {
				add = true;
			}

			if (add) {
				if (count >= 5) {
					numbers.add(number.toString());
				}
				number.delete(0, number.length());
				count = 0;
			}
		}
		// if (count >= 5)
		// return number.toString();
		return numbers;
	}

}
