package org.owasp.seraphimdroid.activitylog;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.data.DatabaseAdapter;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityLog extends Activity {
	
	private ActivityLogAdapter activityLogAdapter;
	private DatabaseAdapter dbAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();
        ListView listView = (ListView) findViewById(R.id.activity_log);
        activityLogAdapter = new ActivityLogAdapter(this,dbAdapter.GetLogItem());
        listView.setAdapter(activityLogAdapter);
        dbAdapter.close();
    }
    
    class ActivityLogAdapter extends CursorAdapter {
    	
    	public ActivityLogAdapter(Context context, Cursor cursor) {
    		super(context, cursor);
    	}

    	@Override
    	public void bindView(View view, Context context, Cursor cursor) {
  
    		TextView sourceTextView = (TextView) view.findViewById(R.id.message);
    		sourceTextView.setText(cursor.getString(4) + " (Message Id:" + cursor.getString(1) + 
    						", Source: " + cursor.getString(2) + 
    						", Phone Number: " + cursor.getString(3) + ")");
    	}

    	@Override
    	public View newView(Context context, Cursor cursor, ViewGroup parent) {
    		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    		View view = inflater.inflate(R.layout.activity_log_item, parent, false);
    		return view;
    	}
    }
}