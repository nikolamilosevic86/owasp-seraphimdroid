package org.owasp.seraphimdroid;

import java.lang.reflect.Method;

import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.CallRecepter;
import org.owasp.seraphimdroid.services.MakeACallService;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
		lvCallLogs.setPadding(17, 0, 10, 10);

		lvCallLogs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				return;
			}
		});

		String sql = "SELECT * from " + DatabaseHelper.TABLE_CALL_LOGS
				+ " ORDER BY _id DESC";
		Cursor cursor = db.rawQuery(sql, null);

		lvCallLogs.setAdapter(new CallLogAdapter(getActivity(), cursor, true));

		return view;
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

	private void placeCall(String number) {
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			Class<?> classTelephony;
			classTelephony = Class.forName(telephonyManager.getClass()
					.getName());
			Method methodGetITelephony = classTelephony
					.getDeclaredMethod("getITelephony");

			methodGetITelephony.setAccessible(true);

			Object telephonyInterface = methodGetITelephony
					.invoke(telephonyManager);

			Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface
					.getClass().getName());
			Method methodPlaceCall = telephonyInterfaceClass.getDeclaredMethod(
					"call", String.class);
			methodPlaceCall.invoke(telephonyInterface, number);

			// methodEndCall.invoke(telephonyInterface);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}