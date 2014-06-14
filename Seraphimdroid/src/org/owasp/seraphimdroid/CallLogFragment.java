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

public class CallLogFragment extends Fragment {

	private ListView lvCallLogs;
	private DatabaseHelper dbHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		view = inflater.inflate(R.layout.fragment_call_log, container, false);

		dbHelper = new DatabaseHelper(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		lvCallLogs = (ListView) view.findViewById(R.id.lv_call_logs);
		String sql = "SELECT * from " + DatabaseHelper.TABLE_CALL_LOGS
				+ " ORDER BY _id DESC";
		Cursor cursor = db.rawQuery(sql, null);

		lvCallLogs.setAdapter(new CallLogAdapter(getActivity(), cursor, true));

		return view;
	}
	
	

}

class CallLogAdapter extends CursorAdapter {

	public CallLogAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		TextView tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
		TextView tvReason = (TextView) convertView.findViewById(R.id.tv_reason);
		TextView tvTime = (TextView) convertView.findViewById(R.id.tv_time);

		String reason = cursor.getString(3);

		if (reason.length() > 30) {
			reason = reason.substring(0, 29) + "...";
		}

		tvNumber.setText(cursor.getString(1));
		tvTime.setText(cursor.getString(2));
		tvReason.setText(reason);

	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.log_item, parent, false);
	}
}