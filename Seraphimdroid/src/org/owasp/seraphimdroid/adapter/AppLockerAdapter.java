package org.owasp.seraphimdroid.adapter;

import java.util.ArrayList;
import java.util.List;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AppLockerAdapter extends BaseAdapter {

	protected static final String String = null;
	private List<String> lockedApps;
	private List<String> appList;
	private Context context;

	public AppLockerAdapter(Context context, List<String> appList) {
		this.context = context;
		this.appList = appList;
		lockedApps = new ArrayList<String>(10);
		generateLockedApps();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return appList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.app_locker_item, parent, false);
		}

		final String pkgName = (String) getItem(position);

		// Initializing Views.
		TextView tvLabel = (TextView) view
				.findViewById(R.id.tv_app_locker_label);
		ImageView imgIcon = (ImageView) view.findViewById(R.id.app_locker_icon);
		ToggleButton tb = (ToggleButton) view.findViewById(R.id.tb_is_locked);

		// Setting properties

		try {
			PackageManager pm = context.getPackageManager();
			ApplicationInfo appInfo = pm.getApplicationInfo(pkgName,
					PackageManager.GET_META_DATA);
			final String appName = appInfo.loadLabel(pm).toString();

			tvLabel.setText(appInfo.loadLabel(pm));
			imgIcon.setImageDrawable(appInfo.loadIcon(pm));

			if (lockedApps.contains(pkgName)) {
				tb.setChecked(true);
			} else {
				tb.setChecked(false);
			}
			tb.setTag(pkgName);
			tb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					ToggleButton tb = (ToggleButton) view;
					String tag = (String) tb.getTag();

					DatabaseHelper dbHelper = new DatabaseHelper(context);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					Cursor cursor = db.rawQuery(
							"SELECT * FROM locks WHERE package_name=\'" + tag
									+ "\'", null);

					if (tag.equals(pkgName)) {
						if (tb.isChecked()) {

							if (!cursor.moveToNext()) {
								ContentValues cv = new ContentValues();
								cv.put("package_name", tag);
								db.insert(DatabaseHelper.TABLE_LOCKS, null, cv);

								Toast.makeText(context, "Locked: " + appName,
										Toast.LENGTH_SHORT).show();
							}
						} else {

							if (cursor.moveToNext()) {
								String[] whereArgs = { tag };
								db.delete(DatabaseHelper.TABLE_LOCKS,
										"package_name=?", whereArgs);
								Toast.makeText(context, "Unlocked: " + appName,
										Toast.LENGTH_SHORT).show();
							}

						}
					}
					generateLockedApps();
					cursor.close();
					db.close();
					dbHelper.close();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return view;
	}

	private void generateLockedApps() {
		lockedApps.clear();
		DatabaseHelper dbHelper = new DatabaseHelper(this.context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String[] selections = { "package_name" };
		Cursor cursor = db.query(DatabaseHelper.TABLE_LOCKS, selections, null,
				null, null, null, null);
		while (cursor.moveToNext()) {
			lockedApps.add(cursor.getString(0));
		}
		notifyDataSetChanged();
		cursor.close();
		db.close();
		dbHelper.close();
	}

}
