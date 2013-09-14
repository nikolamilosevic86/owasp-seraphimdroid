package org.owasp.seraphimdroid;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.owasp.seraphimdroid.data.DatabaseAdapter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import android.widget.Toast;

//Will be used for filtering spam and phishing sms
public class SMSReciever extends BroadcastReceiver {
	
	public static final int NOTIFICATION_ID_SPAM_SMS = 900;

	public static final String SMS_RECEIVED_ACTION =

	"android.provider.Telephony.SMS_RECEIVED";
	
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
		 manager.notify(NOTIFICATION_ID_SPAM_SMS, builder.build());
  }
	

    private static boolean IsMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern,  Pattern.CASE_INSENSITIVE);
            Matcher matcher = patt.matcher(s);
            return matcher.find();
        } catch (PatternSyntaxException pse) {
        return false;  
    }       
}

	@Override
	public void onReceive(Context context, Intent intent) {
		// this regex matches all url.. All google.com, http://google.com , www.google.com and http://www.google.com will be matched
		String regex = "([a-zA-Z0-9+&@#/%?=~_|!:,;]*[\\.][a-zA-Z0-9+&@#/%?=~_|!:,;]*){1,}";
				//"(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		//String regex2 ="(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>???“”‘’]))";
         
		String smsData = null;
		String smsNumber = "";
		long timestamp = 0;

		if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
			
			Bundle pudsBundle = intent.getExtras();

			Object[] pdus = (Object[]) pudsBundle.get("pdus");

			SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
			int message_id = messages.getIndexOnIcc();
			timestamp = messages.getTimestampMillis();
			smsNumber = messages.getOriginatingAddress();
			smsData = messages.getMessageBody();
			// there is url in message
			//TODO: Add various regex for phishing (phone number, url without http)
			if(IsMatch(smsData,regex)){
				showNotification(context, "Souspicious SMS (Potencial phishing)", smsData);
				DatabaseAdapter da = new DatabaseAdapter(context);
				da.addLogItem(message_id, "SPAM_SMS", smsNumber, smsData);
				this.abortBroadcast();
			}

			
//			this.abortBroadcast();
			//Code for returning to sms to regular messages.
//			Uri uri = Uri.parse("content://sms");
//			ContentValues cv = new ContentValues();
//			cv.put("address", smsNumber);
//			cv.put("date", timestamp);
//			cv.put("read", 0);
//			cv.put("type", 1);
//			cv.put("subject", "");
//			cv.put("body", smsData);
//			context.getContentResolver().insert(uri, cv);
		}
		
		

	}

}

