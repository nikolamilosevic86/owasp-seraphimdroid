package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class MakeACallService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String number = intent.getStringExtra("PHONE_NUMBER");
		
		Log.d("MAKEACALL",number);

		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + number));
		callIntent.putExtra("CALL_ALLOWED", true);
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(callIntent);
		
		MainActivity.shouldReceive = false;

		return super.onStartCommand(intent, flags, startId);
	}

}
