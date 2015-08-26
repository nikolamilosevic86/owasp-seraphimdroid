package org.owasp.seraphimdroid.adapter;

import java.util.ArrayList;
import java.util.List;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.services.ServicesLockService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ServicesLockerAdapter extends BaseAdapter {

	Context context;
	private List<String> lockedServices;
	
	public ServicesLockerAdapter(Context context) {
		this.context = context;
		lockedServices = new ArrayList<String>(10);
		generateLockedServices();
	}
	
	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public Object getItem(int position) {
		return labels[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.app_locker_item, parent, false);
		}

		final String service = (String) getItem(position);
		
		// Initializing Views.
		TextView tvLabel = (TextView) view
				.findViewById(R.id.tv_app_locker_label);
		tvLabel.setText(labels[position]);
		TextView tvAppType = (TextView) view
				.findViewById(R.id.tv_app_locker_app_type);
		tvAppType.setText(descriptions[position]);
		ImageView imgIcon = (ImageView) view.findViewById(R.id.app_locker_icon);
		imgIcon.setBackgroundResource(icons[position]);
		
		ToggleButton tb = (ToggleButton) view.findViewById(R.id.tb_is_locked);

		if (lockedServices.contains(service)) {
			tb.setChecked(true);
			tb.setText("Locked");
		} else {
			tb.setChecked(false);
			tb.setText("Unlocked");
		}
		
		tb.setTag(service);
		tb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ToggleButton tb = (ToggleButton) view;
				String tag = (String) tb.getTag();

				DatabaseHelper dbHelper = new DatabaseHelper(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Cursor cursor = db.rawQuery(
						"SELECT * FROM services WHERE service_name=\'" + tag
								+ "\'", null);

				if (tag.equals(service)) {
					if (tb.isChecked()) {

						if (!cursor.moveToNext()) {
							ContentValues cv = new ContentValues();
							cv.put("service_name", tag);
							db.insert(DatabaseHelper.TABLE_SERVICES_LOCKS, null, cv);
							Toast.makeText(context, "Locked: " + service,
									Toast.LENGTH_SHORT).show();
							switch (position) {
							case 0:
								ServicesLockService.registerWifiListener();
								break;
							case 1:
								ServicesLockService.registerBluetoothListener();
								break;
							case 2:
								ServicesLockService.registerMobileDataListener();
								break;
							default:
								break;
							}
						}
					} else {

						if (cursor.moveToNext()) {
							String[] whereArgs = { tag };
							db.delete(DatabaseHelper.TABLE_SERVICES_LOCKS,
									"service_name=?", whereArgs);
							Toast.makeText(context, "Unlocked: " + service,
									Toast.LENGTH_SHORT).show();
							switch (position) {
							case 0:
								ServicesLockService.unregisterWifiListener();;
								break;
							case 1:
								ServicesLockService.unregisterBluetoothListener();
								break;
							case 3:
								ServicesLockService.unregisterMobileDataListener();
								break;
							default:
								break;
							}
						}

					}
				}
				cursor.close();
				db.close();
				dbHelper.close();
				generateLockedServices();
			}
		});

		return view;
	}

	private void generateLockedServices() {
		lockedServices.clear();
		DatabaseHelper dbHelper = new DatabaseHelper(this.context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String[] selections = { "service_name" };
		Cursor cursor = db.query(DatabaseHelper.TABLE_SERVICES_LOCKS, selections, null,
				null, null, null, null);
		while (cursor.moveToNext()) {
			lockedServices.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		dbHelper.close();
		notifyDataSetChanged();
	}
	
	String[] labels = {
			"WiFi",
			"Bluetooth",
			"Mobile Network Data"
	};
	
	String[] descriptions = {
			"Prevent turning on/off WiFi",
			"Prevent turning on/off Bluetooth",
			"Prevent turning on/off Mobile Data"
	};
	
	int[] icons = {
			R.drawable.icon_wifi,
			R.drawable.icon_bluetooth,
			R.drawable.icon_data
	};
	
}
