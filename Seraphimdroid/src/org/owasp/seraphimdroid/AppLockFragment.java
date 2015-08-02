package org.owasp.seraphimdroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.owasp.seraphimdroid.adapter.AppLockerAdapter;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AppLockFragment extends Fragment {

	private ListView lvAppLockerList;
	private PackageManager pm;
	private List<String> appList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_application_locker,
				container, false);

		lvAppLockerList = (ListView) view.findViewById(R.id.lv_app_locker);
		appList = new ArrayList<String>();
		prepareList();

		// DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		// Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
		// "SELECT * FROM locks", null);

		lvAppLockerList
				.setAdapter(new AppLockerAdapter(getActivity(), appList));

		return view;
	}

	public static boolean isSystemPackage(PackageInfo packageInfo) {
		return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
				: false;
	}

	private void prepareList() {

		pm = getActivity().getPackageManager();
		List<AppContainer> container = new ArrayList<AppLockFragment.AppContainer>();
		List<PackageInfo> installedAppList = pm.getInstalledPackages(0);
		for (PackageInfo info: installedAppList) {
			if(pm.getLaunchIntentForPackage(info.packageName)!=null) {
				container.add(new AppContainer(pm.getApplicationLabel(info.applicationInfo).toString(),info.packageName));
			}
		}
//		container.add(new AppContainer("Install/Uninstall", "com.android.packageinstaller"));
		Collections.sort(container,
				new CustomCompare());
		
		for (AppContainer ri : container) {
			if (ri.appPackageName
					.equals("org.owasp.seraphimdroid"))
				continue;
			appList.add(ri.appPackageName);
		}
	}
	
	public class AppContainer {
		String appLabel,appPackageName;
		public AppContainer(String label, String packageName) {
			this.appLabel = label;
			this.appPackageName = packageName;
		}
	}
	
	private class CustomCompare implements Comparator<AppContainer> {

		@Override
		public int compare(AppContainer l, AppContainer r) {
			return l.appLabel.compareToIgnoreCase(r.appLabel);
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
//		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

//	private boolean addToDatabase(String pkgName) {
//		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
//		String sql = "SELECT * FROM locks where package_name=\'" + pkgName
//				+ "\'";
//
//		Cursor cursor = db.rawQuery(sql, null);
//
//		if (cursor.moveToNext()) {
//			cursor.close();
//			db.close();
//			dbHelper.close();
//			return false;
//
//		} else {
//
//			ContentValues cv = new ContentValues();
//			cv.put("package_name", pkgName);
//			cv.put("locked", 0);
//			db.insert(DatabaseHelper.TABLE_LOCKS, null, cv);
//			cursor.close();
//			db.close();
//			dbHelper.close();
//			return true;
//		}
//
//	}
}
