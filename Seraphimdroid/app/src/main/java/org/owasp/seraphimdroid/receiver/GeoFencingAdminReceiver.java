package org.owasp.seraphimdroid.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class GeoFencingAdminReceiver extends DeviceAdminReceiver {

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		CharSequence warning = "If you disable device admin access you wont be able to use Geolocation feature of Seraphimdroid. Disbale only if you know that you really want to.";
		return warning;
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
	}

}
