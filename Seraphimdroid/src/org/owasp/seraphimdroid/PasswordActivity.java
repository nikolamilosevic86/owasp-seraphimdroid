package org.owasp.seraphimdroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.owasp.seraphimdroid.helper.DatabaseHelper;
import org.owasp.seraphimdroid.model.NoImeEditText;
import org.owasp.seraphimdroid.receiver.BluetoothStateReceiver;
import org.owasp.seraphimdroid.receiver.MobileDataStateReceiver;
import org.owasp.seraphimdroid.receiver.WifiStateReceiver;
import org.owasp.seraphimdroid.services.AppLockService;
import org.owasp.seraphimdroid.services.KillBackgroundService;
import org.owasp.seraphimdroid.services.MakeACallService;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PasswordActivity extends Activity implements OnClickListener {

	public static final String TAG = "Password Activity";
	public static String lastUnlocked = null;

	private Button[] btnPass;
	private Button btnClear;
	private EditText etPassword;
	private TextView tvAlert, tvAppLabel;
	private ImageView imgAppIcon;
	private LinearLayout layoutOk;

	// Variables used to implement password mechanism
	private boolean isFirstAttempt;
	private boolean isSecondAttempt;
	private DatabaseHelper dbHelper;
	private String passwordTrail, passwordConfirm;
	private String pkgName;
	String deviceId = "";
	String service = "";
	int state;
	private boolean tryUnlocking;
	
	private boolean makeCall = false;
	private String phoneNumber = "";
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_password);
		
		//Retrieve the intent.
		Intent rootIntent = getIntent();
		pkgName = rootIntent.getStringExtra("PACKAGE_NAME");
        if(rootIntent.getExtras().containsKey("device_id")) {
        	deviceId = rootIntent.getStringExtra("device_id");
        }
        if(rootIntent.getExtras().containsKey("service")) {
        	service = rootIntent.getStringExtra("service");
        	state = Integer.parseInt(rootIntent.getStringExtra("state"));
        }
        
		// Initializing buttons.
		initButtons();

		// Initializing View.
		layoutOk = (LinearLayout) findViewById(R.id.layout_ok);
		etPassword = (NoImeEditText) findViewById(R.id.et_password);
		tvAlert = (TextView) findViewById(R.id.tv_alert);
		tvAppLabel = (TextView) findViewById(R.id.tv_app_label);
		imgAppIcon = (ImageView) findViewById(R.id.img_app_icon);
		imgAppIcon.setImageResource(R.drawable.ic_launcher_small);
		
		if(pkgName.length()>0) {
			try {
				PackageManager pm = getPackageManager();
				ApplicationInfo appInfo = pm.getApplicationInfo(pkgName,
						PackageManager.GET_META_DATA);
				String label = (String) appInfo.loadLabel(pm);
				if(pkgName.equals(getPackageName())) {
					tvAppLabel.setText(label);
				}
				else {
					tvAppLabel.setText(Html.fromHtml("<small>" + label+ " is locked by SeraphimDroid. In order to use this app, type Seraphimdroid passphrase.</small>"));
				}
				makeCall = rootIntent.getBooleanExtra("MAKE_CALL", false);
				phoneNumber = rootIntent.getStringExtra("PHONE_NUMBER");
			} catch (Exception e) {}
		}
		else {
			if(deviceId.length()>0) {
				tvAppLabel.setText(Html.fromHtml("<small>Seraphimdroid has detected a SIM change. In order to use the device, type Seraphimdroid passphrase.</small>"));
			}
			else if(service.length()>0) {
				tvAppLabel.setText(Html.fromHtml("<small>Seraphimdroid is set to lock this service. In order to start service, type Seraphimdroid passphrase.</small>"));
			}
		}
		
		// Initializing other required variables.

		dbHelper = new DatabaseHelper(getApplicationContext());
		passwordConfirm = null;
		passwordTrail = null;

		if (isPasswordCreated()) {
			layoutOk.setVisibility(View.GONE);
			tryUnlocking = true;
		} else {
			layoutOk.setVisibility(View.VISIBLE);
			btnPass[10].setText("Retry");
			btnPass[10].setTag("reset");
			etPassword.setHint("Enter 4 digit PIN");
		}
		
	}
	
	@Override
	protected void onResume() {
		startService(new Intent(this, AppLockService.class));
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		Intent killIntent = new Intent(PasswordActivity.this,
				KillBackgroundService.class);
		killIntent.putExtra("PACKAGE_NAME", pkgName);
		startService(killIntent);
		finish();
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			Intent killIntent = new Intent(PasswordActivity.this,
					KillBackgroundService.class);
			killIntent.putExtra("PACKAGE_NAME", pkgName);
			startService(killIntent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			Intent killIntent = new Intent(PasswordActivity.this,
					KillBackgroundService.class);
			killIntent.putExtra("PACKAGE_NAME", pkgName);
			startService(killIntent);
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	private void initButtons() {

		btnPass = new Button[12];

		btnPass[0] = (Button) findViewById(R.id.btn_password_0);
		btnPass[1] = (Button) findViewById(R.id.btn_password_1);
		btnPass[2] = (Button) findViewById(R.id.btn_password_2);
		btnPass[3] = (Button) findViewById(R.id.btn_password_3);
		btnPass[4] = (Button) findViewById(R.id.btn_password_4);
		btnPass[5] = (Button) findViewById(R.id.btn_password_5);
		btnPass[6] = (Button) findViewById(R.id.btn_password_6);
		btnPass[7] = (Button) findViewById(R.id.btn_password_7);
		btnPass[8] = (Button) findViewById(R.id.btn_password_8);
		btnPass[9] = (Button) findViewById(R.id.btn_password_9);
		btnPass[10] = (Button) findViewById(R.id.btn_password_reset);
		btnPass[11] = (Button) findViewById(R.id.btn_password_ok);

		for (int i = 0; i < 12; i++) {
			btnPass[i].setOnClickListener(this);

			if (i < 10) {
				btnPass[i].setText("" + i);
				btnPass[i].setTag("" + i);
			} else if (i == 10) {
				btnPass[i].setTag("exit");
				btnPass[i].setText("Exit");
				if (isFirstAttempt) {
					btnPass[i].setTag("reset");
					btnPass[i].setText("Reset");
				}

			} else {
				btnPass[i].setText(android.R.string.ok);
				btnPass[i].setTag("enter");
			}

		}

		btnClear = (Button) findViewById(R.id.btn_password_delete);
		btnClear.setText("Clear");
		btnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (etPassword.getText().toString().length() > 0) {
					etPassword.setText(etPassword.getText().toString()
							.substring(0, (etPassword.getText().length() - 1)));
					Log.d(TAG, etPassword.getText().toString());
				}
			}
		});

	}

	@Override
	public void onClick(View view) {
		tvAlert.setVisibility(View.GONE);
		String tag = (String) view.getTag();
		if (tag.equals("enter")) {

			if (isFirstAttempt) {
				if (etPassword.getText().toString().length() < 4) {
					String message = "PIN should be atleast 4 digits long.";
					Toast.makeText(getApplicationContext(), message,
							Toast.LENGTH_SHORT).show();
					tvAlert.setText(message);
					tvAlert.setVisibility(View.VISIBLE);
					return;
				}
				tvAlert.setText("");
				tvAlert.setVisibility(View.GONE);
				passwordTrail = etPassword.getText().toString();
				btnPass[10].setClickable(true);
				isFirstAttempt = false;
				isSecondAttempt = true;
				etPassword.setText("");
			} else if (isSecondAttempt) {
				String message = "";
				if (etPassword.getText().toString().length() != passwordTrail
						.length()) {
					message = "PIN should be of same size a before";
					tvAlert.setText(message);
					tvAlert.setVisibility(View.VISIBLE);
				} else if (!etPassword.getText().toString()
						.equals(passwordTrail)) {
					message = "PIN should be same as before";
					tvAlert.setText(message);
					tvAlert.setVisibility(View.VISIBLE);
				} else {
					tvAlert.setText("");
					tvAlert.setVisibility(View.GONE);
					isSecondAttempt = false;
					passwordConfirm = etPassword.getText().toString();
					savePassword();
					layoutOk.setVisibility(View.GONE);
					tryUnlocking = true;
					Toast.makeText(getApplicationContext(),
							"Password created successfully", Toast.LENGTH_SHORT)
							.show();
					finish();
				}
			} else if (isPasswordCorrect(etPassword.getText().toString())) {
				lastUnlocked = pkgName;
				this.finish();
			} else {
				tvAlert.setText("Incorrect Pin");
				tvAlert.setVisibility(View.VISIBLE);
			}

		} else if (tag.equals("reset")) {
			isFirstAttempt = true;
			Toast.makeText(getApplicationContext(), "Try Again",
					Toast.LENGTH_LONG).show();
		} else if (tag.equals("exit")) {
			Intent killIntent = new Intent(PasswordActivity.this,
					KillBackgroundService.class);
			killIntent.putExtra("PACKAGE_NAME", pkgName);
			startService(killIntent);
			finish();
		} else {

			int number = Integer.parseInt(tag);
			if (number < 10) {
				etPassword.setText(etPassword.getText().toString() + number);
				Log.d(TAG, etPassword.getText().toString());
			}
			if (tryUnlocking) {
				if (isPasswordCorrect(etPassword.getText().toString())) {
					if (makeCall) {
						Intent callServiceIntent = new Intent(
								PasswordActivity.this, MakeACallService.class);
						callServiceIntent.putExtra("PHONE_NUMBER", phoneNumber);
						startService(callServiceIntent);
					} else {
						if(deviceId.length()>0) {
							//Check for SIM1
							SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
							if(defaultPrefs.contains("sim_1")) {
								if(defaultPrefs.getString("sim_1", "").equals(deviceId)==false) {
									defaultPrefs.edit().putString("sim_1", deviceId).commit();
								}
							}
							else {
								defaultPrefs.edit().putString("sim_1", deviceId).commit();
								lastUnlocked = pkgName;
								this.finish();
							}
							//Check for SIM2
//							if(defaultPrefs.contains("sim_2")) {
//								if(defaultPrefs.getString("sim_2", "").equals(deviceId)==false) {
//									defaultPrefs.edit().putString("sim_2", deviceId).commit();
//								}
//							}
//							else {
//								defaultPrefs.edit().putString("sim_2", deviceId).commit();
//								lastUnlocked = pkgName;
//								this.finish();
//							}
						}
						if(service.length()>0) {
							switch (service) {
							case "wifi":
								WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
								if(state==0) {
									WifiStateReceiver.setStatus(true);
									wifiManager.setWifiEnabled(true);
								}
								else {
									WifiStateReceiver.setStatus(false);
									wifiManager.setWifiEnabled(false);
								}
								break;
							case "bluetooth":
								if(state==0) {
									BluetoothStateReceiver.setStatus(true);
									BluetoothAdapter.getDefaultAdapter().enable();
								}
								else {
									BluetoothStateReceiver.setStatus(false);
									BluetoothAdapter.getDefaultAdapter().disable();
								}
								break;
							case "mobile":
								if(state==0) {
									MobileDataStateReceiver.setStatus(true);
									try {
										MobileDataStateReceiver.changeMobileDataStatus(getApplicationContext(), true);
									} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException
											| IllegalArgumentException | NoSuchMethodException
											| InvocationTargetException e) {
										e.printStackTrace();
									}
								}
								else {
									MobileDataStateReceiver.setStatus(false);
									try {
										MobileDataStateReceiver.changeMobileDataStatus(getApplicationContext(), false);
									} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException
											| IllegalArgumentException | NoSuchMethodException
											| InvocationTargetException e) {
										e.printStackTrace();
									}
								}
								break;
							default:
								break;
							}
							
						}
					}
					lastUnlocked = pkgName;
					
					this.finish();
				}
			}

		}
	}
	
	private boolean isPasswordCreated() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM password", null);
		if (cursor.moveToNext()) {
			isFirstAttempt = false;
			isSecondAttempt = false;

			cursor.close();
			db.close();
			return true;
		} else {
			isFirstAttempt = true;
			isSecondAttempt = false;

			btnPass[10].setClickable(false);

			cursor.close();
			db.close();
			return false;
		}
	}

	private void savePassword() {

		byte[] hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(passwordConfirm.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		ContentValues cv = new ContentValues();
		if (hash != null) {
			cv.put("password", hash);
		}
		if (!isPasswordCreated()) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.insert("password", null, cv);
			db.close();
		} else {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String[] args = { "1" };
			db.update("password", cv, "_id=?", args);
			db.close();
		}

	}

	private boolean isPasswordCorrect(String password) {
		boolean isCorrect = false;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes("UTF-8"));

			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM password", null);

			cursor.moveToNext();
			if (Arrays.equals(hash, cursor.getBlob(1))) {
				etPassword.setText("");
				isCorrect = true;
			}
			cursor.close();
			db.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return isCorrect;
	}
}
