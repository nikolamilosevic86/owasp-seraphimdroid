package org.owasp.seraphimdroid.customclasses;

public class PermissionData {
	private String permission;
	private String permissionName;
	private String description;
	private String regularUseDescription;
	private String maliciousUseDescription;
	private int weight;
	
	public PermissionData(String permission){
		this.permission = permission;
		setData();
	}
	
	private void setData(){
		weight = 0;
	}	
	
	//Getters and setter
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

	public String getRegularUseDescription() {
		return regularUseDescription;
	}

	public void setRegularUseDescription(String regularUseDescription) {
		this.regularUseDescription = regularUseDescription;
	}

	public String getMaliciousUseDescription() {
		return maliciousUseDescription;
	}

	public void setMaliciousUseDescription(String maliciousUseDescription) {
		this.maliciousUseDescription = maliciousUseDescription;
	}
	
	

}
