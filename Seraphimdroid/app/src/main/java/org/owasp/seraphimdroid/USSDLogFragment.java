package org.owasp.seraphimdroid;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
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
import org.owasp.seraphimdroid.services.MakeACallService;

public class USSDLogFragment extends Fragment {

	private DatabaseHelper dbHelper;
	private ListView lvUSSDLogs;
	private CursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = null;
		view = inflater.inflate(R.layout.fragment_ussd_log, container, false);

		dbHelper = new DatabaseHelper(getActivity());
		lvUSSDLogs = (ListView) view.findViewById(R.id.lv_ussd_logs);

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String sql = "SELECT * from " + DatabaseHelper.TABLE_USSD_LOGS
				+ " ORDER BY _id DESC";
		Cursor cursor = db.rawQuery(sql, null);

		adapter = new USSDLogAdapter(getActivity(), cursor, true);
		lvUSSDLogs.setAdapter(adapter);

		lvUSSDLogs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
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

class USSDLogAdapter extends CursorAdapter {

	private Context mContext;

	public USSDLogAdapter(Context context, Cursor c, boolean autoRequery) {
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

		String number = cursor.getString(1);
		String reason = cursor.getString(3);
		String time = cursor.getString(2);

		Bundle extras = new Bundle();
		extras.putInt("LOG_TYPE", LogDetailActivity.USSD_LOG);
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

		final String ussd = number.replaceAll("#", Uri.encode("#"));
		tvNumber.setText(number);
		tvTime.setText(time);
		tvReason.setText(reason);
		btnRedial.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent callServiceIntent = new Intent(mContext,
						MakeACallService.class);
				callServiceIntent.putExtra("PHONE_NUMBER", ussd);
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