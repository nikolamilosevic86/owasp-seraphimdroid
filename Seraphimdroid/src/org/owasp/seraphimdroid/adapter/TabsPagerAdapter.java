package org.owasp.seraphimdroid.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.owasp.seraphimdroid.CallLogFragment;
import org.owasp.seraphimdroid.SMSLogFragment;
import org.owasp.seraphimdroid.USSDLogFragment;

public class TabsPagerAdapter extends FragmentStatePagerAdapter{

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			// Top Rated fragment activity
			return new CallLogFragment();
		case 1:
			// Games fragment activity
			return new SMSLogFragment();

		case 2:
			// Movies fragment activity
			return new USSDLogFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

}
