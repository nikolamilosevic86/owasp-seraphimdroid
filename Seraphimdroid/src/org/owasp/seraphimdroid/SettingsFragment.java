package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.GeoFencingAdminReceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
			remoteLocationPref, appInstallerLockPref;
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
		
		appInstallerLockPref = (CheckBoxPreference) findPreference("lock_app_installer");
		if(defaultPrefs.getBoolean("uninstall_locked", true)) {
			appInstallerLockPref.setChecked(true);
		}
		appInstallerLockPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				CheckBoxPreference pref = (CheckBoxPreference) preference;
				String pkgName = "com.android.packageinstaller";
				DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Cursor cursor = db.rawQuery(
						"SELECT * FROM locks WHERE package_name=\'" + pkgName
								+ "\'", null);
				if(pref.isChecked()) {
					if (!cursor.moveToNext()) {
						ContentValues cv = new ContentValues();
						cv.put("package_name", pkgName);
						db.insert(DatabaseHelper.TABLE_LOCKS, null, cv);
						defaultPrefs.edit().putBoolean("uninstall_locked", true);
					}
				}
				else {
					if (cursor.moveToNext()) {
						String[] whereArgs = { pkgName };
						db.delete(DatabaseHelper.TABLE_LOCKS,
								"package_name=?", whereArgs);
						defaultPrefs.edit().putBoolean("uninstall_locked", false);
					}
				}
				cursor.close();
				db.close();
				dbHelper.close();
				return false;
			}
		});
		
		CheckBoxPreferenceClickListener listener = new CheckBoxPreferenceClickListener();

		remoteLockPref.setOnPreferenceClickListener(listener);
		remoteWipePref.setOnPreferenceClickListener(listener);
		remoteLocationPref.setOnPreferenceClickListener(listener);
		
		PreferenceScreen settingsChangePreference = (PreferenceScreen) findPreference("settings_check");
		settingsChangePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
	            arrayAdapter.add("Once a Day");
	            arrayAdapter.add("Once a Week");
	            arrayAdapter.add("Once a Fortnight");
	            arrayAdapter.add("Once a Month");
	            AlertDialog.Builder builder = new AlertDialog.Builder(
	                    getActivity());
	            builder.setTitle("Set Interval");
	            builder.setIcon(R.drawable.ic_launcher_small);
	            builder.setNegativeButton("Cancel", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
	            builder.setAdapter(arrayAdapter, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = arrayAdapter.getItem(which);
						Toast.makeText(getActivity(), "Interval changed to " + strName, Toast.LENGTH_SHORT).show();
						int milliSeconds = 24*60*60*1000;
						switch (which) {
						case 1:
							milliSeconds *= 7;
							break;
						case 2:
							milliSeconds *= 15;
							break;
						case 3:
							milliSeconds *= 30;
							break;
						default:
							break;
						}
						defaultPrefs.edit().putInt("interval", milliSeconds).commit();
					}
				});
	            
	            builder.show();
				return false;
			}
		});
	}

	private class CheckBoxPreferenceClickListener implements
			OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			final CheckBoxPreference pref = (CheckBoxPreference) preference;
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

						}
						else {
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
					// pref.setChecked(false);

					showSecureNumberDialog(pref);
				}
			} else {
				Toast.makeText(getActivity(),
						"You need to set a secret code first",
						Toast.LENGTH_SHORT).show();
				// pref.setChecked(false);

				showSecretCodeDialog(pref);

			}
			return false;
		}
	}

	private void showSecureNumberDialog(final CheckBoxPreference pref) {
		// Dialog to enter secure phone number.
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("You need to set a number to which you could receive location cordinates in case phone gets lost. Please enter a phone number now");

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View editTextPref = inflater.inflate(R.layout.edit_text_dialog_view,
				null, false);
		final EditText etNumber = (EditText) editTextPref
				.findViewById(R.id.et_pref);
		etNumber.setHint("Enter secure phone number");
		etNumber.setInputType(InputType.TYPE_CLASS_PHONE);
		builder.setView(editTextPref);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				if (etNumber.getText().length() < 5) {
					Toast.makeText(getActivity(), "Enter a valid number",
							Toast.LENGTH_SHORT).show();

				} else {
					SharedPreferences dPrefs = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					dPrefs.edit()
							.putString("geo_location_number_primary",
									etNumber.getText().toString()).commit();

					requestDeviceAdminPermission(pref);
					Toast.makeText(
							getActivity(),
							"You need to activate Device Administration Access",
							Toast.LENGTH_SHORT).show();
					arg0.dismiss();
				}
				pref.setChecked(false);
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub

						arg0.dismiss();
						pref.setChecked(false);
					}
				});
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
		// checkBox.setChecked(false);

	}

	private void showSecretCodeDialog(final CheckBoxPreference pref) {
		// Dialog to create Secret Code.
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("To access the remote services, you need to have a secret code which will trigger the actions. You haven't had created one yet, please create one now.");

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View editTextPref = inflater.inflate(R.layout.edit_text_dialog_view,
				null, false);
		final EditText etNumber = (EditText) editTextPref
				.findViewById(R.id.et_pref);
		etNumber.setHint("Enter secret code ");
		etNumber.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(editTextPref);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

				SharedPreferences dPrefs = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				if (etNumber.getText().toString().equals(" "))
					etNumber.setText("");

				if (etNumber.getText().toString().length() < 5) {
					Toast.makeText(getActivity(),
							"Length should be greater than 5 characters",
							Toast.LENGTH_SHORT).show();

				} else {
					dPrefs.edit()
							.putString("remote_secret_code",
									etNumber.getText().toString()).commit();
					arg0.dismiss();
					if (dPrefs.getString("geo_location_number_primary", "")
							.equals("")) {
						showSecureNumberDialog(pref);
					} else {
						Toast.makeText(
								getActivity(),
								"You need to activate Device Administration Access",
								Toast.LENGTH_SHORT).show();
						requestDeviceAdminPermission(pref);
					}

				}
				pref.setChecked(false);
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub

						arg0.dismiss();
						pref.setChecked(false);
					}
				});
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
		// checkBox.setChecked(false);
	}

	private void requestDeviceAdminPermission(Preference pref){
		if (!dpm.isAdminActive(component)) {
			String key = pref.getKey();
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
