package org.owasp.seraphimdroid.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.model.DrawerItem;

import java.util.List;

public class DrawerAdapter extends BaseAdapter {

	private List<DrawerItem> dataList;
	private Context context;
	private SharedPreferences defaults;

	public DrawerAdapter(Context context, List<DrawerItem> list) {
		this.context = context;
		this.dataList = list;
		defaults = PreferenceManager.getDefaultSharedPreferences(context);

	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return dataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.drawer_row_layout, null,
					false);
		}

		TextView txtItemName = (TextView) convertView
				.findViewById(R.id.drawer_item_name);

		ImageView imgIcon = (ImageView) convertView
				.findViewById(R.id.drawer_icon);

		ImageView imgInfoIcon = (ImageView) convertView
				.findViewById(R.id.img_info_btn);

		boolean isInfoVisible = defaults.getBoolean("info_visible_" + position,
				true);

		if (position > 5 || !isInfoVisible) {
			imgInfoIcon.setVisibility(View.GONE);
			imgInfoIcon.setClickable(false);
		}
		imgInfoIcon.setTag(position);
		imgInfoIcon.setOnClickListener(new InfoButtonListener());

		txtItemName.setText(dataList.get(position).getItemName());
		imgIcon.setImageResource(dataList.get(position).getIconId());

		return convertView;
	}

	private class InfoButtonListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			AlertDialog.Builder info = new AlertDialog.Builder(context);
			info.setTitle("Information");
			info.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			});
			Integer position = (Integer) view.getTag();
			switch (position) {
			case 0:
				info.setMessage(context
						.getString(R.string.info_permission_scanner));
				break;
			case 1:
				info.setMessage(context.getString(R.string.info_settings_scanner));
				break;
			case 2:
				info.setMessage(context.getString(R.string.info_blocked_logs));
				break;
			case 3:
				info.setMessage(context.getString(R.string.info_app_lock));
				break;
			case 4:
				info.setMessage(context.getString(R.string.info_service_lock));
				break;
			case 5:
				info.setMessage(context.getString(R.string.info_geo_fencing));
				break;
			}
			defaults.edit().putBoolean("info_visible_" + position, false)
					.commit();
			info.create().show();
			view.setVisibility(View.GONE);
		}
	}

}
