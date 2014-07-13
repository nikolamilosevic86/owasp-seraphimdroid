package org.owasp.seraphimdroid;

import java.io.InputStream;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.CallRecepter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SMSLogFragment extends Fragment {

	private ListView lvSMSLogs;
	private DatabaseHelper dbHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		view = inflater.inflate(R.layout.fragment_sms_log, container, false);

		dbHelper = new DatabaseHelper(getActivity());
		lvSMSLogs = (ListView) view.findViewById(R.id.lv_sms_logs);

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String sql = "SELECT * from " + DatabaseHelper.TABLE_SMS_LOGS
				+ " ORDER BY _id DESC";
		Cursor cursor = db.rawQuery(sql, null);

		lvSMSLogs.setAdapter(new SMSLogAdapter(getActivity(), cursor, true));
		
		lvSMSLogs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Bundle extras = (Bundle) view.getTag();

				Intent detailIntent = new Intent(getActivity(),
						LogDetailActivity.class);
				if (extras != null) {
					detailIntent.putExtras(extras);
					startActivity(detailIntent);
				} else {
					Log.d("CallLogFragment", "OnItemClick");
					return;
				}

			}
		});

		return view;
	}
}

class SMSLogAdapter extends CursorAdapter {

	// private Context mContext;

	public SMSLogAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
		// mContext = context;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		TextView tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
		TextView tvReason = (TextView) convertView.findViewById(R.id.tv_reason);
		TextView tvTime = (TextView) convertView.findViewById(R.id.tv_time);
		Button btnRedial = (Button) convertView.findViewById(R.id.btn_redial);
		final String number = cursor.getString(1);
		String reason = cursor.getString(3);
		String time = cursor.getString(2);
		
		Bundle extras = new Bundle();
		extras.putString("NUMBER", number);
		extras.putString("REASON", reason);
		extras.putString("TIME", time);
		convertView.setTag(extras);

		int width = tvReason.getWidth();
		if (width != 0) {
			if (reason.length() > width)
				reason = reason.substring(0, width - 5) + "...";
		} else if (reason != null) {
			if (reason.length() > 45) {
				reason = reason.substring(0, 44) + "...";
			}
		} else {
			reason = "Not defined";
		}

		

		String name = null;
		if (CallRecepter.contactExists(context, number)) {
			// Columns for the query.
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

		if (name != null)
			tvNumber.setText(name);
		else
			tvNumber.setText(number);
		tvTime.setText(time);
		tvReason.setText(reason);
		btnRedial.setVisibility(View.GONE);
	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.log_item, parent, false);
	}
}