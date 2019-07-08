package org.owasp.seraphimdroid;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.owasp.seraphimdroid.adapter.AppLockerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

}
