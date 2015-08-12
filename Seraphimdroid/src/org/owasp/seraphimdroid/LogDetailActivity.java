package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.receiver.CallRecepter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

public class LogDetailActivity extends Activity {

	private TextView tvName, tvNumber, tvReason, tvTime, tvSMSContent,
			tvSMSContentLabel, tvSMSType, tvSMSTypeLabel;
	private Context context;

	public static final int CALL_LOG = 0;
	public static final int SMS_LOG = 1;
	public static final int USSD_LOG = 2;

	public static final int SMS_IN = 0;
	public static final int SMS_OUT = 1;

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
		tvSMSContent = (TextView) findViewById(R.id.tv_log_content);
		tvSMSContentLabel = (TextView) findViewById(R.id.tv_log_content_label);
		tvSMSType = (TextView) findViewById(R.id.tv_log_sms_type);
		tvSMSTypeLabel = (TextView) findViewById(R.id.tv_log_sms_type_label);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Integer logType = extras.getInt("LOG_TYPE");
			String number = extras.getString("NUMBER");
			String reason = extras.getString("REASON");
			String time = extras.getString("TIME");
			String name = null;

			switch (logType) {
			case CALL_LOG:
				tvSMSContent.setVisibility(View.GONE);
				tvSMSContentLabel.setVisibility(View.GONE);
				tvSMSType.setVisibility(View.GONE);
				tvSMSTypeLabel.setVisibility(View.GONE);
				break;

			case SMS_LOG:
				tvSMSContent.setVisibility(View.VISIBLE);
				tvSMSContentLabel.setVisibility(View.VISIBLE);
				tvSMSType.setVisibility(View.VISIBLE);
				tvSMSTypeLabel.setVisibility(View.VISIBLE);
				String content = extras.getString("CONTENT");
				Integer type = extras.getInt("TYPE");
				String smsType = "";
				if (type == SMS_IN)
					smsType = "Incoming";
				else if (type == SMS_OUT)
					smsType = "Outgoing";
				tvSMSContent.setText(content);
				tvSMSType.setText(smsType);
				break;

			case USSD_LOG:
				tvSMSContent.setVisibility(View.GONE);
				tvSMSContentLabel.setVisibility(View.GONE);
				tvSMSType.setVisibility(View.GONE);
				tvSMSTypeLabel.setVisibility(View.GONE);
				break;

			default:
				break;
			}

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

			if (name != null && !name.equals(""))
				tvName.setText(name);
			else
				tvName.setText("<no name>");

			tvNumber.setText(number);
			tvReason.setText(reason);
			tvTime.setText(time);

		}

		findViewById(R.id.btn_log_finish).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
						finish();
					}
				});

	}

}
