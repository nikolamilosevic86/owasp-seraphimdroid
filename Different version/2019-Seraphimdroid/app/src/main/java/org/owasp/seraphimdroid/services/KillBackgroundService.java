package org.owasp.seraphimdroid.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KillBackgroundService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String packageName = intent.getStringExtra("PACKAGE_NAME");
		
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		
		
		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(homeIntent);
		
		am.killBackgroundProcesses(packageName);
		
		return Service.START_NOT_STICKY;
	}
	
	

}
