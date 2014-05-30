package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.model.PermissionData;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;

public class PermissionGetter {

	private final String maliciousDesc = "No Malicious use known";
	private final int weight = 0;

	// String permission;
	PackageManager packageManager;

	public PermissionGetter(PackageManager packageManager) {

		this.packageManager = packageManager;

	}

	public PermissionData generatePermissionData(String permission) {

		PermissionInfo perInfo = null;
		try {
			perInfo = packageManager.getPermissionInfo(permission,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (perInfo != null) {
			String perName = (String) perInfo.loadLabel(packageManager);
			String perDesc = (String) perInfo.loadDescription(packageManager);
			if(perDesc == null || perDesc.equals("")){
				perDesc = "No Description Available.";
			}

			PermissionData perData = new PermissionData(permission, perName,
					perDesc, maliciousDesc, weight);
			return perData;
		}
		return null;
		
	}
}
