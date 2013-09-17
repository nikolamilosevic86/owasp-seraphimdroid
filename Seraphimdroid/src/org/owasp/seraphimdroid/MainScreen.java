package org.owasp.seraphimdroid;


import org.owasp.seraphimdroid.activitylog.ActivityLog;
import org.owasp.seraphimdroid.data.DatabaseAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainScreen extends Activity {
	
	DatabaseAdapter db;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		context = this;
		
		Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
		
		// Malicious Activity Log button handler
		Button viewLogBtn = (Button)findViewById(R.id.button4);
		viewLogBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(context, ActivityLog.class);
				context.startActivity(myIntent);
			}
		});
	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

}
