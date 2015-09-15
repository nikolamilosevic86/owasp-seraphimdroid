package org.owasp.seraphimdroid;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
		runCheck();
	}
	
}
