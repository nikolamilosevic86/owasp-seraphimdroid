package org.owasp.seraphimdroid;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingsCheckerFragment extends Fragment {

	View view;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_settings_scanner, container, false);
		runCheck();
		return view;
	}
	
	private void runCheck() {
		//Check for USB DEbugging
		if(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.ADB_ENABLED, 0) != 1) {
			((ImageView) view.findViewById(R.id.imageView1)).setImageResource(R.drawable.ic_green_tick);
			((TextView) view.findViewById(R.id.debugging_click_here)).setVisibility(View.GONE);
		}
		else {
			RelativeLayout usbDebuggingLayout = (RelativeLayout) view.findViewById(R.id.debugging);
			usbDebuggingLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS), 0);
				}
			});
		}
		//Check for Unknown Sources
		if(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0) != 1) {
			((ImageView) view.findViewById(R.id.imageView2)).setImageResource(R.drawable.ic_green_tick);
			((TextView) view.findViewById(R.id.unknown_sources_click_here)).setVisibility(View.GONE);
		}
		else {
			RelativeLayout usbDebuggingLayout = (RelativeLayout) view.findViewById(R.id.unknown_sources);
			usbDebuggingLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS), 0);
				}
			});
		}


		if(getDeviceEncryptionStatus() != 1) {
			((ImageView) view.findViewById(R.id.imageView6)).setImageResource(R.drawable.ic_green_tick);
			((TextView) view.findViewById(R.id.encrypt_click_here)).setVisibility(View.GONE);
		}
		else {
			RelativeLayout usbDebuggingLayout = (RelativeLayout) view.findViewById(R.id.encrypted);
			usbDebuggingLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(Settings.ACTION_SECURITY_SETTINGS), 0);
				}
			});
		}


		if(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCK_PATTERN_ENABLED, 0) != 1) {
			((ImageView) view.findViewById(R.id.imageView7)).setImageResource(R.drawable.ic_green_tick);
			((TextView) view.findViewById(R.id.pin_click_here)).setVisibility(View.GONE);
		}
		else {
			RelativeLayout usbDebuggingLayout = (RelativeLayout) view.findViewById(R.id.pin);
			usbDebuggingLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS), 0);
				}
			});
		}

	}
	/**
	 * Returns the encryption status of the device. Prior to Honeycomb, whole device encryption was
	 * not supported by Android, and this method returns ENCRYPTION_STATUS_UNSUPPORTED.
	 *
	 * @return One of the following constants from DevicePolicyManager:
	 *         ENCRYPTION_STATUS_UNSUPPORTED, ENCRYPTION_STATUS_INACTIVE,
	 *         ENCRYPTION_STATUS_ACTIVATING, or ENCRYPTION_STATUS_ACTIVE.
	 */
	private int getDeviceEncryptionStatus() {

		int status = DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED;

			final DevicePolicyManager dpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
			if (dpm != null) {
				status = dpm.getStorageEncryptionStatus();
			}


		return status;
	}
	@Override
	public void onResume() {
		super.onResume();
		runCheck();
	}
	
}
