package org.owasp.seraphimdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.owasp.seraphimdroid.R;

public class PermissionScannerFragment extends Fragment {

	private ExpandableListView lvPermissionList;

	private PackageManager pkgManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_permission_scanner,
				container, false);

		lvPermissionList = (ExpandableListView) view
				.findViewById(R.id.permissions_list);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		pkgManager = getActivity().getPackageManager();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
