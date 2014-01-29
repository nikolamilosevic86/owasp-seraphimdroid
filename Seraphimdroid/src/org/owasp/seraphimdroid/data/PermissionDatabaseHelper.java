package org.owasp.seraphimdroid.data;

import org.owasp.seraphimdroid.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PermissionDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "permissions_database";
	private static final int VERSION = 1;

	private static final String TABLE_NAME_PERMISSIONS = "permissions_data";

	private static final String KEY_PERMISSION_ = "Permission";
	private static final String KEY_PERMISSION_NAME = "Permission_name";
	private static final String KEY_PERMISSION_DESCRIPTION = "Description";
	private static final String KEY_PERMISSION_MALICIOUS_USE = "Malicious_use";
	private static final String KEY_PERMISSION_WEIGHT = "Weight";

	private static final String CREATE_TABLE_PERMISSIONS = "create table permissions_data"
			+ "(_id integer primary key autoincrement, Permission text not null, "
			+ "Permission_name text, Description text, Malicious_use text, Weight integer)";

	private Context context;
	
	public PermissionDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_PERMISSIONS);
		initialize(context, db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion > oldVersion){
			db.execSQL("drop table if exist " + TABLE_NAME_PERMISSIONS);
			onCreate(db);
		}
	}
	
	public void initialize(Context context, SQLiteDatabase db){
		
		Resources res = context.getResources();
		String [] permissions = res.getStringArray(R.array.permissions);
		String [] permissionNames = res.getStringArray(R.array.permission_names);
		String [] descriptions = res.getStringArray(R.array.descriptions);
		
		ContentValues cv = new ContentValues();
		for(int i = 0; i < permissions.length; i++){
			cv.put(KEY_PERMISSION_, permissions[i]);
			cv.put(KEY_PERMISSION_NAME, permissionNames[i]);
			cv.put(KEY_PERMISSION_DESCRIPTION, descriptions[i]);
			cv.put(KEY_PERMISSION_MALICIOUS_USE, "No Malicious Use");
			cv.put(KEY_PERMISSION_WEIGHT, 0);
			db.insert(TABLE_NAME_PERMISSIONS, KEY_PERMISSION_MALICIOUS_USE, cv);
		}
		
	}

}
