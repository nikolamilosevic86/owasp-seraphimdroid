package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.activitylog.ActivityLog;
import org.owasp.seraphimdroid.data.DatabaseAdapter;
import org.owasp.seraphimdroid.data.PermissionDatabaseHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainScreen extends Activity {
	
	public static PermissionDatabaseHelper helper;
	DatabaseAdapter db;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		context = this;
		helper = new PermissionDatabaseHelper(getApplicationContext());

		Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();

		// Malicious Activity Log button handler
		Button viewLogBtn = (Button) findViewById(R.id.button4);
		viewLogBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(context, ActivityLog.class);
				context.startActivity(myIntent);
			}
		});
		
		//Permission Scanner button handler
		Button btnPermissionScanner = (Button) findViewById(R.id.btnPermissionScanner);
		btnPermissionScanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PermissionScanner.class);
				context.startActivity(intent);
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
