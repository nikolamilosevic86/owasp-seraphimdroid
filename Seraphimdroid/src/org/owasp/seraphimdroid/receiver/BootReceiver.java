package org.owasp.seraphimdroid.receiver;

import org.owasp.seraphimdroid.services.SettingsCheckService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, SettingsCheckService.class);
            context.startService(serviceIntent);
        }
	}

}
