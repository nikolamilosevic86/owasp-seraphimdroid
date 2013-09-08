package org.owasp.seraphimdroid.outgoingcalls;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.app.NotificationManager;
import android.widget.Toast;

import org.owasp.seraphimdroid.R;


public class OutgoingCallInterceptor extends BroadcastReceiver {
	public static boolean allowAll = false; // TEMP: allows redials from this app
	private int NOTIFICATION_ID = 800;

	/**
	 * Checks if the outgoing call number exists in system's Contacts list
	 */
	private boolean isNumberInContactsList(Context context, String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Displays blocked call notification
	 */
	private void raiseNotification(Context context, String title, String text) {
		// Make notification
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_launcher)
						.setAutoCancel(true)
						.setContentTitle(title)
						.setContentText(text);

		// Display OutgoingCallLogActivity on notification click
		Intent resultIntent = new Intent(context, OutgoingCallLogActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(OutgoingCallLogActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		// Display notification
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	/**
	 * Returns conuntry code (e.g. "US", "UK", "RS") of user's SIM card
	 * TODO: discuss if this is usable

	private String getSimCountryCode(Context context) {
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String simCountryCode = tm.getSimCountryIso();

		return simCountryCode;
	}
	 */


    @Override
    public void onReceive(Context context, Intent intent) {
	    Bundle bundle = intent.getExtras();

	    if (null != bundle) {
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		    boolean allowCall = isNumberInContactsList(context, number);

		    if (!allowCall && !allowAll) {
			    // Block call
			    setResultData(null);

			    // Notification
			    String notificationTitle = "Outgoing call blocked!";
			    String notificationText = number + " is not in contacts list.";
			    raiseNotification(context, notificationTitle, notificationText);

			    // Toast
			    Toast.makeText(context, "Call blocked!", Toast.LENGTH_LONG).show();

			    Log.d("OutgoingCallBlocked", number);
		    }
		    else {
			    Log.d("OutgoingCallAllowed", number);
		    }

		    // Add call attempt to log
		    CallAttempt callAttempt = new CallAttempt(number, allowCall);
		    CallLogPrototype.addCallAttempt(callAttempt);
	    }

		allowAll = false;   // TEMP: disallow calls
    }
}
