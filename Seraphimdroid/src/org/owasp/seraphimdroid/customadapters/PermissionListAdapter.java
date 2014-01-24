package org.owasp.seraphimdroid.customadapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.customclasses.PermissionData;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PermissionListAdapter extends BaseExpandableListAdapter {

	Context context;
	List<String> groupHeaders;
	HashMap<String, ArrayList<PermissionData>> childDataItems;

	public PermissionListAdapter(Context context, List<String> headers,
			HashMap<String, ArrayList<PermissionData>> child) {
		this.context = context;
		groupHeaders = headers;
		childDataItems = child;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
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
		PermissionData permission = (PermissionData) getChild(groupPosition,
				childPosition);

		tvChild.setText(permission.getPermission());
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

		// Initializing childViews.
		RelativeLayout rl = (RelativeLayout) convertView
				.findViewById(R.id.rlGroupHeader);
		TextView tvHeader = (TextView) convertView.findViewById(R.id.tvHeader);
		ImageView ivAppIcon = (ImageView) convertView
				.findViewById(R.id.ivAppIcon);

		String pkgname = getGroup(groupPosition);
		PackageManager temp = context.getPackageManager();
		int weightSum = getWeightSum(groupPosition);

		try {
			// Settings name of the app.
			String appname = temp
					.getApplicationInfo(pkgname, PackageManager.GET_META_DATA)
					.loadLabel(temp).toString();
			tvHeader.setText(appname + ": " + weightSum);

			// Setting the icon for the app.
			Drawable icon = temp.getApplicationInfo(pkgname,
					PackageManager.GET_META_DATA).loadIcon(temp);
			ivAppIcon.setImageDrawable(icon);

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		convertView.setTag(pkgname);

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

	public int getWeightSum(int groupPosition) {
		int childCount = getChildrenCount(groupPosition);
		int weightSum = 0;
		for (int count = 0; count != childCount; count++) {
			weightSum += ((PermissionData) getChild(groupPosition, count))
					.getWeight();
		}
		return weightSum;
	}
}
