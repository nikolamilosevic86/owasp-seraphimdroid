package org.owasp.seraphimdroid;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.owasp.seraphimdroid.R;

public class BlockerFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_blocker, container,
				false);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

}
