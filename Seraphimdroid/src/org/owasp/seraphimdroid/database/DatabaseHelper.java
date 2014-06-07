package org.owasp.seraphimdroid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final static String DB_NAME = "APP_DATABASE";
	private final static int VERSION = 1;

	private static final String createCallTable = "CREATE TABLE IF NOT EXISTS call_logs (_id integer primary key autoincrement, phone_number TEXT , time TEXT, reason TEXT) ";
	private static final String createUSSDTable = "CREATE TABLE IF NOT EXISTS ussd_logs (_id integer primary key autoincrement, phone_number TEXT , time TEXT, reason TEXT) ";
	private static final String createSMSTable = "CREATE TABLE IF NOT EXISTS sms_logs (_id integer primary key autoincrement, phone_number TEXT , time TEXT, reason TEXT) ";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createCallTable);
		db.execSQL(createSMSTable);
		db.execSQL(createUSSDTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS call_logs");
		db.execSQL("DROP TABLE IF EXISTS ussd_logs");
		db.execSQL("DROP TABLE IF EXISTS sms_logs");
		
		this.onCreate(db);

	}

}
