package org.owasp.seraphimdroid.receiver;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.owasp.seraphimdroid.MainActivity;
import org.owasp.seraphimdroid.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v4.app.NotificationCompat;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ApplicationInstallReceiver extends BroadcastReceiver{

	String[] permissions = {
			"ACCESS_COARSE_LOCATION","ACCESS_COARSE_UPDATES","ACCESS_FINE_LOCATION","ACCESS_FM_RECEIVER","ACCESS_LOCATION_EXTRA_COMMANDS","ACCESS_MOCK_LOCATION","ACCESS_NETWORK_STATE","ACCESS_WIFI_STATE","ACCESS_WIMAX_STATE","AUTHENTICATE_ACCOUNTS","BATTERY_STATS","BIND_ACCESSIBILITY_SERVICE","BIND_APPWIDGET","BIND_DEVICE_ADMIN","BIND_INPUT_METHOD","BIND_REMOTEVIEWS","BIND_WALLPAPER","BLUETOOTH","BLUETOOTH_ADMIN","BROADCAST_SMS","BROADCAST_STICKY","BROADCAST_WAP_PUSH","CALL_PHONE","CALL_PRIVILEGED","CAMERA","CHANGE_COMPONENT_ENABLED_STATE","CHANGE_CONFIGURATION","CHANGE_NETWORK_STATE","CHANGE_WIFI_MULTICAST_STATE","CHANGE_WIFI_STATE","CHANGE_WIMAX_STATE","DEVICE_POWER","DISABLE_KEYGUARD","EXPAND_STATUS_BAR","FLASHLIGHT","FM_RADIO_RECEIVER","FM_RADIO_TRANSMITTER","GET_ACCOUNTS","GET_PACKAGE_SIZE","GET_TASKS","GLOBAL_SEARCH","HARDWARE_TEST","INSTALL_PACKAGES","INTERACT_ACROSS_USERS","INTERACT_ACROSS_USERS_FULL","INTERNET","KILL_BACKGROUND_PROCESSES","MANAGE_ACCOUNTS","MANAGE_USB","MODIFY_AUDIO_SETTINGS","MODIFY_PHONE_STATE","MOUNT_UNMOUNT_FILESYSTEMS","NETWORK","NFC","PERSISTENT_ACTIVITY","PROCESS_OUTGOING_CALLS","RAISED_THREAD_PRIORITY","READ_CALENDAR","READ_CALL_LOG","READ_CONTACTS","READ_EXTERNAL_STORAGE","READ_LOGS","READ_PHONE_STATE","READ_PROFILE","READ_SECURE_SETTINGS","READ_SMS","READ_SYNC_SETTINGS","READ_SYNC_STATS","READ_TASKS","READ_USER_DICTIONARY","RECEIVE_BOOT_COMPLETED","RECEIVE_MMS","RECEIVE_SMS","RECORD_AUDIO","RESTART_PACKAGES","SEND_SMS","SET_ACTIVITY_WATCHER","SET_DEBUG_APP","SET_ORIENTATION","SET_PREFERRED_APPLICATIONS","SET_WALLPAPER","SET_WALLPAPER_HINTS","STORAGE","SYSTEM_ALERT_WINDOW","SYSTEM_TOOLS","UPDATE_DEVICE_STATS","USEFMRADIO","USES_POLICY_FORCE_LOCK","USE_CREDENTIALS","VIBRATE","WAKE_LOCK","WRITE_APN_SETTINGS","WRITE_CALENDAR","WRITE_CALL_LOG","WRITE_CONTACTS","WRITE_EXTERNAL_STORAGE","WRITE_PROFILE","WRITE_SECURE_SETTINGS","WRITE_SETTINGS","WRITE_SMS","WRITE_SYNC_SETTINGS","WRITE_TASKS","WRITE_USER_DICTIONARY","ACCESS_CACHE_FILESYSTEM","ACCESS_GPS","ACCESS_LOCATION","ADD_SYSTEM_SERVICE","BACKUP","CLEAR_APP_CACHE","CLEAR_APP_USER_DATA","DELETE_CACHE_FILES","DELETE_PACKAGES","DIAGNOSTIC","INTERNAL_SYSTEM_WINDOW","PERMISSION_NAME","PROCESS_CALL","PROCESS_INCOMING_CALLS","READ_OWNER_DATA","REBOOT","RECEIVE_WAP_PUSH","REORDER_TASKS","SET_ALWAYS_FINISH","SET_PROCESS_LIMIT","STATUS_BAR","WRITE_SECURE"
	};
	private SMO svmModel;
	PackageManager pkgManager;
	HashMap<String, Integer> map;
	
	@Override
	public void onReceive(Context arg0, Intent intent) {
		try {
			AssetManager assetManager = arg0.getAssets();
			InputStream stream = assetManager.open("SMOWeka366.model");
			svmModel = (SMO) SerializationHelper.read(stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		map = new HashMap<String, Integer>();
		for(int i=0; i<permissions.length; i++) {
			map.put(permissions[i], i);
		}
		pkgManager = arg0.getPackageManager();
		String app_name = intent.getData().getSchemeSpecificPart().toString();
		PackageInfo pkgInfo;
		String[] appPermissions = new String[126];
		try {
			pkgInfo = pkgManager.getPackageInfo(app_name,
					PackageManager.GET_PERMISSIONS);
			
			appPermissions = pkgInfo.requestedPermissions;
		} catch(Exception e) {e.printStackTrace();}
		
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
		for (int i=0; i<appPermissions.length; i++) {
			String permission = appPermissions[i].replace("android.permission.","");
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
				fireNotification(arg0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void fireNotification(Context context) {
	    Intent notificationIntent = new Intent(context, MainActivity.class);
	    //Open Application Lock Fragment
	    notificationIntent.putExtra("FRAGMENT_NO", 0);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

	    PendingIntent intent = PendingIntent.getActivity(context, 0,
	            notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
	    Notification notification;
	    notification= new NotificationCompat.Builder(context)
                .setContentTitle("Malicious App Install")
                .setContentText(
                        "The application you installed may be malicous. Click to See").setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent).setWhen(0).setAutoCancel(true)
                .build();
		// Display Notification
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(0, notification);
	}

}
