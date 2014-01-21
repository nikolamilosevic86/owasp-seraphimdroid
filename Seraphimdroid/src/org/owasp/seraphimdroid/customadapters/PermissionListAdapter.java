package org.owasp.seraphimdroid.customadapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class PermissionListAdapter extends BaseExpandableListAdapter {

	Context context;
	List<String> groupHeaders;
	HashMap<String, ArrayList<String>> childDataItems;

	public PermissionListAdapter(Context context, List<String> headers,
			HashMap<String, ArrayList<String>> child) {
		this.context = context;
		groupHeaders = headers;
		childDataItems = child;
	}

	@Override
	public String getChild(int groupPosition, int childPosition) {
		return childDataItems.get(groupHeaders.get(groupPosition)).get(
				childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.child_item_layout, parent,
					false);
		}

		TextView tvChild = (TextView) convertView.findViewById(R.id.tvChild);
		tvChild.setText(getChild(groupPosition, childPosition));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (childDataItems.get(groupHeaders.get(groupPosition)) != null) {
			return childDataItems.get(groupHeaders.get(groupPosition)).size();
		}
		return 0;
	}

	@Override
	public String getGroup(int groupPosition) {
		return groupHeaders.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupHeaders.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.group_header_layout,
					parent, false);
		}

		TextView tvHeader = (TextView) convertView.findViewById(R.id.tvHeader);
		String pkgname = getGroup(groupPosition);
		PackageManager temp = context.getPackageManager();
		try {

			// String appname = temp.getPackageInfo(pkgname,
			// PackageManager.GET_META_DATA).applicationInfo.loadLabel(
			// temp).toString();
			String appname = temp
					.getApplicationInfo(pkgname, PackageManager.GET_META_DATA)
					.loadLabel(temp).toString();
			tvHeader.setText(appname);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
