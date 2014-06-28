package org.owasp.seraphimdroid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final static String DB_NAME = "APP_DATABASE";
	private final static int VERSION = 1;

	public final static String TABLE_CALL_LOGS = "call_logs";
	public final static String TABLE_SMS_LOGS = "sms_logs";
	public final static String TABLE_USSD_LOGS = "ussd_logs";
	public static final String TABLE_LOCKS = "locks";
	public static final String TABLE_PASS = "password";
	public final static String TABLE_BLACKLIST = "blacklist";

	private static final String createCallTable = "CREATE TABLE IF NOT EXISTS call_logs (_id integer primary key autoincrement, phone_number TEXT , time TEXT, reason TEXT) ";
	private static final String createUSSDTable = "CREATE TABLE IF NOT EXISTS ussd_logs (_id integer primary key autoincrement, phone_number TEXT , time TEXT, reason TEXT) ";
	private static final String createSMSTable = "CREATE TABLE IF NOT EXISTS sms_logs (_id integer primary key autoincrement, phone_number TEXT , time TEXT, reason TEXT) ";
	public static final String createPasswordTable = "CREATE TABLE IF NOT EXISTS password (_id integer primary key autoincrement, password BLOB)";
	private static final String createLocksTable = "CREATE TABLE IF NOT EXISTS locks (_id INTEGER primary key autoincrement, package_name TEXT)";
	public static final String createBlacklistTable = "CREATE TABLE IF NOT EXISTS blacklist (_id INTEGER PRIMARY KEY AUTOINCREMENT, number TEXT NOT NULL)";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createCallTable);
		db.execSQL(createSMSTable);
		db.execSQL(createUSSDTable);
		db.execSQL(createPasswordTable);
		db.execSQL(createLocksTable);
		db.execSQL(createBlacklistTable);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS call_logs");
		db.execSQL("DROP TABLE IF EXISTS ussd_logs");
		db.execSQL("DROP TABLE IF EXISTS sms_logs");
		db.execSQL("DROP TABLE IF EXISTS locks");
		db.execSQL("DROP TABLE IF EXISTS blacklist");

		this.onCreate(db);

	}

}
