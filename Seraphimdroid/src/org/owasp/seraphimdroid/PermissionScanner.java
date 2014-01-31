package org.owasp.seraphimdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.owasp.seraphimdroid.customadapters.PermissionListAdapter;
import org.owasp.seraphimdroid.customclasses.PermissionData;
import org.owasp.seraphimdroid.customclasses.PermissionGetter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class PermissionScanner extends Activity {

	private ExpandableListView elvAppPermissions;
	private List<ApplicationInfo> installedApplications;
	private ArrayList<String> packageHeaders;
	private HashMap<String, ArrayList<PermissionData>> childDataItems;
	private boolean isDataChanged;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission_scanner);

		final PackageManager pm = getPackageManager();
		isDataChanged = true;

		// Initializing Expandable List View.
		elvAppPermissions = (ExpandableListView) findViewById(R.id.elvApplicationPermissions);

		// Initializing Containers.
		installedApplications = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		packageHeaders = new ArrayList<String>();
		childDataItems = new HashMap<String, ArrayList<PermissionData>>();

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

							String AppName = pkgName;
							try {
								AppName = pm
										.getApplicationInfo(pkgName,
												PackageManager.GET_META_DATA)
										.loadLabel(pm).toString();
							} catch (NameNotFoundException ne) {
								ne.printStackTrace();
							}

							Toast.makeText(getApplicationContext(),
									"Uninstalling: " + AppName,
									Toast.LENGTH_LONG).show();
							isDataChanged = true;
							return true;
						}
						return false;
					}
				});

		elvAppPermissions.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View child,
					int groupPosition, int childPosition, long childId) {
				Intent descriptionIntent = new Intent(getApplicationContext(),
						PermissionDescription.class);

				String permissionName = (String) ((TextView) child
						.findViewById(R.id.tvChild)).getTag();
				descriptionIntent.putExtra("PERMISSION_NAME", permissionName);
				startActivity(descriptionIntent);
				return true;
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
		if (isDataChanged) {
			prepareListData();
		}
	}

	public void prepareListData() {
		// Clear the already stored data
		packageHeaders.clear();
		childDataItems.clear();

		// Populate Containers.
		// for (ApplicationInfo appInfo : installedApplications) {
		// try {
		// PackageInfo packInfo = pm.getPackageInfo(appInfo.packageName,
		// PackageManager.GET_PERMISSIONS);
		//
		// if (!isSystemPackage(packInfo)
		// && !(packInfo.packageName
		// .equals("org.owasp.seraphimdroid"))) {
		// // Adding header keys
		// packageHeaders.add(appInfo.packageName);
		//
		// // Adding permissions
		// String[] permissions = packInfo.requestedPermissions;
		// if (permissions != null) {
		// ArrayList<PermissionData> reqPermissions = new
		// ArrayList<PermissionData>();
		// for (String per : permissions) {
		// // PermissionData pd = PermissionData
		// // .getPermissionData(getApplicationContext(),
		// // per);
		// PermissionData pd = new PermissionGetter().execute(
		// per).get();
		// reqPermissions.add(pd);
		// }
		// childDataItems.put(appInfo.packageName, reqPermissions);
		// } else {
		//
		// }
		// }
		//
		// // pd.dismiss();
		// } catch (NameNotFoundException ne) {
		// ne.printStackTrace();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ExecutionException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		new PopulateContainer().execute();
		isDataChanged = false;

	}

	private class PopulateContainer extends AsyncTask<Void, Void, Void> {

		ProgressDialog loading = new ProgressDialog(PermissionScanner.this);

		@Override
		protected void onPreExecute() {
			loading.setTitle("Scanning Permissions");
			loading.setMessage("Please Wait...");
			loading.show();
			loading.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d("PopulateContainer", "Inside doInBackgroud");
			final PackageManager pm = getApplicationContext()
					.getPackageManager();
			for (ApplicationInfo appInfo : installedApplications) {
				try {
					PackageInfo packInfo = pm
							.getPackageInfo(appInfo.packageName,
									PackageManager.GET_PERMISSIONS);

					if (!isSystemPackage(packInfo)
							&& !(packInfo.packageName
									.equals("org.owasp.seraphimdroid"))) {
						// Adding header keys
						packageHeaders.add(appInfo.packageName);

						// Adding permissions
						String[] permissions = packInfo.requestedPermissions;
						if (permissions != null) {
							PermissionGetter gp = new PermissionGetter();
							ArrayList<PermissionData> reqPermissions = new ArrayList<PermissionData>();
							for (String per : permissions) {
								// PermissionData pd = PermissionData
								// .getPermissionData(getApplicationContext(),
								// per);
								// PermissionData pd = new PermissionGetter()
								// .execute(per).get();

								gp.setPermission(per);
								PermissionData pd = gp.getPermissionData();
								Log.d("PopulateContainer",
										pd.getPermissionName());

								reqPermissions.add(pd);
							}
							childDataItems.put(appInfo.packageName,
									reqPermissions);
						} else {

						}
					}
				} catch (NameNotFoundException ne) {
					ne.printStackTrace();
				} // catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// } catch (ExecutionException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
			}
			Log.d("PopulateContainer", "Exiting doInBackground");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Setting Adapters for the list view
			PermissionListAdapter adapter = new PermissionListAdapter(
					PermissionScanner.this, packageHeaders, childDataItems);
			elvAppPermissions.setAdapter(adapter);
			loading.dismiss();
			super.onPostExecute(result);
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
