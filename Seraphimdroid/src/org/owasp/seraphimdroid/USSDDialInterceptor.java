package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class USSDDialInterceptor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d("USSDReceiver", number);
        if (USSDValidator.isSafeUssd(number)) {
        	// Let the call proceed
        	setResult(Activity.RESULT_OK, number, null);
        } else {
        	// Drop the call, show a toast a message
        	setResult(Activity.RESULT_CANCELED, null, null);
        	CharSequence text = context.getString(R.string.ussd_blocked_msg) + " " + number ;
        	int duration = Toast.LENGTH_LONG;
        	Toast toast = Toast.makeText(context, text, duration);
        	toast.show();
        }
    }
	
}
