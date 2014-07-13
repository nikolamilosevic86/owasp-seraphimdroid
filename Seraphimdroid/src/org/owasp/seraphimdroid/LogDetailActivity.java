package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.receiver.CallRecepter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.TextView;

public class LogDetailActivity extends Activity {

	private TextView tvName, tvNumber, tvReason, tvTime;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_detail);
		context = this.getApplicationContext();
		
		// Setting properties
		this.setTitle("Log Details");
		this.setFinishOnTouchOutside(true);
		
		
		// Initializing TextViews
		tvName = (TextView) findViewById(R.id.tv_log_name);
		tvNumber = (TextView) findViewById(R.id.tv_log_number);
		tvReason = (TextView) findViewById(R.id.tv_log_reason);
		tvTime = (TextView) findViewById(R.id.tv_log_time);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String number = extras.getString("NUMBER");
			String reason = extras.getString("REASON");
			String time = extras.getString("TIME");
			String name = null;

			if (CallRecepter.contactExists(getApplicationContext(), number)) {
				String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };

				// Encode phone number to build the uri
				Uri contactUri = Uri.withAppendedPath(
						ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
						Uri.encode(number));

				// Placing query
				Cursor c = context.getContentResolver().query(contactUri,
						projection, null, null, null);
				if (c.moveToFirst()) {
					// Get values from the contacts database
					name = c.getString(c
							.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
				}

				c.close();
			}
			
			if(name != null && !name.equals(""))
				tvName.setText(name);
			else
				tvName.setText("<no name>");
			
			tvNumber.setText(number);
			tvReason.setText(reason);
			tvTime.setText(time);
			
			
		}

	}

}
