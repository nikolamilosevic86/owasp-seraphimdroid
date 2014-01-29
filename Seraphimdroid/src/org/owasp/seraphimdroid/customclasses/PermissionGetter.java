package org.owasp.seraphimdroid.customclasses;

import org.owasp.seraphimdroid.MainScreen;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class PermissionGetter extends AsyncTask<String, Integer, PermissionData> {

	@Override
	protected PermissionData doInBackground(String... params) {
		String permission = params[0];
		SQLiteDatabase db = MainScreen.helper.getReadableDatabase();
		String sql = "Select * from permissions_data where Permission=\'"
				+ permission + "\'";

		Cursor cursor = db.rawQuery(sql, null);

		// Creating the object required.
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			PermissionData pd = new PermissionData(cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getInt(5));
			cursor.close();

			try {
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return pd;
		}
		return new PermissionData(permission, permission,
				"No Description Available", "No Description Available", 0);

	}

}
