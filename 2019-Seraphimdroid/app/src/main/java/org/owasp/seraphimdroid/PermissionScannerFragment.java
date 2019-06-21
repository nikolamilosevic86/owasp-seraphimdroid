package org.owasp.seraphimdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.owasp.seraphimdroid.adapter.PermissionScannerAdapter;
import org.owasp.seraphimdroid.model.PermissionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PermissionScannerFragment extends Fragment {

	private static final String TAG = "PermissionScanner";

	private ExpandableListView lvPermissionList;

	private PackageManager pkgManager;

	private static boolean isDataChanged;

	// Declaring Containers.
	private ArrayList<String> appList;
	private HashMap<String, List<PermissionData>> childPermissions;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_permission_scanner,
				container, false);

		isDataChanged = true;

		lvPermissionList = (ExpandableListView) view
				.findViewById(R.id.app_list);

		// lvPermissionList.setAdapter(new
		// PermissionScannerAdapter(getActivity(),
		// appList, childPermissions));

		lvPermissionList.setClickable(true);

		lvPermissionList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView list,
										View clickedView, int groupPos, int childPos, long childId) {

				Log.d(TAG, "Starting Permission Description");

				PermissionData perData = childPermissions.get(
						appList.get(groupPos)).get(childPos);

				String permission = perData.getPermission();

				if (!permission.equals("No Permission")) {

					Intent startPerDesc = new Intent(getActivity(),
							PermissionDescription.class);

					startPerDesc.putExtra("PERMISSION_NAME", permission);
					startActivity(startPerDesc);
				} else {
					ApplicationInfo appInfo;
					try {
						appInfo = pkgManager.getApplicationInfo(
								appList.get(groupPos),
								PackageManager.GET_META_DATA);
						String appName = (String) appInfo.loadLabel(pkgManager);

						Toast.makeText(getActivity(),
								appName + " uses no permission",
								Toast.LENGTH_LONG).show();
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}

				}
				return true;
			}
		});

		lvPermissionList
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
												   View item, int postion, long id) {

						if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
							return true;
						}

						final String pkgName = appList.get(postion);
						ColorDrawable cd = (ColorDrawable) item.findViewById(R.id.safety_indicator).getBackground();
						int colorCode = cd.getColor();

						final boolean mal_flag;
						mal_flag = colorCode == Color.RED;
