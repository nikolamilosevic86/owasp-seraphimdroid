package org.owasp.seraphimdroid;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.CallRecepter;
import org.owasp.seraphimdroid.services.MakeACallService;

public class CallLogFragment extends Fragment {

	private ListView lvCallLogs;
	private DatabaseHelper dbHelper;
	private CursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		view = inflater.inflate(R.layout.fragment_call_log, container, false);

		dbHelper = new DatabaseHelper(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		lvCallLogs = (ListView) view.findViewById(R.id.lv_call_logs);
		lvCallLogs.setPadding(17, 0, 10, 10);

		// lvCallLogs.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// return;
		// }
		// });

		String sql = "SELECT * from " + DatabaseHelper.TABLE_CALL_LOGS
				+ " ORDER BY _id DESC";
		Cursor cursor = db.rawQuery(sql, null);
		adapter = new CallLogAdapter(getActivity(), cursor, true);
		lvCallLogs.setAdapter(adapter);

		lvCallLogs.setOnItemClickListener(new OnItemClickListener() {

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

	@Override
	public void onResume() {
adapter.notifyDataSetInvalidated();
		adapter.notifyDataSetChanged();
		super.onResume();
	}

}

class CallLogAdapter extends CursorAdapter {

	private Context mContext;

	public CallLogAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
		mContext = context;
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
		extras.putInt("LOG_TYPE", LogDetailActivity.CALL_LOG);
		extras.putString("NUMBER", number);
		extras.putString("REASON", reason);
		extras.putString("TIME", time);
		convertView.setTag(extras);

		int width = tvReason.getWidth();
		if (width != 0) {
			if (reason.length() > width)
				reason = reason.substring(0, width - 5) + "...";
		} else if (reason != null) {
			if (reason.length() > 30) {
				reason = reason.substring(0, 29) + "...";
			}
		} else {
			reason = "Not defined";
		}

		// if (reason != null) {
		// if (reason.length() > 30) {
		// reason = reason.substring(0, 29) + "...";
		// }
		// } else {
		// reason = "Not defined";
		// }

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

		if (name != null && !name.equals(""))
			tvNumber.setText(name);
		else
			tvNumber.setText(number);
		tvTime.setText(time);
		tvReason.setText(reason);

		btnRedial.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// Call the number blocked;

				// Intent callIntent = new Intent(Intent.ACTION_CALL);
				// callIntent.setData(Uri.parse("tel:" + number));
				// mContext.startActivity(callIntent);
				// MainActivity.shouldReceive = false;

				// placeCall(number);

				Intent callServiceIntent = new Intent(mContext,
						MakeACallService.class);
				callServiceIntent.putExtra("PHONE_NUMBER", number);
				callServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startService(callServiceIntent);
			}
		});

	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.log_item, parent, false);
	}

}