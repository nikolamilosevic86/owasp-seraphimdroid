package org.owasp.seraphimdroid.ussd;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;

import org.owasp.seraphimdroid.MainScreen;
import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.R.drawable;
import org.owasp.seraphimdroid.R.string;
import org.owasp.seraphimdroid.activitylog.ActivityLog;
import org.owasp.seraphimdroid.data.DatabaseAdapter;



public class USSDUriInterceptor extends Activity {
	
	private static final int NOTIFICATION_ID_USSD_1 = 901;
	private String dialedNumber;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		onNewIntent(getIntent());
	}

	protected void onNewIntent(Intent paramIntent) {
		super.onNewIntent(paramIntent);
		this.dialedNumber = paramIntent.getDataString();

		String number = "";
		try {
			number = extractPhoneNumber(URLDecoder.decode(this.dialedNumber,
					"UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		
		if (USSDValidator.isSafeUssd(number)) {
			// Allow the USSD code execution
			processDialIntent(this.dialedNumber);		
		} else {
        	
          	CharSequence title = getString(R.string.ussd_blocked_title);
          	CharSequence shortMsg = getString(R.string.ussd_blocked_short_msg) + " " + number;
        	String detailMsg = getString(R.string.ussd_blocked_msg_1) + " " + number + getString(R.string.ussd_blocked_msg_2);
  
      		// Add an entry in log DB
       		DatabaseAdapter da = new DatabaseAdapter(this);
       		da.open();
       		da.addLogItem(0, "USSD", number, detailMsg);

        	// show a notification message
        	showNotification(this, title, shortMsg);
        	
		}
		finish();
	}

	protected void processDialIntent(String paramString) {
		Intent localIntent = new Intent("android.intent.action.DIAL");
		localIntent.setData(Uri.parse(paramString));
		startActivity(localIntent);
	}

	protected String extractPhoneNumber(String str) {
		String[] strings = str.split(":");
		return Array.getLength(strings) > 1 ? strings[1] : str;
	}
   
	private void showNotification(Context context, CharSequence title, CharSequence text) {
		 NotificationCompat.Builder builder =
		         new NotificationCompat.Builder(context)
		         .setSmallIcon(R.drawable.ic_launcher)
		         .setContentTitle(title)
		         .setContentText(text)
		         .setTicker(title);
		
		 Intent notificationIntent = new Intent(context, ActivityLog.class);
		 PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 
		   PendingIntent.FLAG_UPDATE_CURRENT);
		 builder.setContentIntent(contentIntent);
		
		 // Add as notification
		 NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		 manager.notify(NOTIFICATION_ID_USSD_1, builder.build());
  }

   
}
