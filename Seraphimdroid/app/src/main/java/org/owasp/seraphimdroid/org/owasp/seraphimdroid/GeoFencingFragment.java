package org.owasp.seraphimdroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.owasp.seraphimdroid.receiver.GeoFencingAdminReceiver;
import org.owasp.seraphimdroid.services.GPSTracker;
import org.owasp.seraphimdroid.services.GeoFencingService;

import java.lang.reflect.Field;

public class GeoFencingFragment extends Fragment {

	// private MapView mapView;
	private GoogleMap googleMap;

	private CheckBox cbRemoteLock, cbRemoteWipe, cbSiren, cbLocation;
	private TextView tvCenter, tvRemoteWipeLabel, tvRemoteLockLabel,
			tvSirenLabel, tvSendLocationLabel;
	private Button btnFence;
	private EditText etRadius;
	private ImageButton imgBtnLocation;
	public static final int ADMIN_ACTIVATION_REQ = 1001;
	public static final int GPS_ALERT = 1002;

	private SharedPreferences prefs;
	public static final String lockKey = "org.owasp.seraphimdroid.geofencing.lock";
	public static final String wipeKey = "org.owasp.seraphimdroid.geofencing.wipe";
	public static final String sirenKey = "org.owasp.seraphimdroid.geofencing.siren";
	public static final String locationKey = "org.owasp.seraphimdroid.geofencing.location";

	public static Location center = null;
	private DevicePolicyManager dpm;
	private ComponentName deviceAdminComponent;

