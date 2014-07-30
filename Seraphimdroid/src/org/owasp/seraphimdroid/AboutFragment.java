package org.owasp.seraphimdroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

	private TextView tvDevNames;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		tvDevNames = (TextView) view.findViewById(R.id.tv_dev_names);
		String devNames = "Nikola Milosevic \nFurquan Ahmed\n ";
		tvDevNames.setText(devNames);

		return view;
	}

}
