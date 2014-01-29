package org.owasp.seraphimdroid.customclasses;

import java.util.concurrent.ExecutionException;

import android.content.Context;

public class PermissionData {
	private String permission;
	private String permissionName;
	private String description;
	private String maliciousUseDescription;
	private int weight;

	/*
	 * public PermissionData(Context context, String permission, int nameId, int
	 * descriptionId, int maliciousId, int weight) { this.permission =
	 * permission; this.permissionName = context.getString(nameId);
	 * this.description = context.getString(descriptionId); this.weight =
	 * weight; if (maliciousId != 0) this.maliciousUseDescription =
	 * context.getString(maliciousId); }
	 */

	public PermissionData(String permission, String name, String description,
			String malicious, int weight) {
		this.permission = permission;
		this.permissionName = name;
		this.description = description;
		this.maliciousUseDescription = malicious;
		this.weight = weight;

	}

	// Getters and setter
	public String getPermissionName() {
		return permissionName;
	}

	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMaliciousUseDescription() {
		return maliciousUseDescription;
	}

	public void setMaliciousUseDescription(String maliciousUseDescription) {
		this.maliciousUseDescription = maliciousUseDescription;
	}

	public static PermissionData getPermissionData(Context context,
			String permission) {

		try {
			PermissionData pd = new PermissionGetter().execute(permission)
					.get();

			return pd;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new PermissionData(permission, permission,
				"No Description Available", "No Description Available", 0);

		/*
		 * PermissionData[] permissionData = {
		 * 
		 * }; HashMap<String, PermissionData> permissionDataMap = new
		 * HashMap<String, PermissionData>();
		 * 
		 * // Setting HashMap for (int i = 0; i < permissions.length; i++) {
		 * permissionDataMap.put(permissions[i], permissionData[i]); }
		 */

	}

}

