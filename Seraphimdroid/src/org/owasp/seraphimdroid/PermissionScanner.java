package org.owasp.seraphimdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.customadapters.PermissionListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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

		// Setting listener for the listview
		elvAppPermissions
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View item, int postion, long groupId) {
						if (ExpandableListView.getPackedPositionType(groupId) != ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
							String pkgName = item.getTag().toString();
							Uri packageUri = Uri.parse("package:" + pkgName);
							Intent uninstallIntent = new Intent(
									Intent.ACTION_DELETE, packageUri);
							startActivity(uninstallIntent);
							return true;
						}
						return false;
					}
				});

	}

	public boolean isSystemPackage(PackageInfo packageInfo) {
		return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
				: false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		prepareListData();

		// Setting Adapters for the list view
		PermissionListAdapter adapter = new PermissionListAdapter(this,
				packageHeaders, childDataItems);
		elvAppPermissions.setAdapter(adapter);
	}

	public void prepareListData() {
		PackageManager pm = getApplicationContext().getPackageManager();

		packageHeaders.clear();
		childDataItems.clear();

		// Populate Containers.
		for (ApplicationInfo appInfo : installedApplications) {
			try {
				PackageInfo packInfo = pm.getPackageInfo(appInfo.packageName,
						PackageManager.GET_PERMISSIONS);

				if (!isSystemPackage(packInfo)
						&& !(packInfo.packageName
								.equals("org.owasp.seraphimdroid"))) {
					// Adding header keys
					packageHeaders.add(appInfo.packageName);

					// Adding permissions
					String[] permissions = packInfo.requestedPermissions;
					if (permissions != null) {
						ArrayList<String> reqPermissions = new ArrayList<String>();
						for (String per : permissions) {
							reqPermissions.add(per);
						}
						childDataItems.put(appInfo.packageName, reqPermissions);
					} else {
						ArrayList<String> reqPermissions = new ArrayList<String>();
						reqPermissions.add("No permissions used");
						childDataItems
								.put(packInfo.packageName, reqPermissions);
					}
				}
			} catch (NameNotFoundException ne) {
				ne.printStackTrace();
			}
		}
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
