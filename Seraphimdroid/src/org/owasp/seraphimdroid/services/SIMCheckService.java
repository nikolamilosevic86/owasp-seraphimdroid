package org.owasp.seraphimdroid.services;

import org.owasp.seraphimdroid.PasswordActivity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class SIMCheckService extends IntentService {

	String idSIM1;
	int idSIM2;
	Boolean isSIM1Detected, isSIM2Detected;
	Context context;
	static TelephonyManager telephony;
	SharedPreferences defaultPrefs;
	
	public SIMCheckService() {
		super("SIMCheckService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		context = getApplicationContext();
		telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
//		isSIM1Detected = telephony.getDataState()==telephony.SIM_STATE_READY;
		if(telephony.getSimSerialNumber()!=null) {
			idSIM1 = telephony.getSimSerialNumber() + telephony.getNetworkOperator() + telephony.getNetworkCountryIso();
		}
        
        String oldSIM1 = null;
        if(defaultPrefs.contains("sim_1")) {
        	oldSIM1 = defaultPrefs.getString("sim_1", "");
        }
        
        //SIM not found
        if(idSIM1==null) return;
        
        //SIM Check
        if(oldSIM1==null || (wasPresent(idSIM1, oldSIM1)==false)) {
        	//show lock
        	Toast.makeText(getApplicationContext(), "SIM Change Detected", Toast.LENGTH_SHORT).show();
        	showLock(idSIM1);
        	return;
        }
                
	}
	
	private Boolean wasPresent(String id, String oldSIM) {
		if(oldSIM!=null ) {
			if(oldSIM.equals(id+"")) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	private void showLock(final String id) {
		Intent passwordAct = new Intent(context, PasswordActivity.class);
		passwordAct.putExtra("PACKAGE_NAME", "SIM Change");
		passwordAct.putExtra("device_id", id+"");
		passwordAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(passwordAct);
	}

}