//						Uri packageUri = Uri.parse("package:" + pkgName);
//						Intent uninstallIntent = new Intent(
//								Intent.ACTION_DELETE, packageUri);
//
//						startActivity(uninstallIntent);
//						String AppName = pkgName;
//						try {
//							AppName = pkgManager
//									.getApplicationInfo(pkgName,
//											PackageManager.GET_META_DATA)
//									.loadLabel(pkgManager).toString();
//						} catch (NameNotFoundException ne) {
//							ne.printStackTrace();
//						}
//
//						Toast.makeText(
//								PermissionScannerFragment.this.getActivity(),
//								"Uninstalling: " + AppName, Toast.LENGTH_LONG)
//								.show();

						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

						builder.setCancelable(true);
						builder.setTitle(pkgName);

						builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
							}
						});

						builder.setPositiveButton("Report", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent fb_intent = new Intent(getActivity(), ReportActivity.class);
								fb_intent.putExtra("report", true);
								fb_intent.putExtra("package", pkgName);
								fb_intent.putExtra("malicious", mal_flag);
								startActivity(fb_intent);
							}
						});

						builder.setNegativeButton("Uninstall", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								Uri packageUri = Uri.parse("package:" + pkgName);

								Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);

								startActivity(uninstallIntent);
								String AppName = pkgName;
								try {
									AppName = pkgManager
											.getApplicationInfo(pkgName,
													PackageManager.GET_META_DATA)
											.loadLabel(pkgManager).toString();
								} catch (NameNotFoundException ne) {
									ne.printStackTrace();
								}

								Toast.makeText(
										PermissionScannerFragment.this.getActivity(),
										"Uninstalling: " + AppName, Toast.LENGTH_LONG)
										.show();
							}
						});

						builder.setMessage("Selecting the Report Action will Report the App to us with your feedback. and selecting uninstall will uninstall the app.");

						AlertDialog alertDialog = builder.create();
						alertDialog.show();

						return true;
					}
				});

		isDataChanged = true;

		ImageView imgBtnHide = (ImageView) view.findViewById(R.id.img_hide_info);
		final RelativeLayout rlInfo = (RelativeLayout) view
				.findViewById(R.id.rl_info);
		imgBtnHide.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				rlInfo.setVisibility(View.GONE);
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		pkgManager = getActivity().getPackageManager();
		// isDataChanged = true;

		// Initializing Containers.
		appList = new ArrayList<>();
		childPermissions = new HashMap<>();
		// appList.clear();
		// childPermissions.clear();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (isDataChanged) {
			prepareList();
		}

		Log.d(TAG, "Ending onResume");
	}

	private void prepareList() {
		// Clear previous data.
		appList.clear();
		childPermissions.clear();

		new AsyncListGenerator().execute();

		Log.d(TAG, "Preparing list");

	}

	private boolean isSystemPackage(PackageInfo packageInfo) {
		return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
	}

	private class CustomComparator implements Comparator<ApplicationInfo> {
		@Override
		public int compare(ApplicationInfo l, ApplicationInfo r) {
			return pkgManager.getApplicationLabel(l).toString().compareToIgnoreCase(pkgManager.getApplicationLabel(r).toString());
		}
	}

	private class AsyncListGenerator extends AsyncTask<Void, Void, Void> {

		ProgressDialog loading = new ProgressDialog(getActivity());
		PermissionScannerAdapter adapter;

		@Override
		protected void onPreExecute() {
			loading.setTitle("Scanning Permissions");
			loading.setMessage("Please Wait...");
			loading.show();
			loading.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			isDataChanged = false;
			List<ApplicationInfo> installedApps = pkgManager
					.getInstalledApplications(PackageManager.GET_META_DATA);
			Collections.sort(installedApps, new CustomComparator());

			PermissionGetter permissionGetter = new PermissionGetter(
					pkgManager, PermissionScannerFragment.this.getActivity());

			for (ApplicationInfo appInfo : installedApps) {

				PackageInfo pkgInfo;
				try {
					pkgInfo = pkgManager.getPackageInfo(appInfo.packageName,
							PackageManager.GET_PERMISSIONS);

					List<PermissionData> reqPermissions = new ArrayList<>();

					if (!isSystemPackage(pkgInfo)
							&& !(pkgInfo.applicationInfo.loadLabel(pkgManager)
							.equals(getResources().getString(
									R.string.app_name)))) {

						appList.add(pkgInfo.packageName);

						// Log.d(TAG, pkgInfo.packageName);

						String[] appPermissions = pkgInfo.requestedPermissions;

						// Log.d(TAG, "Adding " + appPermissions[0]);

						if (appPermissions != null) {

							for (String permission : appPermissions) {
								// Log.d(TAG, permission);
								if (permissionGetter
										.generatePermissionData(permission) != null) {
									reqPermissions
											.add(permissionGetter
													.generatePermissionData(permission));

									// Log.d(TAG,
									// "Adding permissions to reqPermissions");

								}
							}

						} else {
							reqPermissions.add(new PermissionData(
									"No Permission", "No Permission",
									"No Permission", "No Permission", 0));
						}

						childPermissions.put(pkgInfo.packageName,
								reqPermissions);
						// Log.d(TAG, "Adding childItem");
					}

				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			Log.d(TAG, "returning");
			adapter = new PermissionScannerAdapter(
					getActivity(), appList, childPermissions);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Setting Adapters for the list view
			lvPermissionList.setAdapter(adapter);
			loading.dismiss();
			super.onPostExecute(result);
		}

	}

}
