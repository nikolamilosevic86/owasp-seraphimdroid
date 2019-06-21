package org.owasp.seraphimdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import org.owasp.seraphimdroid.adapter.BlacklistAdapter;
import org.owasp.seraphimdroid.helper.DatabaseHelper;

public class BlacklistActivity extends Activity {

	private ListView lvBlacklist;
	private BlacklistAdapter adapter;

	// private static final int CURSOR_LOADER_ID = 111;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_call_log);

		lvBlacklist = (ListView) findViewById(R.id.lv_call_logs);
		lvBlacklist.setPadding(15, 0, 10, 10);

		getActionBar().setTitle("Blacklist Contacts");
		getActionBar().setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);

		DatabaseHelper dbHelper = new DatabaseHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.TABLE_BLACKLIST, null);

		adapter = new BlacklistAdapter(this, cursor, false);
		lvBlacklist.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mInflater = this.getMenuInflater();
		mInflater.inflate(R.menu.blacklist_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		adapter.notifyDataSetInvalidated();
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.TABLE_BLACKLIST, null);

		adapter = new BlacklistAdapter(this, cursor, false);
		lvBlacklist.setAdapter(adapter);

		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_contact) {
			Intent addContactIntent = new Intent(this,
					AddBlacklistContactActivity.class);
			startActivity(addContactIntent);
		}

		return super.onOptionsItemSelected(item);
	}

}
