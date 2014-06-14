package org.owasp.seraphimdroid.adapter;

import java.util.List;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.model.DrawerItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class DrawerAdapter extends BaseAdapter {

	private List<DrawerItem> dataList;
	private Context context;

	public DrawerAdapter(Context context, List<DrawerItem> list) {
		this.context = context;
		this.dataList = list;
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

		txtItemName.setText(dataList.get(position).getItemName());
		imgIcon.setImageResource(dataList.get(position).getIconId());

		return convertView;
	}

}
