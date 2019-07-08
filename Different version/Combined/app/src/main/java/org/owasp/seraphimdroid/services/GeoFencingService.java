package org.owasp.seraphimdroid.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.owasp.seraphimdroid.GeoFencingFragment;
import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.receiver.GeoFencingAdminReceiver;

import java.io.IOException;
import java.util.Calendar;

public class GeoFencingService extends Service {

	public static Location center;
	public static long TIME = 1000 * 10, // Seconds
			DISTANCE; // meters
	private static int counter = 0;
	private DevicePolicyManager deviceMan;
	private ComponentName deviceAdminComponent;
	private SharedPreferences prefs, defaultPrefs;
	private LocationManager lm;
	private android.location.LocationListener listener;

	private static final int ForeGroundId = 1002;
	private boolean isLocked = false;

	private AlarmManager am = null;
	private BroadcastReceiver alarmReceiver = null;
	private BroadcastReceiver sendLocationReceiver = null;
	private PendingIntent operationSiren = null;
	private PendingIntent operationSendLocation = null;
	private MediaPlayer mPlayer = null;
	private AudioManager audioMan = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("Service", "ON");

		center = GeoFencingFragment.center;
		DISTANCE = intent.getLongExtra("RADIUS", 200);
		if (center == null) {
			Toast.makeText(getApplicationContext(), "Can't find the center",
					Toast.LENGTH_SHORT).show();
			stopService(new Intent(this, GeoFencingService.class));
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
				.setVibrate(new long[]{300, 500, 300})
				.setLights(Color.YELLOW, 2000, 3000)
				.setSmallIcon(R.drawable.ic_launcher);

		startForeground(ForeGroundId, builder.build());

		alarmReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (mPlayer != null && mPlayer.isPlaying()) {
					mPlayer.stop();
					mPlayer.reset();
				}
				if (mPlayer != null) {
					mPlayer.release();
					mPlayer = null;
				}
				mPlayer = MediaPlayer.create(GeoFencingService.this,
						R.raw.alarm_sound);
				// if (mPlayer == null) {
				// Uri soundUri = RingtoneManager
				// .getDefaultUri(RingtoneManager.TYPE_ALARM);
				// if (soundUri == null)
				// soundUri = RingtoneManager
				// .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				// if (soundUri == null)
				// soundUri = RingtoneManager
				// .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				// }
				if (audioMan == null)
					audioMan = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				Resources res = getResources();
				AssetFileDescriptor afd = res
						.openRawResourceFd(R.raw.alarm_sound);
				mPlayer.reset();
				mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mPlayer.setLooping(true);
				try {
					mPlayer.setDataSource(afd.getFileDescriptor(),
							afd.getStartOffset(), afd.getLength());
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				audioMan.setStreamVolume(AudioManager.STREAM_ALARM,
						audioMan.getStreamMaxVolume(AudioManager.STREAM_ALARM),
						AudioManager.FLAG_PLAY_SOUND);
				// mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				try {
					mPlayer.prepareAsync();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
				mPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer player) {
						player.start();
					}
				});

			}

		};

		sendLocationReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// Setting up variable to create message.
				GPSTracker gps = new GPSTracker(getApplicationContext());
				Location current = null;
				gps.location = null;

				if (gps.canGetLocation()) {
					while (current == null) {
						current = gps.getLocation();
					}
				}
				String message = "";
				if (current != null) {
					message = "Latitude = " + current.getLatitude()
							+ "\nLongitude = " + current.getLongitude();
				}

				SmsManager sms = SmsManager.getDefault();

				PendingIntent pi = PendingIntent.getActivity(
						getApplicationContext(), 0, new Intent(
								GeoFencingService.this, MainActivity.class), 0);

				// Getting phoneNumber
				SharedPreferences defaultPrefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				String phoneNumber = defaultPrefs.getString(
						"geo_location_number_primary", "0");

				if (!phoneNumber.equals("0") && phoneNumber != null)
					sms.sendTextMessage(phoneNumber, null, message, pi, null);
				gps.stopUsingGPS();
			}
		};

		IntentFilter filter = new IntentFilter(
				"org.owasp.seraphimdroid.geo_fencing_alarm");
		registerReceiver(alarmReceiver, filter);
		filter = new IntentFilter("org.owasp.seraphimdroid.send_location");
		registerReceiver(sendLocationReceiver, filter);

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
		// SMSRecepter.isRunning = false;
		// stopService(new Intent(this, SMSRecepter.class));

		// Stopping Alarm.
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.reset();
			mPlayer.release();
		}
		if (alarmReceiver != null)
			unregisterReceiver(alarmReceiver);
		if (am != null && operationSiren != null)
			am.cancel(operationSiren);
		if(operationSendLocation != null)
			am.cancel(operationSendLocation);

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
						Toast.makeText(getApplicationContext(),
								"Creating alarm", Toast.LENGTH_SHORT).show();
						if (am == null)
							am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

						Intent intent = new Intent(
								"org.owasp.seraphimdroid.geo_fencing_alarm");
						if (operationSiren == null)
							operationSiren = PendingIntent.getBroadcast(
									GeoFencingService.this, 0, intent, 0);
						am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance()
								.getTimeInMillis() + 1, operationSiren);
					}
					if (prefs.getBoolean(GeoFencingFragment.locationKey, false)) {

						Intent intent = new Intent(
								"org.owasp.seraphimdroid.send_location");
						if (operationSendLocation == null)
							operationSendLocation = PendingIntent.getBroadcast(
									GeoFencingService.this, 0, intent, 0);
						long intervalMillis = interval * 1000 * 60;
						am.setInexactRepeating(AlarmManager.RTC, Calendar
								.getInstance().getTimeInMillis(),
								intervalMillis, operationSendLocation);
						// Intent sendLocationIntent = new Intent(
						// GeoFencingService.this,
						// SendLocationService.class);
						// sendLocationIntent.putExtra("INTERVAL", interval);
						// startService(sendLocationIntent);
					}
					isLocked = true;
					// counter = 0;
				}

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
