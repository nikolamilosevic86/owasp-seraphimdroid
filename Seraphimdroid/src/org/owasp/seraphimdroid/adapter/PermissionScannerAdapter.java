package org.owasp.seraphimdroid.adapter;

import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.model.PermissionData;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PermissionScannerAdapter extends BaseExpandableListAdapter {

	// private static final String TAG = "PermissionScannerAdapter";

	private Context context;
	private List<String> groupHeaders;
	private HashMap<String, List<PermissionData>> childItems;
	private PackageManager packageManager;

	public PermissionScannerAdapter(Context context, List<String> grpHeaders,
			HashMap<String, List<PermissionData>> childs) {
		this.context = context;
		this.groupHeaders = grpHeaders;
		this.childItems = childs;
		this.packageManager = context.getPackageManager();
	}

	@Override
	public Object getChild(int groupPos, int childPos) {
		// TODO Auto-generated method stub
		return childItems.get(groupHeaders.get(groupPos)).get(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos) {
		return childPos;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

			convertView = inflater.inflate(R.layout.permission_scanner_item,
					null);
		}

		TextView tvChildTitle = (TextView) convertView
				.findViewById(R.id.permission_scanner_item_title);

		PermissionData perData = (PermissionData) getChild(groupPosition,
				childPosition);

		tvChildTitle.setText(perData.getPermissionName());

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPos) {
		if (childItems.get(groupHeaders.get(groupPos)) != null) {
			return childItems.get(groupHeaders.get(groupPos)).size();
		}

		return 0;
	}

	@Override
	public Object getGroup(int groupPos) {

		return groupHeaders.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		return groupHeaders.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		return groupPos;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.permission_scanner_group,
					null);
		}

		// LinearLayout groupView =
		// (LinearLayout)convertView.findViewById(R.id.group_view);
		//
		// groupView.setBackgroundColor(Color.BLACK);

		ImageView imgIcon = (ImageView) convertView
				.findViewById(R.id.permission_scanner_icon);

		TextView tvGroupTitle = (TextView) convertView
				.findViewById(R.id.permission_scanner_group_title);

		TextView indicator = (TextView) convertView
				.findViewById(R.id.safety_indicator);

		String pkgName = (String) getGroup(groupPosition);

		int color = getColor(groupPosition);

		try {
			ApplicationInfo appInfo = packageManager.getApplicationInfo(
					pkgName, PackageManager.GET_META_DATA);

			imgIcon.setImageDrawable(appInfo.loadIcon(packageManager));

			tvGroupTitle.setText(appInfo.loadLabel(packageManager));
			
			indicator.setBackgroundColor(color);

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPos, int childPos) {
		// TODO Auto-generated method stub
		return true;
	}

	private int getColor(int groupPos) {

		int totalWeight = 0;

		List<PermissionData> permissionList = childItems.get(groupHeaders
				.get(groupPos));

		for (PermissionData perData : permissionList) {
			totalWeight += perData.getWeight();
		}

		if (totalWeight < 5) {
			return Color.GREEN;
		} else if (totalWeight >= 5 && totalWeight < 10) {
			return Color.YELLOW;
		} else if (totalWeight >= 10 && totalWeight < 15) {
			return Color.parseColor("#FFA500");
		} else {
			return Color.RED;
		}

	}
	
	
}