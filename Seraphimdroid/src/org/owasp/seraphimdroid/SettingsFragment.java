package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.GeoFencingAdminReceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	//
	// View view = inflater.inflate(R.layout.fragment_settings, container,
	// false);
	//
	// LinearLayout layoutChangePassword = (LinearLayout) view
	// .findViewById(R.id.layout_change_password);
	// layoutChangePassword.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View view) {
	// changePassword(view);
	// }
	// });
	//
	// return view;
	//
	// }
	private CheckBoxPreference remoteLockPref, remoteWipePref,
			remoteLocationPref;
	private final int REMOTE_LOCK_ID = 201;
	private final int REMOTE_WIPE_ID = 202;
	private final int REMOTE_LOCATION_ID = 203;
	private DevicePolicyManager dpm;
	private ComponentName component;
	private SharedPreferences defaultPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_fragment);

		dpm = (DevicePolicyManager) getActivity().getSystemService(
				Context.DEVICE_POLICY_SERVICE);
		component = new ComponentName(getActivity(),
				GeoFencingAdminReceiver.class);

		defaultPrefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		remoteLockPref = (CheckBoxPreference) findPreference("remote_lock");
		remoteWipePref = (CheckBoxPreference) findPreference("remote_wipe");
		remoteLocationPref = (CheckBoxPreference) findPreference("remote_location");

		CheckBoxPreferenceClickListener listener = new CheckBoxPreferenceClickListener();

		remoteLockPref.setOnPreferenceClickListener(listener);
		remoteWipePref.setOnPreferenceClickListener(listener);
		remoteLocationPref.setOnPreferenceClickListener(listener);
	}

	private class CheckBoxPreferenceClickListener implements
			OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			CheckBoxPreference pref = (CheckBoxPreference) preference;
			String secretCode = defaultPrefs
					.getString("remote_secret_code", "");
			
			String phoneNumber = defaultPrefs.getString(
					"geo_location_number_primary", "");
			String key = pref.getKey();

			if (secretCode != null && !secretCode.equals("")) {
				if (!phoneNumber.equals("") && phoneNumber != null) {
					if (pref.isChecked()) {
						if (dpm.isAdminActive(component)) {
							if (key.equals("remote_lock")) {
								dpm.setPasswordQuality(
										component,
										DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
								if (!dpm.isActivePasswordSufficient()) {
									Toast.makeText(
											getActivity(),
											"Create a password to lock device with",
											Toast.LENGTH_SHORT).show();

									startActivity(new Intent(
											DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
									remoteLockPref.setChecked(false);
									return true;
								} else
									pref.setChecked(true);
							} else if (key.equals("remote_location")) {
								LocationManager lm = (LocationManager) getActivity()
										.getSystemService(
												Context.LOCATION_SERVICE);
								if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
									AlertDialog.Builder builder = new AlertDialog.Builder(
											getActivity());
									builder.setMessage("GPS isd needed to access this service. Please turn ON the GPS to use this feature.");
									builder.setTitle("Enable GPS");
									builder.setPositiveButton(
											"GPS settings",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int arg1) {
													Intent intent = new Intent(
															Settings.ACTION_LOCATION_SOURCE_SETTINGS);
													startActivity(intent);
												}
											})
											.setNegativeButton(
													getString(android.R.string.cancel),
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(
																DialogInterface dialog,
																int arg1) {
															Toast.makeText(
																	getActivity(),
																	"Can't enable the service, GPS is required",
																	Toast.LENGTH_SHORT)
																	.show();
															dialog.dismiss();
														}
													});
									AlertDialog gpsAlert = builder.create();
									gpsAlert.show();
									remoteLocationPref.setChecked(false);
								} else {
									pref.setChecked(true);
								}

							} else
								pref.setChecked(true);

						} else {
							Intent deviceAdminIntent = new Intent(
									DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
							deviceAdminIntent.putExtra(
									DevicePolicyManager.EXTRA_DEVICE_ADMIN,
									component);
							deviceAdminIntent.putExtra(
									DevicePolicyManager.EXTRA_ADD_EXPLANATION,
									"Required for GeoFencing");

							if (key.equals("remote_lock"))
								startActivityForResult(deviceAdminIntent,
										REMOTE_LOCK_ID);
							else if (key.equals("remote_wipe"))
								startActivityForResult(deviceAdminIntent,
										REMOTE_WIPE_ID);
							else if (key.equals("remote_location"))
								startActivityForResult(deviceAdminIntent,
										REMOTE_LOCATION_ID);
						}
					}
				} else {
					Toast.makeText(getActivity(),
							"You need to set a secure phone number first",
							Toast.LENGTH_SHORT).show();
					pref.setChecked(false);
				}
			} else {
				Toast.makeText(getActivity(),
						"You need to set a secret code first",
						Toast.LENGTH_SHORT).show();
				pref.setChecked(false);
			}
			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REMOTE_LOCK_ID:
			if (resultCode == Activity.RESULT_OK) {
				// Start Service
				dpm.setPasswordQuality(component,
						DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
				if (!dpm.isActivePasswordSufficient()) {
					Toast.makeText(getActivity(),
							"Create a password to lock device with",
							Toast.LENGTH_SHORT).show();
					startActivity(new Intent(
							DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
					remoteLockPref.setChecked(false);
					return;
				} else
					remoteLockPref.setChecked(true);
			} else {
				Toast.makeText(getActivity(), "Remote Lock can't be enabled",
						Toast.LENGTH_LONG).show();
				remoteLockPref.setChecked(false);
			}
			break;
		case REMOTE_WIPE_ID:
			if (resultCode == Activity.RESULT_OK) {
				// Start Service
				remoteWipePref.setChecked(true);
			} else {
				Toast.makeText(getActivity(), "Remote Wipe can't be enabled",
						Toast.LENGTH_LONG).show();
				remoteWipePref.setChecked(false);
			}
			break;
		case REMOTE_LOCATION_ID:
			if (resultCode == Activity.RESULT_OK) {
				// Start Service
				remoteLocationPref.setChecked(true);
			} else {
				Toast.makeText(getActivity(),
						"Remote Location can't be enabled", Toast.LENGTH_LONG)
						.show();
				remoteLocationPref.setChecked(false);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void changePassword(View view) {
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS password");
		db.execSQL(DatabaseHelper.createPasswordTable);
		Intent pIntent = new Intent(getActivity(), PasswordActivity.class);

		pIntent.putExtra("PACKAGE_NAME", this.getActivity().getPackageName());
		startActivity(pIntent);
	}

}
