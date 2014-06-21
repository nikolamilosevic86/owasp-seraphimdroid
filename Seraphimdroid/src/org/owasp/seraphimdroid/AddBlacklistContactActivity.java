package org.owasp.seraphimdroid;

import java.util.ArrayList;
import java.util.List;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddBlacklistContactActivity extends Activity {

	private Button btnAdd, btnCancel;
	private EditText etContactName, etContactNumber;
	private ImageView imgAddFromContacts;

	private final int PICK_CONTACT = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blacklist_add_dialog);

		// Initializing views
		initViews();
		setTitle("Add Number");
		this.setFinishOnTouchOutside(false);
	}

	private void initViews() {

		btnAdd = (Button) findViewById(R.id.btn_blacklist_add);
		btnAdd.setTag("add");
		btnCancel = (Button) findViewById(R.id.btn_blacklist_cancel);
		btnCancel.setTag("cancel");
		// etContactName = (EditText) findViewById(R.id.et_contact_name);
		etContactNumber = (EditText) findViewById(R.id.et_contact_number);
		imgAddFromContacts = (ImageView) findViewById(R.id.img_add_from_contacts);
		imgAddFromContacts.setTag("add_from_contacts");

		AddContactToBlacklistListener listener = new AddContactToBlacklistListener();
		btnAdd.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		imgAddFromContacts.setOnClickListener(listener);
	}

	private void addToBlacklist() {
		String number = etContactNumber.getText().toString();
		if (number != null && !number.equals("")) {
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			// db.execSQL(DatabaseHelper.createBlacklistTable);
			ContentValues cv = new ContentValues();
			cv.put("number", number);
			db.insert(DatabaseHelper.TABLE_BLACKLIST, null, cv);
			if (db != null)
				db.close();
			if (dbHelper != null)
				dbHelper.close();

		} else {
			Toast.makeText(this, "Number can't be empty", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PICK_CONTACT:
				Cursor cursor = null;
				int phoneIdx = 0;
				String phoneNumber = "";
				List<String> allNumbers = new ArrayList<String>();
				try {
					Uri result = data.getData();
					String id = result.getLastPathSegment();
					cursor = getContentResolver().query(Phone.CONTENT_URI,
							null, Phone.CONTACT_ID + "=?", new String[] { id },
							null);
					phoneIdx = cursor.getColumnIndex(Phone.DATA);
					if (cursor.moveToFirst()) {
						while (cursor.isAfterLast() == false) {
							phoneNumber = cursor.getString(phoneIdx);
							allNumbers.add(phoneNumber);
							cursor.moveToNext();
						}
					} else {

					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (cursor != null) {
						cursor.close();
					}

					final CharSequence[] items = allNumbers
							.toArray(new String[allNumbers.size()]);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Choose a number");
					builder.setItems(items,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									String selectedNumber = items[position]
											.toString();
									selectedNumber = selectedNumber.replace(
											"-", "");
									selectedNumber = removeSpaces(selectedNumber);
									etContactNumber.setText(selectedNumber);
								}
							});
					AlertDialog alert = builder.create();
					if (allNumbers.size() > 1) {
						alert.show();
					} else {
						String selectedNumber = phoneNumber.toString();
						selectedNumber = selectedNumber.replace("-", "");
						selectedNumber = removeSpaces(selectedNumber);
						etContactNumber.setText(selectedNumber);
					}

					if (phoneNumber.length() == 0) {
						Toast.makeText(this, "No number for the contact",
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}

		} else {

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private String removeSpaces(String str) {
		StringBuilder withoutSpace = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				continue;
			} else {
				withoutSpace.append(str.charAt(i));
			}
		}

		return withoutSpace.toString();
	}

	private class AddContactToBlacklistListener implements
			android.view.View.OnClickListener {

		@Override
		public void onClick(View view) {
			String tag = (String) view.getTag();
			if (tag.equals("add")) {
				addToBlacklist();
				if (etContactNumber.getText().toString().equals("")
						|| etContactNumber.getText().toString() == null){
					return;
				}
					finish();
			} else if (tag.equals("cancel")) {
				finish();
			} else if (tag.equals("add_from_contacts")) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK);
				contactPickerIntent
						.setType(ContactsContract.Contacts.CONTENT_TYPE);
				startActivityForResult(contactPickerIntent, PICK_CONTACT);
			}
		}

	}

}
