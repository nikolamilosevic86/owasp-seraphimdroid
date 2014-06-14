package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

		String sql = "SELECT * from " + DatabaseHelper.TABLE_USSD_LOGS + " ORDER BY _id DESC";
		Cursor cursor = db.rawQuery(sql, null);
		
		lvUSSDLogs.setAdapter(new USSDLogAdapter(getActivity(), cursor, true));

		
		return view;
	}
}

class USSDLogAdapter extends CursorAdapter {

	public USSDLogAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		TextView tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
		TextView tvReason = (TextView) convertView.findViewById(R.id.tv_reason);
		TextView tvTime = (TextView) convertView.findViewById(R.id.tv_time);

		tvNumber.setText(cursor.getString(1));
		tvTime.setText(cursor.getString(2));
		tvReason.setText(cursor.getString(3));
		
	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.log_item, parent, false);
	}
}