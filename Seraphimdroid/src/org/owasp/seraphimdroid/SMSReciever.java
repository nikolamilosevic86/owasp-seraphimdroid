package org.owasp.seraphimdroid;


import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;

import android.telephony.SmsMessage;

import android.widget.Toast;

//Will be used for filtering spam and phishing sms
public class SMSReciever extends BroadcastReceiver {

	public static final String SMS_RECEIVED_ACTION =

	"android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {

		String smsData = null;
		String smsNumber = "";
		long timestamp = 0;

		if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {

			Bundle pudsBundle = intent.getExtras();

			Object[] pdus = (Object[]) pudsBundle.get("pdus");

			SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
			timestamp = messages.getTimestampMillis();
			smsNumber = messages.getOriginatingAddress();
			smsData = messages.getMessageBody();
			FileWriter f;
				try {

					f = new FileWriter(
							Environment.getExternalStorageDirectory()
									+ "/SMSLog.txt", true);
					Date d = new Date(timestamp);
					SimpleDateFormat postFormater = new SimpleDateFormat(
							"dd.MM.yyyy HH:mm:ss");

					String newDateStr = postFormater.format(d);
					f.write(newDateStr + " - " + smsNumber + " - " + smsData
							+ "\r\n");
					f.flush();
					f.close();

				} catch (Exception ex) {
					Toast.makeText(context, "Error:" + ex.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			
//			this.abortBroadcast();
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

