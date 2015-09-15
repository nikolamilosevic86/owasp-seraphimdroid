package org.owasp.seraphimdroid.model;

public class DrawerItem {
	private String itemName;
	private int iconId;

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public DrawerItem(String name, int id) {
		itemName = name;
		iconId = id;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
}
