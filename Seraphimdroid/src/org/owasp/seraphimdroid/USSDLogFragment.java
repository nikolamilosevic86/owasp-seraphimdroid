package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.services.MakeACallService;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class USSDLogFragment extends Fragment {

	private DatabaseHelper dbHelper;
	private ListView lvUSSDLogs;

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

		lvUSSDLogs.setAdapter(new USSDLogAdapter(getActivity(), cursor, true));

		return view;
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

		String reason = cursor.getString(3);
		if (reason != null) {
			if (reason.length() > 30) {
				reason = reason.substring(0, 29) + "...";
			}
		} else {
			reason = "Not defined";
		}
		final String number = cursor.getString(1);
		tvNumber.setText(number);
		tvTime.setText(cursor.getString(2));
		tvReason.setText(reason);
		btnRedial.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
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