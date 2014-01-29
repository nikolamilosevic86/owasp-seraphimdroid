package org.owasp.seraphimdroid.customclasses;

import java.util.HashMap;

import org.owasp.seraphimdroid.R;

import android.content.Context;
import android.content.res.Resources;

public class PermissionData {
	private String permission;
	private String permissionName;
	private String description;
	private String maliciousUseDescription;
	private int weight;

	/*
	 * public PermissionData(Context context, String permission, int nameId, int
	 * descriptionId, int maliciousId, int weight) { this.permission =
	 * permission; this.permissionName = context.getString(nameId);
	 * this.description = context.getString(descriptionId); this.weight =
	 * weight; if (maliciousId != 0) this.maliciousUseDescription =
	 * context.getString(maliciousId); }
	 */

	public PermissionData(Context context, String permission, String name,
			String description, String malicious, int weight) {
		this.permission = permission;
		this.permissionName = name;
		this.description = description;
		this.maliciousUseDescription = malicious;
		this.weight = weight;
	}

	// Getters and setter
	public String getPermissionName() {
		return permissionName;
	}

	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMaliciousUseDescription() {
		return maliciousUseDescription;
	}

	public void setMaliciousUseDescription(String maliciousUseDescription) {
		this.maliciousUseDescription = maliciousUseDescription;
	}

	public static PermissionData getPermissionData(Context context,
			String permission) {
		String[] permissions = { "android.permission.ACCESS_COARSE_LOCATION",
				"android.permission.ACCESS_FINE_LOCATION",
				"android.permission.ACCESS_LOCATION_EXTRA_COMMANDS",
				"android.permission.ACCESS_MOCK_LOCATION",
				"android.permission.ACCESS_NETWORK_STATE",
				"android.permission.ACCESS_WIFI_STATE",
				"com.android.voicemail.permission.ADD_VOICEMAIL",
				"android.permission.AUTHENTICATE_ACCOUNTS",
				"android.permission.BATTERY_STATS",
				"android.permission.BIND_ACCESSIBILITY_SERVICE",
				"android.permission.BIND_DEVICE_ADMIN",
				"android.permission.BIND_INPUT_METHOD",
				"android.permission.BIND_NFC_SERVICE",
				"android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
				"android.permission.BIND_PRINT_SERVICE",
				"android.permission.BIND_REMOTEVIEWS",
				"android.permission.BIND_TEXT_SERVICE",
				"android.permission.BIND_VPN_SERVICE",
				"android.permission.BIND_WALLPAPER",
				"android.permission.BLUETOOTH",
				"android.permission.BLUETOOTH_ADMIN",
				"android.permission.BLUETOOTH_PRIVILEGED",
				"android.permission.BROADCAST_STICKY",
				"android.permission.CALL_PHONE", "android.permission.CAMERA",
				"android.permission.CHANGE_CONFIGURATION",
				"android.permission.CHANGE_NETWORK_STATE",
				"android.permission.CHANGE_WIFI_MULTICAST_STATE",
				"android.permission.CHANGE_WIFI_STATE",
				"android.permission.CLEAR_APP_CACHE",
				"android.permission.DISABLE_KEYGUARD",
				"android.permission.EXPAND_STATUS_BAR",
				"android.permission.FLASHLIGHT",
				"android.permission.GET_ACCOUNTS",
				"android.permission.GET_PACKAGE_SIZE",
				"android.permission.GET_TASKS",
				"android.permission.GLOBAL_SEARCH",
				"com.android.launcher.permission.INSTALL_SHORTCUT",
				"android.permission.INTERNET",
				"android.permission.KILL_BACKGROUND_PROCESSES",
				"android.permission.MANAGE_ACCOUNTS",
				"android.permission.MANAGE_DOCUMENTS",
				"android.permission.MODIFY_AUDIO_SETTINGS",
				"android.permission.NFC",
				"android.permission.PROCESS_OUTGOING_CALLS",
				"android.permission.READ_CALENDAR",
				"android.permission.READ_CALL_LOG",
				"android.permission.READ_CONTACTS",
				"android.permission.READ_EXTERNAL_STORAGE",
				"com.android.browser.permission.READ_HISTORY_BOOKMARKS",
				"android.permission.READ_PHONE_STATE",
				"android.permission.READ_PROFILE",
				"android.permission.READ_SMS",
				"android.permission.READ_SOCIAL_STREAM",
				"android.permission.READ_SYNC_SETTINGS",
				"android.permission.READ_SYNC_STATS",
				"android.permission.READ_USER_DICTIONARY",
				"android.permission.RECEIVE_BOOT_COMPLETED",
				"android.permission.RECEIVE_MMS",
				"android.permission.RECEIVE_SMS",
				"android.permission.RECEIVE_WAP_PUSH",
				"android.permission.RECORD_AUDIO",
				"android.permission.REORDER_TASKS",
				"android.permission.SEND_SMS",
				"com.android.alarm.permission.SET_ALARM",
				"android.permission.SET_TIME_ZONE",
				"android.permission.SET_WALLPAPER",
				"android.permission.SET_WALLPAPER_HINTS",
				"android.permission.SUBSCRIBED_FEEDS_READ",
				"android.permission.SUBSCRIBED_FEEDS_WRITE",
				"android.permission.SYSTEM_ALERT_WINDOW",
				"android.permission.TRANSMIT_IR",
				"com.android.launcher.permission.UNINSTALL_SHORTCUT",
				"android.permission.USE_CREDENTIALS",
				"android.permission.USE_SIP", "android.permission.VIBRATE",
				"android.permission.WAKE_LOCK",
				"android.permission.WRITE_CALENDAR",
				"android.permission.WRITE_CALL_LOG",
				"android.permission.WRITE_CONTACTS",
				"android.permission.WRITE_EXTERNAL_STORAGE",
				"com.android.browser.permission.WRITE_HISTORY_BOOKMARKS",
				"android.permission.WRITE_PROFILE",
				"android.permission.WRITE_SETTINGS",
				"android.permission.WRITE_SMS",
				"android.permission.WRITE_SOCIAL_STREAM",
				"android.permission.WRITE_SYNC_SETTINGS",
				"android.permission.WRITE_USER_DICTIONARY" };

		int index = 0;
		while (!permissions[index].equals(permission)) {
			index++;
			if (index == permissions.length) {
				index--;
				break;
			}
		}

		Resources res = context.getResources();
		String[] descriptions = res.getStringArray(R.array.descriptions);
		String[] permissionNames = res.getStringArray(R.array.permission_names);

		// Creating the object required.
		PermissionData pd = new PermissionData(context, permissions[index],
				permissionNames[index], descriptions[index],
				"No Malicious Use", 0);

		/*
		 * PermissionData[] permissionData = {
		 * 
		 * }; HashMap<String, PermissionData> permissionDataMap = new
		 * HashMap<String, PermissionData>();
		 * 
		 * // Setting HashMap for (int i = 0; i < permissions.length; i++) {
		 * permissionDataMap.put(permissions[i], permissionData[i]); }
		 */

		return pd;
	}

}
