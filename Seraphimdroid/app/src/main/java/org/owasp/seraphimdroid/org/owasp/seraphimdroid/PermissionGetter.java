package org.owasp.seraphimdroid;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.owasp.seraphimdroid.database.DatabaseHelper;

public class PermissionGetter {

	private String maliciousDesc = "No Malicious use known";
	private int weight = 0;
	private Context context;

	// String permission;
	PackageManager packageManager;

	public PermissionGetter(PackageManager packageManager, Context context) {

		this.packageManager = packageManager;
		this.context = context;

	}

	public PermissionData generatePermissionData(String permission) {

		PermissionInfo perInfo = null;
		PerData pd = retrieveData(permission);
		try {
			perInfo = packageManager.getPermissionInfo(permission,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (perInfo != null) {
			String perName = (String) perInfo.loadLabel(packageManager);
			String perDesc = (String) perInfo.loadDescription(packageManager);
			if (perDesc == null || perDesc.equals("")) {
				perDesc = "No Description Available.";
			}
			if (pd != null) {
				weight = pd.weight;
				if (!pd.maliciousUse.equals(""))
					maliciousDesc = pd.maliciousUse;
			}

			PermissionData perData = new PermissionData(permission, perName,
					perDesc, maliciousDesc, weight);
			return perData;
		}
		return null;

	}

	private PerData retrieveData(String permission) {
		// Log.d("PERMISSION GETTER", "Retriving permisison:" + permission);
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.TABLE_PERMISSIONS + " WHERE permission=\'"
				+ permission + "\'", null);

		PerData pd = new PerData();
		if (cursor.moveToFirst()) {
			pd.permission = cursor.getString(1);
			pd.weight = cursor.getInt(2);

			pd.maliciousUse = cursor.getString(3);
		} else {
			pd = null;
		}
		cursor.close();
		db.close();
		dbHelper.close();
		return pd;
	}

	class PerData {
		public String permission;
		public int weight;
		public String maliciousUse;
	}
}
