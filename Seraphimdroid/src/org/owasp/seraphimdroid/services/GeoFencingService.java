package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.GeoFencingFragment;
import org.owasp.seraphimdroid.receiver.GeoFencingAdminReceiver;

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
import android.util.Log;
import android.widget.Toast;

public class GeoFencingService extends Service {

	public static Location center;
	public static long TIME = 1000, // Seconds
			DISTANCE; // meters
	private static int counter = 0;
	private DevicePolicyManager deviceMan;
	private ComponentName deviceAdminComponent;
	private SharedPreferences prefs;

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

		deviceAdminComponent = new ComponentName(this,
				GeoFencingAdminReceiver.class);
		deviceMan = (DevicePolicyManager) this
				.getSystemService(Context.DEVICE_POLICY_SERVICE);

		LocationManager lm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		String provider = lm.getBestProvider(new Criteria(), false);
		android.location.LocationListener listener = new FencingListener();
		lm.requestLocationUpdates(provider, TIME, 1, listener);

		Toast.makeText(getApplicationContext(), "Fencing ON",
				Toast.LENGTH_SHORT).show();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		deviceMan.setMaximumFailedPasswordsForWipe(deviceAdminComponent, 0);
		// deviceMan.setPasswordMinimumLength(deviceAdminComponent, 0);
		// deviceMan.resetPassword("",
		// DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
		Log.d("GeoService", "Fencing removed");
		Toast.makeText(getApplicationContext(), "Fencing removed.",
				Toast.LENGTH_SHORT).show();
		super.onDestroy();

	}

	private class FencingListener implements android.location.LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			if (counter < 5) {
				if (center.distanceTo(location) > DISTANCE) {
					counter++;
				} else {
					counter = 0;
				}
			}

			if (counter >= 5) {
				if (prefs.getBoolean(GeoFencingFragment.lockKey, false)) {
					deviceMan.lockNow();
				}
				if (prefs.getBoolean(GeoFencingFragment.wipeKey, false)) {
					SharedPreferences defaultsPrefs = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					int tries = Integer.valueOf(defaultsPrefs.getString(
							"geo_password_tries", "5"));
					deviceMan.lockNow();
					deviceMan.setMaximumFailedPasswordsForWipe(
							deviceAdminComponent, tries);
				}
				if (prefs.getBoolean(GeoFencingFragment.sirenKey, false)) {
					// Start Siren.
				}
				if (prefs.getBoolean(GeoFencingFragment.locationKey, false)) {

				}
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			deviceMan.lockNow();

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
