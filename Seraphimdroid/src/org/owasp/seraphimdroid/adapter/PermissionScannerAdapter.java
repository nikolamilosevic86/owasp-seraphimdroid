package org.owasp.seraphimdroid.adapter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.model.PermissionData;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class PermissionScannerAdapter extends BaseExpandableListAdapter {

	private Context context;
	private List<String> groupHeaders;
	private HashMap<String, List<PermissionData>> childItems;
	private PackageManager packageManager;
	private SMO svmModel;
	int[] appClassificationColors;
	String TAG = "PermissionsAdapter";
	HashMap<String, Integer> map;
	
	
	public PermissionScannerAdapter(Context context, List<String> grpHeaders,
			HashMap<String, List<PermissionData>> childs) {
		Log.d(TAG, "Created");
		this.context = context;
		this.groupHeaders = grpHeaders;
		this.childItems = childs;
		this.packageManager = context.getPackageManager();
		this.appClassificationColors = new int[groupHeaders.size()];
		map = new HashMap<String, Integer>();
		for(int i=0; i<permissions.length; i++) {
			map.put(permissions[i], i);
		}
		try {
			AssetManager assetManager = context.getAssets();
			InputStream stream = assetManager.open("SMOWeka366.model");
			svmModel = (SMO) SerializationHelper.read(stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<groupHeaders.size(); i++) {
			classifyPermissions(i);
		}	
	}

	@Override
	public Object getChild(int groupPos, int childPos) {
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
//			e.printStackTrace();
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPos, int childPos) {
		return true;
	}

	private void classifyPermissions(int groupPos) {
		List<PermissionData> permissionList = childItems.get(groupHeaders
				.get(groupPos));
		
		FastVector fvWekaAttributes = new FastVector(permissions.length+1);
		
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("goodware");
		fvClassVal.addElement("malware");
		Attribute ClassAttribute  = new Attribute("class", fvClassVal);
		fvWekaAttributes.addElement(ClassAttribute);
		
		for(int i=0; i<permissions.length; i++) {
			Attribute permission = new Attribute(permissions[i], 0);
			fvWekaAttributes.addElement(permission);
		}
		
		Instances Instances = new Instances("Rel", fvWekaAttributes, 0);

		Instance iExample = new Instance(permissions.length+1);
		for (PermissionData perData : permissionList) {
			String permission = perData.getPermission().replace("android.permission.","");
			if(map.containsKey(permission)) {
				iExample.setValue((Attribute) fvWekaAttributes.elementAt(map.get(permission)+1), 1);
			}
		}

		Instances.add(iExample);
		Instances.setClassIndex(0);
		
		StringToWordVector filter = new StringToWordVector();
		try {
			filter.setInputFormat(Instances);
			filter.input(Instances.instance(0));
			filter.batchFinished();
			Instances ins = Filter.useFilter(Instances, filter);
			double prediction = svmModel.classifyInstance(ins.firstInstance());
			if(prediction==1.0) {
				appClassificationColors[groupPos] = Color.RED;
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		appClassificationColors[groupPos] = Color.GREEN;
	}
	
	private int getColor(int groupPos) {
		return appClassificationColors[groupPos];
	}
	
	String[] permissions = {
			"ACCESS_COARSE_LOCATION","ACCESS_COARSE_UPDATES","ACCESS_FINE_LOCATION","ACCESS_FM_RECEIVER","ACCESS_LOCATION_EXTRA_COMMANDS","ACCESS_MOCK_LOCATION","ACCESS_NETWORK_STATE","ACCESS_WIFI_STATE","ACCESS_WIMAX_STATE","AUTHENTICATE_ACCOUNTS","BATTERY_STATS","BIND_ACCESSIBILITY_SERVICE","BIND_APPWIDGET","BIND_DEVICE_ADMIN","BIND_INPUT_METHOD","BIND_REMOTEVIEWS","BIND_WALLPAPER","BLUETOOTH","BLUETOOTH_ADMIN","BROADCAST_SMS","BROADCAST_STICKY","BROADCAST_WAP_PUSH","CALL_PHONE","CALL_PRIVILEGED","CAMERA","CHANGE_COMPONENT_ENABLED_STATE","CHANGE_CONFIGURATION","CHANGE_NETWORK_STATE","CHANGE_WIFI_MULTICAST_STATE","CHANGE_WIFI_STATE","CHANGE_WIMAX_STATE","DEVICE_POWER","DISABLE_KEYGUARD","EXPAND_STATUS_BAR","FLASHLIGHT","FM_RADIO_RECEIVER","FM_RADIO_TRANSMITTER","GET_ACCOUNTS","GET_PACKAGE_SIZE","GET_TASKS","GLOBAL_SEARCH","HARDWARE_TEST","INSTALL_PACKAGES","INTERACT_ACROSS_USERS","INTERACT_ACROSS_USERS_FULL","INTERNET","KILL_BACKGROUND_PROCESSES","MANAGE_ACCOUNTS","MANAGE_USB","MODIFY_AUDIO_SETTINGS","MODIFY_PHONE_STATE","MOUNT_UNMOUNT_FILESYSTEMS","NETWORK","NFC","PERSISTENT_ACTIVITY","PROCESS_OUTGOING_CALLS","RAISED_THREAD_PRIORITY","READ_CALENDAR","READ_CALL_LOG","READ_CONTACTS","READ_EXTERNAL_STORAGE","READ_LOGS","READ_PHONE_STATE","READ_PROFILE","READ_SECURE_SETTINGS","READ_SMS","READ_SYNC_SETTINGS","READ_SYNC_STATS","READ_TASKS","READ_USER_DICTIONARY","RECEIVE_BOOT_COMPLETED","RECEIVE_MMS","RECEIVE_SMS","RECORD_AUDIO","RESTART_PACKAGES","SEND_SMS","SET_ACTIVITY_WATCHER","SET_DEBUG_APP","SET_ORIENTATION","SET_PREFERRED_APPLICATIONS","SET_WALLPAPER","SET_WALLPAPER_HINTS","STORAGE","SYSTEM_ALERT_WINDOW","SYSTEM_TOOLS","UPDATE_DEVICE_STATS","USEFMRADIO","USES_POLICY_FORCE_LOCK","USE_CREDENTIALS","VIBRATE","WAKE_LOCK","WRITE_APN_SETTINGS","WRITE_CALENDAR","WRITE_CALL_LOG","WRITE_CONTACTS","WRITE_EXTERNAL_STORAGE","WRITE_PROFILE","WRITE_SECURE_SETTINGS","WRITE_SETTINGS","WRITE_SMS","WRITE_SYNC_SETTINGS","WRITE_TASKS","WRITE_USER_DICTIONARY","ACCESS_CACHE_FILESYSTEM","ACCESS_GPS","ACCESS_LOCATION","ADD_SYSTEM_SERVICE","BACKUP","CLEAR_APP_CACHE","CLEAR_APP_USER_DATA","DELETE_CACHE_FILES","DELETE_PACKAGES","DIAGNOSTIC","INTERNAL_SYSTEM_WINDOW","PERMISSION_NAME","PROCESS_CALL","PROCESS_INCOMING_CALLS","READ_OWNER_DATA","REBOOT","RECEIVE_WAP_PUSH","REORDER_TASKS","SET_ALWAYS_FINISH","SET_PROCESS_LIMIT","STATUS_BAR","WRITE_SECURE"
	};
	
	
}