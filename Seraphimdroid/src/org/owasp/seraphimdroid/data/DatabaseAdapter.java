package org.owasp.seraphimdroid.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DatabaseAdapter {
	//User Keys
	public static final String KEY_ID ="_id";
	

    private static final String TAG = "DatabaseAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String KEY_ALL_ID = "_id";
    private static final String KEY_LOG_ITEM_MESSAGE_ID = "MessageID"; //ID of message (ex. for deleting SMS or MMS)
    private static final String KEY_LOG_ITEM_SOURCE = "Source";//Possible values: SPAM_SMS, SPAM_MMS, USSD, PREMIUM_SMS,PREMIUM_MMS, OUTGOING_CALL, can be changed if needed 
    private static final String KEY_LOG_ITEM_PHONE_NUMBER = "PhoneNumber";//Phone number of call, SMS, MMS, empty if USSD
    private static final String KEY_LOG_ITEM_MESSAGE = "Message";//Executed USSD code, content of SMS, MMS (just text)


    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_ITEM_LOG = 
    		"create table item_log (_id integer primary key autoincrement, MessageID integer, Source text,"+
    		"PhoneNumber text, Message text );";
  
    private static final String DATABASE_NAME = "data";
	    private static final String DATABASE_TABLE_ITEM_LOG = "item_log";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        

        @Override
        public void onCreate(SQLiteDatabase db) {  
            db.execSQL(DATABASE_CREATE_ITEM_LOG);
            Log.d("SQL Query",DATABASE_CREATE_ITEM_LOG);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_ITEM_LOG);         
            onCreate(db);
        }
    }
    
    
    public void onStartBackup() {

    	mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_ITEM_LOG);
    	mDb.execSQL(DATABASE_CREATE_ITEM_LOG);
 
}
    
    public DatabaseAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    public DatabaseAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Adds item to suspicious activity log 
     *  
     * @param  message_id  ID of message returned by system (id of SMS or MMS). This parameter can be later used for deleting or editing of message on system
     * @param  Source Source of activity. It can be SPAM_SMS, SPAM_MMS, USSD, PREMIUM_SMS,PREMIUM_MMS, OUTGOING_CALL, or other defined (document it here and up)
     * @param  PhoneNumber Phone number from which SMS or MMS came, or to which PREMIUM SMS or CALL was sent
     * @param  Message Text message of SMS or MMS    
     * @return     ID in log
     */
    public long addLogItem(int message_id, String Source, String PhoneNumber,String Message)
    {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_LOG_ITEM_MESSAGE_ID, message_id);
    	initialValues.put(KEY_LOG_ITEM_SOURCE, Source);
    	initialValues.put(KEY_LOG_ITEM_PHONE_NUMBER, PhoneNumber);
    	initialValues.put(KEY_LOG_ITEM_MESSAGE, Message);

    	long id = mDb.insert(DATABASE_TABLE_ITEM_LOG,  null, initialValues);
    	return id;
    }
    
    /**
     * Removes item from suspicious activity log 
     *  
     * @param  id  ID in malicious activity log (returned in addItem)
     * @return     Was the operation was successful
     */
    public boolean RemoveLogItem(int id)
    {
    	return mDb.delete(DATABASE_TABLE_ITEM_LOG, KEY_ALL_ID + "=" + id, null) > 0;
    }
    /**
     * Returns Cursor to all Suspicious Activity log items
     *  
     * @return     Cursor to items
     */
    public Cursor GetLogItem()
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE_ITEM_LOG, new String[] {KEY_ALL_ID,KEY_LOG_ITEM_MESSAGE_ID,KEY_LOG_ITEM_SOURCE,KEY_LOG_ITEM_PHONE_NUMBER,KEY_LOG_ITEM_MESSAGE},
    			null, null,
                null, null, null, null);
    	  if(mCursor!=null)
          {
    		  mCursor.moveToFirst();
          } 	
    	
    	return mCursor;
    }
    
    
//    public boolean EditLogItem(LogItem m) {
//
//    	Cursor mCursor = mDb.query(true, DATABASE_TABLE_MEMBER, new String[] {KEY_ALL_ID,KEY_MEMBER_NAME,KEY_MEMBER_ICON,KEY_MEMBER_ISDELETED,KEY_MEMBER_ONLINEID,KEY_MEMBER_MODIFICATION_DATE,KEY_MEMBER_SYNC_DATE},
//    			null, null,
//                null, null, null, null);
//    	long ide = -1;
//    	  if(mCursor!=null)
//          {
//    		  mCursor.moveToFirst();
//          } 	
//    	
//    	while(!mCursor.isAfterLast())
//    	{
//    		
//    		int id =mCursor.getInt(0);
//    		if(id==m.getId())
//    		{
//    			ContentValues args = new ContentValues();
//    	        args.put(KEY_ALL_ID, id);
//    	        args.put(KEY_MEMBER_NAME, m.getName());
//    	        Bitmap yourBitmap=m.getIcon();
//    			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//    			yourBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
//    			byte[] bArray = bos.toByteArray();
//    	        args.put(KEY_MEMBER_ICON, bArray);
//    			ide =mDb.update(DATABASE_TABLE_MEMBER, args, KEY_ALL_ID +'='+id, null);
//    		}
//    		mCursor.moveToNext();
//    	}
//    	if(ide>-1)
//    		return true;
//    	else 	
//    		return false;
//    }


    

}
	