	private GPSTracker gpsTracker;
	private AlertDialog gpsAlert = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_geofencing, container,
				false);

		prefs = getActivity().getSharedPreferences("org.owasp.seraphimdroid",
				Context.MODE_PRIVATE);

		dpm = (DevicePolicyManager) getActivity().getSystemService(
				Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponent = new ComponentName(this.getActivity(),
				GeoFencingAdminReceiver.class);

		gpsTracker = new GPSTracker(getActivity());
		// initViews(view, savedInstanceState);
		// prefs = getActivity().getSharedPreferences("org.owasp.seraphimdroid",
		// Context.MODE_PRIVATE);
		//
		// dpm = (DevicePolicyManager) getActivity().getSystemService(
		// Context.DEVICE_POLICY_SERVICE);
		// deviceAdminComponent = new ComponentName(this.getActivity(),
		// GeoFencingAdminReceiver.class);
		//
		// gpsTracker = new GPSTracker(getActivity());
		initViews(view, savedInstanceState);

		return view;
	}

	private SupportMapFragment mapFragment;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager childFragmentManager = getChildFragmentManager();
		mapFragment = (SupportMapFragment) childFragmentManager
				.findFragmentById(R.id.maps);
		if (mapFragment == null) {
			mapFragment = SupportMapFragment.newInstance();
			childFragmentManager.beginTransaction()
					.replace(R.id.maps, mapFragment).commit();
		}

	}

	private void initViews(View view, Bundle savedInstanceState) {

		// try {
		// MapsInitializer.initialize(getActivity());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// switch (GooglePlayServicesUtil
		// .isGooglePlayServicesAvailable(getActivity())) {
		// case ConnectionResult.SUCCESS:
		// mapView = (MapView) view.findViewById(R.id.maps);
		// mapView.onCreate(savedInstanceState);
		// }

		// Initializing CheckBoxes
		cbRemoteLock = (CheckBox) view.findViewById(R.id.cb_remote_lock);
		cbRemoteLock.setTag("lock");
		cbRemoteLock.setChecked(prefs.getBoolean(lockKey, true));
		cbRemoteWipe = (CheckBox) view.findViewById(R.id.cb_remote_wipe);
		cbRemoteWipe.setChecked(prefs.getBoolean(wipeKey, false));
		cbRemoteWipe.setTag("wipe");
		cbSiren = (CheckBox) view.findViewById(R.id.cb_siren);
		cbSiren.setChecked(prefs.getBoolean(sirenKey, true));
		cbSiren.setTag("siren");
		cbLocation = (CheckBox) view.findViewById(R.id.cb_location);
		cbLocation.setChecked(prefs.getBoolean(locationKey, false));
		cbLocation.setTag("location");

		// cbRemoteLock.setOnCheckedChangeListener(listener);

		// Initializing TextViews.
		tvRemoteLockLabel = (TextView) view
				.findViewById(R.id.tv_remote_lock_label);
		tvRemoteLockLabel.setTag("lock");
		tvRemoteWipeLabel = (TextView) view
				.findViewById(R.id.tv_remote_wipe_label);
		tvRemoteWipeLabel.setTag("wipe");
		tvSirenLabel = (TextView) view.findViewById(R.id.tv_siren_label);
		tvSirenLabel.setTag("siren");
		tvSendLocationLabel = (TextView) view
				.findViewById(R.id.tv_location_label);
		tvSendLocationLabel.setTag("location");
		tvCenter = (TextView) view.findViewById(R.id.tv_geo_center);

		// tvCenter.setText(googleMap.getMyLocation().getLatitude() + ","
		// + googleMap.getMyLocation().getLongitude());

		// Initializing EditTexts.
		etRadius = (EditText) view.findViewById(R.id.et_geo_radius);

		// Initializing Buttons.
		btnFence = (Button) view.findViewById(R.id.btn_geo_start_fencing);
		imgBtnLocation = (ImageButton) view
				.findViewById(R.id.img_btn_geo_location);

		// Setting Listeners
		TextViewListener rListener = new TextViewListener();
		CheckBoxListener cbListener = new CheckBoxListener();
		tvRemoteLockLabel.setOnClickListener(rListener);
		tvRemoteWipeLabel.setOnClickListener(rListener);
		tvSendLocationLabel.setOnClickListener(rListener);
		tvSirenLabel.setOnClickListener(rListener);

		cbRemoteLock.setOnCheckedChangeListener(cbListener);
		cbRemoteWipe.setOnCheckedChangeListener(cbListener);
		cbSiren.setOnCheckedChangeListener(cbListener);
		cbLocation.setOnCheckedChangeListener(cbListener);

		btnFence.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				Button btn = (Button) paramView;
				String btnText = btn.getText().toString();
				if (btnText.equals(getString(R.string.turn_gps_on))) {
					showGPSAlert();
					return;
				}

				if (!cbRemoteLock.isChecked() && !cbRemoteWipe.isChecked()
						&& !cbSiren.isChecked() && !cbLocation.isChecked()) {
					Toast.makeText(getActivity(),
							"Select atleast one of the check box",
							Toast.LENGTH_SHORT).show();
				} else

					fencing(paramView);
			}
		});

		imgBtnLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				getCurrentLocation();
				if (center != null) {
					tvCenter.setText(center.getLatitude() + ","
							+ center.getLongitude());
				}
			}
		});

	}

	private void getCurrentLocation() {
		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			if (!gettingLocation)
				new CurrentLocationTask().execute();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Enable GPS for better location accuracy");
			builder.setTitle("Enable GPS");
			builder.setPositiveButton(getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					}).setNegativeButton(getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
						}
					});
			AlertDialog gpsAlert = builder.create();
			gpsAlert.show();
		}

	}

	public void fencing(View view) {
		Button btnFencing = (Button) view;
		if (btnFencing.getText().equals(getString(R.string.geo_start_fencing))) {

			if (dpm.isAdminActive(deviceAdminComponent)) {
				startFencing();
			} else {
				Intent deviceAdminIntent = new Intent(
						DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				deviceAdminIntent.putExtra(
						DevicePolicyManager.EXTRA_DEVICE_ADMIN,
						deviceAdminComponent);
				deviceAdminIntent.putExtra(
						DevicePolicyManager.EXTRA_ADD_EXPLANATION,
						"Required for GeoFencing");
				startActivityForResult(deviceAdminIntent, ADMIN_ACTIVATION_REQ);
			}

		} else if (btnFencing.getText().equals(
				getString(R.string.geo_stop_fencing))) {

			enableAll(true);
			// Stop Service
			getActivity().stopService(
					new Intent(getActivity(), GeoFencingService.class));

			btnFencing.setText(getString(R.string.geo_start_fencing));
		}
	}

	private void startFencing() {

		// Start Service
		Intent serviceIntent = new Intent(getActivity(),
				GeoFencingService.class);
		if (center == null) {
			Toast.makeText(getActivity(),
					"Current location not set, getting current location",
					Toast.LENGTH_LONG).show();
			// try {
			// new CurrentLocationTask().execute();
			// // center = gpsTracker.getLocation();
			// if (center != null)
			// tvCenter.setText(center.getLatitude() + ","
			// + center.getLongitude());
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			getCurrentLocation();
			return;
		}
		if (etRadius.getText().toString().equals("")
				|| etRadius.getText().toString().equals(null)
				|| Double.valueOf(etRadius.getText().toString()) < 100) {
			String message = "Radius not set";
			if (!etRadius.getText().toString().equals("")
					&& Double.valueOf(etRadius.getText().toString()) < 100)
				message = "Radius should be more that or equal to 100 meters";
			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			etRadius.requestFocus();
			return;
		}
		if (center != null && !etRadius.getText().toString().equals("")) {

			// dpm.setPasswordMinimumLength(deviceAdminComponent, 3);
			dpm.setPasswordQuality(deviceAdminComponent,
					DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
			// dpm.setPasswordMinimumLetters(deviceAdminComponent, 0);
			// dpm.setPasswordMinimumLowerCase(deviceAdminComponent, 0);
			// dpm.setPasswordMinimumNonLetter(deviceAdminComponent, 0);
			// dpm.setPasswordMinimumNumeric(deviceAdminComponent, 4);
			// dpm.setPasswordMinimumSymbols(deviceAdminComponent, 0);
			// dpm.setPasswordMinimumUpperCase(deviceAdminComponent, 0);

			if (!dpm.isActivePasswordSufficient()) {
				Toast.makeText(getActivity(),
						"Create a password to lock device with",
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(
						DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
				return;
			}

			serviceIntent.putExtra("RADIUS",
					Long.parseLong(etRadius.getText().toString()));
			getActivity().startService(serviceIntent);
			enableAll(false);
			btnFence.setText(getString(R.string.geo_stop_fencing));
			// Toast.makeText(getActivity(), "GeoFencing is ON",
			// Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ADMIN_ACTIVATION_REQ:
			if (resultCode == Activity.RESULT_OK) {
				// Start Service
				startFencing();
			} else {
				Toast.makeText(getActivity(), "Unable to start GeoFencing",
						Toast.LENGTH_LONG).show();
			}
			break;
		case GPS_ALERT:
			LocationManager lm = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				btnFence.setText(getString(R.string.geo_start_fencing));
			} else {
				showGPSAlert();
			}

			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// private void disableAll() {
	// cbRemoteLock.setEnabled(false);
	// cbRemoteWipe.setEnabled(false);
	// cbSiren.setEnabled(false);
	// imgBtnLocation.setEnabled(false);
	// etRadius.setEnabled(false);
	// tvCenter.setEnabled(false);
	// }

	private void enableAll(boolean enabled) {
		cbSiren.setEnabled(enabled);
		cbRemoteLock.setEnabled(enabled);
		cbRemoteWipe.setEnabled(enabled);
		cbLocation.setEnabled(enabled);
		imgBtnLocation.setEnabled(enabled);
		etRadius.setEnabled(enabled);
		tvCenter.setEnabled(enabled);
	}

	private class TextViewListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			AlertDialog.Builder info = new AlertDialog.Builder(getActivity());
			info.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			String tag = (String) view.getTag();
			if (tag.equals("lock")) {
				// cbRemoteLock.performClick();
				info.setMessage(R.string.geo_lock_info);
			} else if (tag.equals("wipe")) {
				// cbRemoteWipe.performClick();
				info.setMessage(R.string.geo_wipe_info);
			} else if (tag.equals("siren")) {
				// cbSiren.performClick();
				info.setMessage(R.string.geo_siren_info);
			} else if (tag.equals("location")) {
				info.setMessage(R.string.geo_location_info);
			}
			AlertDialog infoDialog = info.create();
			infoDialog.show();
		}
	}

	private class CheckBoxListener implements
			android.widget.CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(final CompoundButton checkBox,
				boolean paramBoolean) {
			String tag = (String) checkBox.getTag();
			if (tag.equals("lock")) {
				if (cbRemoteWipe.isChecked() && !checkBox.isChecked()) {
					Toast.makeText(getActivity(),
							"Lock is required if you need to wipe",
							Toast.LENGTH_SHORT).show();
					checkBox.setChecked(true);
				} else
					prefs.edit().putBoolean(lockKey, checkBox.isChecked())
							.commit();
			} else if (tag.equals("wipe")) {
				if (checkBox.isChecked())
					cbRemoteLock.setChecked(true);
				prefs.edit().putBoolean(wipeKey, checkBox.isChecked()).commit();
			} else if (tag.equals("siren")) {
				prefs.edit().putBoolean(sirenKey, checkBox.isChecked())
						.commit();
			} else if (tag.equals("location")) {
				SharedPreferences dPrefs = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				String number = dPrefs.getString("geo_location_number_primary",
						null);
				if (checkBox.isChecked()) {
					if (number == null || number.equals("")) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage("You need to set a number to which you could receive location cordinates in case phone gets lost. Please enter a phone number now");
						// builder.setNeutralButton("OK",
						// new DialogInterface.OnClickListener() {
						// @Override
						// public void onClick(DialogInterface dialog,
						// int arg1) {
						// dialog.dismiss();
						// }
						// });
						LayoutInflater inflater = (LayoutInflater) getActivity()
								.getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);
						View editTextPref = inflater.inflate(
								R.layout.edit_text_dialog_view, null, false);
						final EditText etNumber = (EditText) editTextPref
								.findViewById(R.id.et_pref);
						etNumber.setHint("Enter secure phone number");
						builder.setView(editTextPref);

						builder.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										if (etNumber.getText().length() < 5) {
											Toast.makeText(getActivity(),
													"Enter a valid number",
													Toast.LENGTH_SHORT).show();
											checkBox.setChecked(false);
										} else {
											SharedPreferences dPrefs = PreferenceManager
													.getDefaultSharedPreferences(getActivity());
											dPrefs.edit()
													.putString(
															"geo_location_number_primary",
															etNumber.getText()
																	.toString())
													.commit();
											arg0.dismiss();
										}
									}
								});
						builder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub

										arg0.dismiss();
										checkBox.setChecked(false);
									}
								});
						builder.setCancelable(false);
						AlertDialog alert = builder.create();
						alert.show();
						// checkBox.setChecked(false);
					}
				} else
					prefs.edit().putBoolean(locationKey, checkBox.isChecked())
							.commit();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// mapView.onResume();

		// Initializing Map
		if (googleMap == null) {
			// googleMap = ((MapFragment) getActivity().getFragmentManager()
			// .findFragmentById(R.id.maps)).getMap();
			googleMap = mapFragment.getMap();
			googleMap.getUiSettings().setZoomControlsEnabled(false);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.setMyLocationEnabled(true);
		}

		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		if (isServiceRunning(GeoFencingService.class)) {
			if (GeoFencingService.center != null)
				tvCenter.setText(GeoFencingService.center.getLatitude() + ","
						+ GeoFencingService.center.getLongitude());
			etRadius.setText(GeoFencingService.DISTANCE + "");
			enableAll(false);
			btnFence.setText(getString(R.string.geo_stop_fencing));
		} else {
			if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				showGPSAlert();
			} else {
				try {
					if (!gettingLocation)
						new CurrentLocationTask().execute();
					// center = gpsTracker.getLocation();

					// tvCenter.setText(center.getLatitude() + ","
					// + center.getLongitude());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private void showGPSAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("GPS is needed to access this service. Please turn ON the GPS to use this feature.");
		builder.setTitle("Enable GPS");
		builder.setCancelable(false);
		builder.setPositiveButton("GPS settings",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						// startActivity(intent);
						startActivityForResult(intent, GPS_ALERT);
					}
				}).setNegativeButton(getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						// FragmentManager fm = getActivity()
						// .getSupportFragmentManager();
						// fm.beginTransaction()
						// .replace(R.id.fragment_container,
						// new AppLockFragment()).commit();
						// Toast.makeText(
						// getActivity(),
						// "Cannot access the service without GPS",
						// Toast.LENGTH_SHORT).show();

						btnFence.setText(getString(R.string.turn_gps_on));

						dialog.dismiss();
					}
				});
		gpsAlert = builder.create();
		gpsAlert.show();

	}

	@Override
	public void onPause() {
		if (gpsAlert != null)
			gpsAlert.dismiss();
		android.app.Fragment mapFragment = getActivity().getFragmentManager()
				.findFragmentById(R.id.maps);
		if (mapFragment != null) {
			getActivity().getFragmentManager().beginTransaction()
					.remove(mapFragment).commit();
		}

		super.onPause();
		// mapView.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFM = Fragment.class
					.getDeclaredField("childFragmentManager");
			childFM.setAccessible(true);
			childFM.set(this, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroy() {
		// android.app.Fragment mapFragment = getActivity().getFragmentManager()
		// .findFragmentById(R.id.maps);
		// if (mapFragment != null) {
		// getActivity().getFragmentManager().beginTransaction()
		// .remove(mapFragment).commit();
		// }
		gpsTracker.stopUsingGPS();
		super.onDestroy();
		// mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		// mapView.onLowMemory();
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager am = (ActivityManager) this.getActivity()
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : am
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName()))
				return true;
		}
		return false;
	}

	private class CurrentLocationTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog pd;
		private int timer = 0;

		public CurrentLocationTask() {
			pd = new ProgressDialog(getActivity());

		}

		@Override
		protected void onPreExecute() {
			pd.setTitle("Getting Location");
			pd.setMessage("Please wait...");
			pd.setCancelable(false);
			pd.show();
			googleMap.stopAnimation();
			googleMap.clear();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			gettingLocation = true;
			while (gettingLocation) {
				if (timer > 3 && timer < 5)
					pd.setCancelable(true);
				if (timer > 10) {
					gettingLocation = false;
					return null;
				}
				timer++;
				if (center == null) {
					center = gpsTracker.getLocation();
					gettingLocation = false;
				}
				try {
					Thread.sleep(1000);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			gettingLocation = false;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			if (center == null) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						getActivity());
				alert.setNeutralButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});

				alert.setTitle("Network problem")
						.setMessage(
								"Please make sure you are either under the open sky or you are connected to internet. If you are connected to internet please check you connection.");
				alert.create().show();
			} else {
				tvCenter.setText(center.getLatitude() + ","
						+ center.getLongitude());
				LatLng current = new LatLng(center.getLatitude(),
						center.getLongitude());
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(current).zoom(17).build();
				// googleMap.addMarker(new MarkerOptions().position(current)
				// .title("You are here!").draggable(false));

				googleMap.animateCamera(
						CameraUpdateFactory.newCameraPosition(cameraPosition),
						2000, null);
			}
			super.onPostExecute(result);
		}

	}

	private static boolean gettingLocation = false;
}
