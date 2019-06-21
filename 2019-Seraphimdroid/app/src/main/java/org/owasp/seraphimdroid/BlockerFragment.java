package org.owasp.seraphimdroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import org.owasp.seraphimdroid.adapter.TabsPagerAdapter;

public class BlockerFragment extends Fragment implements OnPageChangeListener,
		OnTabChangeListener {

	private TabHost tabHost;
	private ViewPager viewPager;
	private int tabNo = 5;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			tabNo = getActivity().getIntent().getIntExtra("TAB_NO", 5);
		} catch (Exception e) {
			e.printStackTrace();
		}

		View view = inflater.inflate(R.layout.fragment_blocker, container,
				false);

		tabHost = (TabHost) view.findViewById(R.id.tabhost);
		tabHost.setup();
		initTabs();

		viewPager = (ViewPager) view.findViewById(R.id.viewpager);
		viewPager.setAdapter(new TabsPagerAdapter(getActivity()
				.getSupportFragmentManager()));
		viewPager.setOnPageChangeListener(this);

		tabHost.getTabWidget().setGravity(Gravity.BOTTOM);
		tabHost.getTabWidget().getChildAt(0);

		if (tabNo < 3) {
			viewPager.setCurrentItem(tabNo);
			tabHost.setCurrentTab(tabNo);
		}
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);

	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.clear_record:
//			DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
//			SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//			int currentTab = tabHost.getCurrentTab();
//			if (currentTab == 0) {
//				db.execSQL("DROP TABLE IF EXISTS "
//						+ DatabaseHelper.TABLE_CALL_LOGS);
//				db.execSQL(DatabaseHelper.createCallTable);
//				viewPager.setCurrentItem(1);
//				tabHost.setCurrentTab(1);
//				viewPager.setCurrentItem(0);
//				tabHost.setCurrentTab(0);
//			} else if (currentTab == 1) {
//				db.execSQL("DROP TABLE IF EXISTS "
//						+ DatabaseHelper.TABLE_SMS_LOGS);
//				db.execSQL(DatabaseHelper.createSMSTable);
//				viewPager.setCurrentItem(0);
//				tabHost.setCurrentTab(0);
//				viewPager.setCurrentItem(1);
//				tabHost.setCurrentTab(1);
//			} else if (currentTab == 2) {
//				db.execSQL("DROP TABLE IF EXISTS "
//						+ DatabaseHelper.TABLE_USSD_LOGS);
//				db.execSQL(DatabaseHelper.createUSSDTable);
//				viewPager.setCurrentItem(1);
//				tabHost.setCurrentTab(1);
//				viewPager.setCurrentItem(2);
//				tabHost.setCurrentTab(2);
//			}
//			db.close();
//			dbHelper.close();
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.menu_blocker, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void initTabs() {
		AddTab(getActivity(), tabHost, tabHost.newTabSpec("call_log")
				.setIndicator("Call Logs"));
		AddTab(getActivity(), tabHost, tabHost.newTabSpec("sms_log")
				.setIndicator("SMS Logs"));
		AddTab(getActivity(), tabHost, tabHost.newTabSpec("ussd_log")
				.setIndicator("USSD Logs"));

		tabHost.setOnTabChangedListener(this);
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onTabChanged(String tag) {
		int position = tabHost.getCurrentTab();
		viewPager.setCurrentItem(position);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		int position = viewPager.getCurrentItem();
		// getActivity().getActionBar().setSelectedNavigationItem(position);
		tabHost.setCurrentTab(position);

	}

	// Method to add a TabHost
	private static void AddTab(Activity activity, TabHost tabHost,
			TabHost.TabSpec tabSpec) {
		tabSpec.setContent(new MyTabFactory(activity));
		tabHost.addTab(tabSpec);
	}

}