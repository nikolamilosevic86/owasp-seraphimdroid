package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.GeoFencingFragment;
import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.receiver.GeoFencingAdminReceiver;
import org.owasp.seraphimdroid.receiver.SMSRecepter;

import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class GeoFencingService extends Service {

	public static Location center;
	public static long TIME = 1000, // Seconds
			DISTANCE; // meters
	private static int counter = 0;
	private DevicePolicyManager deviceMan;
	private ComponentName deviceAdminComponent;
	private SharedPreferences prefs, defaultPrefs;
	private LocationManager lm;
	private android.location.LocationListener listener;

	private static final int ForeGroundId = 1002;
	private boolean isLocked = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("Service", "ON");

		center = new Location(""); // Doesn't really need a provider.
		double lat = intent.getDoubleExtra("LATITUDE", 0);
		double lon = intent.getDoubleExtra("LONGITUDE", 0);
		DISTANCE = intent.getLongExtra("RADIUS", 200);
		if (lat == 0 && lon == 0) {
			center = null;
		} else {
			center.setLatitude(lat);
			center.setLongitude(lon);
		}
		prefs = this.getSharedPreferences("org.owasp.seraphimdroid",
				Context.MODE_PRIVATE);
		defaultPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		deviceAdminComponent = new ComponentName(this,
				GeoFencingAdminReceiver.class);
		deviceMan = (DevicePolicyManager) this
				.getSystemService(Context.DEVICE_POLICY_SERVICE);

		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		String provider = lm.getBestProvider(new Criteria(), false);
		listener = new FencingListener();
		lm.requestLocationUpdates(provider, TIME, 1, listener);

		Toast.makeText(getApplicationContext(), "Fencing ON",
				Toast.LENGTH_SHORT).show();

		Intent geoFencingIntent = new Intent(this, MainActivity.class);
		geoFencingIntent.putExtra("FRAGMENT_NO", 3);
		PendingIntent pGeoIntent = PendingIntent.getActivity(this, 0,
				geoFencingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		builder.setContentText("GeoFencing On")
				.setContentTitle("Seraphimdroid").setAutoCancel(true)
				.setContentIntent(pGeoIntent)
				.setSmallIcon(R.drawable.ic_launcher);

		startForeground(ForeGroundId, builder.build());

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		deviceMan.setMaximumFailedPasswordsForWipe(deviceAdminComponent, 0);
		// deviceMan.setPasswordMinimumLength(deviceAdminComponent, 0);
		// deviceMan.resetPassword("",
		// DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
		deviceMan.setPasswordQuality(deviceAdminComponent,
				DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
		SMSRecepter.isRunning = false;
		stopService(new Intent(this, SMSRecepter.class));

		Log.d("GeoService", "Fencing removed");
		Toast.makeText(getApplicationContext(), "Fencing removed.",
				Toast.LENGTH_SHORT).show();
		lm.removeUpdates(listener);
		super.onDestroy();

	}

	private class FencingListener implements android.location.LocationListener {
		private int interval;

		public FencingListener() {
			interval = Integer.valueOf(defaultPrefs.getString(
					"geo_send_location_interval", "5"));
		}

		@Override
		public void onLocationChanged(Location location) {

			if (counter < 3) {
				if (center.distanceTo(location) > DISTANCE) {
					counter++;
				} else {
					counter = 0;
				}
			}

			if (counter >= 3) {
				if (!isLocked) {
					if (prefs.getBoolean(GeoFencingFragment.lockKey, false)) {
						deviceMan.lockNow();
					}
					if (prefs.getBoolean(GeoFencingFragment.wipeKey, false)) {
						int tries = Integer.valueOf(defaultPrefs.getString(
								"geo_password_tries", "5"));
						deviceMan.lockNow();
						deviceMan.setMaximumFailedPasswordsForWipe(
								deviceAdminComponent, tries);
					}
					if (prefs.getBoolean(GeoFencingFragment.sirenKey, false)) {
						// Start Siren.
					}
				}
				if (prefs.getBoolean(GeoFencingFragment.locationKey, false)) {

					String message = "Latitude = " + location.getLatitude()
							+ "\nLongitude = " + location.getLongitude();
					SmsManager sms = SmsManager.getDefault();

					PendingIntent pi = PendingIntent.getActivity(
							getApplicationContext(), 0, new Intent(
									GeoFencingService.this,
									GeoFencingService.class),
							PendingIntent.FLAG_CANCEL_CURRENT);

					// Getting phoneNumber
					SharedPreferences defaultPrefs = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					String phoneNumber = defaultPrefs.getString(
							"geo_location_number_primary", "0");

					if (!phoneNumber.equals("0") && phoneNumber != null)
						sms.sendTextMessage(phoneNumber, null, message, pi,
								null);
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
							interval * 1000 * 60, 1, listener);

					// Intent sendLocationIntent = new Intent(
					// GeoFencingService.this,
					// SendLocationService.class);
					// sendLocationIntent.putExtra("INTERVAL", interval);
					// startService(sendLocationIntent);

				}
				isLocked = true;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				deviceMan.lockNow();
			}

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	}

}
