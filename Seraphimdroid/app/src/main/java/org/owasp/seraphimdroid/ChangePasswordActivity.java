package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChangePasswordActivity extends Activity {

	private EditText etPassword, etConfirmPassword;
	private Button btnChangePassword, btnCancel;
	private TextView tvPasswordAlert, tvConfirmPasswordAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		// Initializing Views.
		initViews();

		getActionBar().setTitle("Change Password");

	}

	private void initViews() {
		tvPasswordAlert = (TextView) findViewById(R.id.tv_password_alert);
		tvConfirmPasswordAlert = (TextView) findViewById(R.id.tv_confirm_password_alert);

		etPassword = (EditText) findViewById(R.id.et_change_password);
		etConfirmPassword = (EditText) findViewById(R.id.et_change_password_confirm);

		btnChangePassword = (Button) findViewById(R.id.btn_change_pass_ok);
		btnChangePassword.setTag("change_password");
		btnCancel = (Button) findViewById(R.id.btn_change_pass_cancel);
		btnCancel.setTag("cancel");

		ChangePasswordButtonListener listener = new ChangePasswordButtonListener();
		btnCancel.setOnClickListener(listener);
		btnChangePassword.setOnClickListener(listener);
	}

	private class ChangePasswordButtonListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			tvPasswordAlert.setVisibility(View.GONE);
			tvConfirmPasswordAlert.setVisibility(View.GONE);
			String tag = (String) view.getTag();
			if (tag.equals("change_password")) {
				String pass = etPassword.getText().toString();
				String passConfirm = etConfirmPassword.getText().toString();
				if (pass.length() < 4) {
					tvPasswordAlert
							.setText("PIN must not be less than 4 digits");
					tvPasswordAlert.setVisibility(View.VISIBLE);
				} else if (!pass.equals(passConfirm)) {
					tvConfirmPasswordAlert.setText("Both PINs must be same");
					tvConfirmPasswordAlert.setVisibility(View.VISIBLE);
				} else if (pass.equals(passConfirm)) {
					changePassword(passConfirm);
					Toast.makeText(ChangePasswordActivity.this,
							"Password changed successfully", Toast.LENGTH_SHORT)
							.show();
					finish();
				}
			} else if (tag.equals("cancel")) {
				tvPasswordAlert.setVisibility(View.GONE);
				tvConfirmPasswordAlert.setVisibility(View.GONE);
				finish();
			}
		}
	}

	private void changePassword(String password) {
		DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		byte[] hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(password.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (hash != null) {

			ContentValues cv = new ContentValues();
			cv.put("password", hash);
			String sql = "DROP TABLE IF EXISTS " + DatabaseHelper.TABLE_PASS;

			db.execSQL(sql);
			db.execSQL(DatabaseHelper.createPasswordTable);
			db.insert(DatabaseHelper.TABLE_PASS, null, cv);
		}
		db.close();
		dbHelper.close();

		// String [] whereArgs = {"1"};
		// db.update(DatabaseHelper.TABLE_PASS, cv, "_id=?", whereArgs);
	}

}
