package org.owasp.seraphimdroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ApplicationInstallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Toast.makeText(arg0, "Uninstalled BOOYAH", Toast.LENGTH_LONG).show();
	}

}
