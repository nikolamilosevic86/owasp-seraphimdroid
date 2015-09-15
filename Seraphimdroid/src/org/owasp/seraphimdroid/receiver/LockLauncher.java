package org.owasp.seraphimdroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.owasp.seraphimdroid.services.AppLockService;

public class LockLauncher extends BroadcastReceiver {

	private static final String TAG = "LockLauncher";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)
				|| action.equals(Intent.ACTION_SCREEN_ON)) {
			context.startService(new Intent(context, AppLockService.class));
		}
		else if(action.equals(Intent.ACTION_SCREEN_OFF)){
			context.stopService(new Intent(context, AppLockService.class));
			Log.d(TAG, "exiting");
			System.exit(0);
		}
		Log.d(TAG, action);
		Toast.makeText(context, action, Toast.LENGTH_SHORT).show();
	}

}
