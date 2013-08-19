package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;

public class USSDUriInterceptor extends Activity {
	
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
			//Abort execution and show a toast message
	       	CharSequence text = getString(R.string.ussd_blocked_msg) + " " + number ;
        	Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        	toast.show();
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
}
