package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.adapter.ServicesLockerAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ServicesLockFragment extends Fragment{

	private ListView lvServicesLockerList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_services_locker, container, false);
		
		lvServicesLockerList = (ListView) view.findViewById(R.id.lv_app_locker);
		lvServicesLockerList
				.setAdapter(new ServicesLockerAdapter(getActivity()));
		
		return view;		
	}
	
}
