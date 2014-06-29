package org.owasp.seraphimdroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class GeoFencingFragment extends Fragment {

	private GoogleMap googleMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_geofencing, container,
				false);

		googleMap = ((MapFragment) getActivity().getFragmentManager()
				.findFragmentById(R.id.maps)).getMap();

		return view;
	}

}
