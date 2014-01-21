package org.owasp.seraphimdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.customadapters.PermissionListAdapter;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

public class PermissionScanner extends Activity {

	private ExpandableListView elvAppPermissions;
	private List<ApplicationInfo> installedApplications;
	private ArrayList<String> packageHeaders;
	private HashMap<String, ArrayList<String>> childDataItems;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.permission_scanner_layout);

		final PackageManager pm = getPackageManager();
		// Initializing Expandable List View.
		elvAppPermissions = (ExpandableListView) findViewById(R.id.elvApplicationPermissions);

		// Initializing Containers.
		installedApplications = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		packageHeaders = new ArrayList<String>();
		childDataItems = new HashMap<String, ArrayList<String>>();

		// Populate Containers.
		for (ApplicationInfo appInfo : installedApplications) {
			try {
				PackageInfo packInfo = pm.getPackageInfo(appInfo.packageName,
						PackageManager.GET_PERMISSIONS);
				
				//Adding header keys
				packageHeaders.add(appInfo.packageName);
				
				//Adding permissions
				String[] permissions = packInfo.requestedPermissions;
				if (permissions != null) {
					ArrayList<String> reqPermissions = new ArrayList<String>();
					for (String per : permissions) {
						reqPermissions.add(per);
					}
					childDataItems.put(appInfo.packageName, reqPermissions);
				}else{
					ArrayList<String> reqPermissions = new ArrayList<String>();
					reqPermissions.add("No permissions used");
					childDataItems.put(packInfo.packageName, reqPermissions);
				}
					
			} catch (NameNotFoundException ne) {
				ne.printStackTrace();
			}
		}
		
		PermissionListAdapter adapter = new PermissionListAdapter(this, packageHeaders, childDataItems);
		elvAppPermissions.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
