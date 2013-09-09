package org.owasp.seraphimdroid.ussd;

import org.owasp.seraphimdroid.MainScreen;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.R.drawable;
import org.owasp.seraphimdroid.R.string;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class USSDDialInterceptor extends BroadcastReceiver {
	
	private static final int NOTIFICATION_ID_USSD_2 = 902;

	@Override
	public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d("USSDReceiver", number);
        if (USSDValidator.isSafeUssd(number)) {
        	// Let the call proceed
        	setResult(Activity.RESULT_OK, number, null);
        } else {
        	// Drop the call, show a notification message
        	setResult(Activity.RESULT_CANCELED, null, null);
           	CharSequence title = context.getString(R.string.ussd_blocked_title);
        	CharSequence text = context.getString(R.string.ussd_blocked_msg) + " " + number ;
        	showNotification(context, title, text);
        }
    }
	
	private void showNotification(Context context, CharSequence title, CharSequence text) {
		 NotificationCompat.Builder builder =
		         new NotificationCompat.Builder(context)
		         .setSmallIcon(R.drawable.ic_launcher)
		         .setContentTitle(title)
		         .setContentText(text)
		         .setTicker(title);
		
		 Intent notificationIntent = new Intent(context, MainScreen.class);
		 PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 
		   PendingIntent.FLAG_UPDATE_CURRENT);
		 builder.setContentIntent(contentIntent);
		
		 // Add as notification
		 NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		 manager.notify(NOTIFICATION_ID_USSD_2, builder.build());
   }
	
}
