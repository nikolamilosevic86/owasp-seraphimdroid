package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.receiver.ApplicationInstallReceiver;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class AppInstallService extends Service {

	private ApplicationInstallReceiver appInstallReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Application scan on run
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
	    appInstallReceiver = new ApplicationInstallReceiver();
		registerReceiver(appInstallReceiver, intentFilter);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(appInstallReceiver);
		super.onDestroy();
	}
	
	

}
