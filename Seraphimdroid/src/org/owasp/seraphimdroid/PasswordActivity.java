package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordActivity extends Activity implements OnClickListener {

	public static final String TAG = "Password Activity";
	public static String lastUnlocked = null;

	private Button[] btnPass;
	private Button btnClear;
	private EditText etPassword;
	private TextView tvAlert;

	// Variables used to implement password mechanism
	private boolean isFirstAttempt;
	private boolean isSecondAttempt;
	private DatabaseHelper dbHelper;
	private String passwordTrail, passwordConfirm;
	private String pkgName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);

		// Retrieve the intent.
		Intent rootIntent = getIntent();
		pkgName = rootIntent.getStringExtra("PACKAGE_NAME");

		// Initializing buttons.
		initButtons();

		// Initializing View.
		etPassword = (EditText) findViewById(R.id.et_password);
		tvAlert = (TextView) findViewById(R.id.tv_alert);

		// Initializing other required variables.

		dbHelper = new DatabaseHelper(getApplicationContext());
		passwordConfirm = null;
		passwordTrail = null;

		if (isPasswordCreated()) {
			// Check for password and proceed if correct
		} else {
			etPassword.setHint("Enter 4 digit PIN");
		}

	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	public void onBackPressed() {

		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		am.killBackgroundProcesses(pkgName);

		finish();
		super.onBackPressed();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			am.killBackgroundProcesses(pkgName);

			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			am.killBackgroundProcesses(pkgName);

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
		// TODO Auto-generated method stub
		tvAlert.setVisibility(View.GONE);
		String tag = (String) view.getTag();
		if (tag.equals("enter")) {

			if (isFirstAttempt) {
				if (etPassword.getText().toString().length() < 4) {
					String message = "PIN should not be less than 4 digits";
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
				etPassword.setText("Enter the PIN again.");
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
				}
				tvAlert.setText("");
				tvAlert.setVisibility(View.GONE);
				isSecondAttempt = false;
				passwordConfirm = etPassword.getText().toString();
				savePassword();
				startActivity(new Intent(PasswordActivity.this,
						MainActivity.class));
			} else if (isPasswordCorrect(etPassword.getText().toString())) {
				// startActivity(new Intent(PasswordActivity.this,
				// MainActivity.class));
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
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			am.killBackgroundProcesses(pkgName);
			finish();
		} else {
			int number = Integer.parseInt(tag);
			if (number < 10) {
				etPassword.setText(etPassword.getText().toString() + number);
				Log.d(TAG, etPassword.getText().toString());
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

		ContentValues cv = new ContentValues();
		cv.put("password", passwordConfirm);
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
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM password", null);

		cursor.moveToNext();
		if (cursor.getString(1).equals(password)) {
			etPassword.setText("");
			cursor.close();
			db.close();
			return true;
		}

		return false;
	}
}
