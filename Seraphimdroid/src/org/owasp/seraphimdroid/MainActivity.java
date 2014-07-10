package org.owasp.seraphimdroid;

import java.util.ArrayList;

import org.owasp.seraphimdroid.adapter.DrawerAdapter;
import org.owasp.seraphimdroid.model.DrawerItem;
import org.owasp.seraphimdroid.services.CheckAppLaunchThread;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

	private CharSequence title, drawerTitle;

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;

	private String[] itemNames;
	private ArrayList<DrawerItem> listItems;
	private TypedArray iconList;

	private DrawerAdapter adapter;
	
	public static boolean isUnlocked = false;

	private Fragment prevSupportFlag = null;

	public static boolean shouldReceive = true;

	private static int fragmentNo = 2;

	@Override
	protected void onResume() {
		if (!isUnlocked) {
			Intent pwdIntent = new Intent(this, PasswordActivity.class);
			pwdIntent.putExtra("PACKAGE_NAME", this.getPackageName());
			isUnlocked = true;
			startActivity(pwdIntent);
			selectFragment(fragmentNo);
		}

		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// Handler handler = new Handler(this.getMainLooper());
		// CheckAppLaunchThread launchChecker = new
		// CheckAppLaunchThread(handler, getApplicationContext());
		// if(launchChecker.isAlive() == false){
		// launchChecker.start();
		// }
		super.onDestroy();
		Handler handler = new Handler(this.getMainLooper());
		CheckAppLaunchThread launchChecker = new CheckAppLaunchThread(handler,
				getApplicationContext());
		if (launchChecker.isAlive() == false) {
			launchChecker.start();
		}

	}

	private android.app.Fragment prevFrag = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// if (!isUnlocked) {
		// Intent pwdIntent = new Intent(this, PasswordActivity.class);
		// pwdIntent.putExtra("PACKAGE_NAME", this.getPackageName());
		// isUnlocked = true;
		// startActivity(pwdIntent);
		// }

		try {
			fragmentNo = getIntent().getIntExtra("FRAGMENT_NO", fragmentNo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// initializing variables.
		title = drawerTitle = getTitle();
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.drawer_list);

		itemNames = getResources().getStringArray(R.array.item_names);
		listItems = new ArrayList<DrawerItem>();
		iconList = getResources().obtainTypedArray(R.array.drawer_icons);

		populateList();

		adapter = new DrawerAdapter(this, listItems);
		drawerList.setAdapter(adapter);

		drawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				selectFragment(position);
			}
		});

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, android.R.string.ok, android.R.string.ok) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(title);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(drawerTitle);
				invalidateOptionsMenu();
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);

		// if (savedInstanceState == null) {
		// selectFragment(fragmentNo);
		// }

		// if (savedInstanceState != null) {
		// isUnlocked = savedInstanceState.getBoolean("ISUNLOCKED", false);
		// }

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// startService(new Intent(this, AppLockService.class));

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		// outState.putBoolean("ISUNLOCKED", isUnlocked);
	}

	private void populateList() {
		// populate the list.
		listItems.add(new DrawerItem(itemNames[0], iconList.getResourceId(0,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[1], iconList.getResourceId(1,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[2], iconList.getResourceId(2,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[3], iconList.getResourceId(3,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[4], iconList.getResourceId(4,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[5], iconList.getResourceId(5,
				R.drawable.ic_launcher)));

		iconList.recycle();
	}

	public void setTitle(CharSequence title) {
		this.title = title;
		getActionBar().setTitle(title);
	}

	public void selectFragment(int position) {
		Fragment fragment = null;
		// android.app.Fragment prevFrag = null;
		switch (position) {
		case 0:
			fragment = new PermissionScannerFragment();
			break;
		case 1:
			fragment = new BlockerFragment();
			break;
		case 2:
			fragment = new AppLockFragment();
			break;
		case 3:
			fragment = new GeoFencingFragment();
			break;
		case 4: {

			if (prevSupportFlag != null) {
				FragmentManager fragMan = getSupportFragmentManager();
				fragMan.beginTransaction().remove(prevSupportFlag).commit();
			}
			android.app.Fragment frag = new SettingsFragment();
			android.app.FragmentManager fm = getFragmentManager();
			fm.beginTransaction().replace(R.id.fragment_container, frag)
					.commit();

			drawerList.setItemChecked(position, true);
			drawerList.setSelection(position);
			setTitle(itemNames[position]);
			drawerLayout.closeDrawer(drawerList);
			prevFrag = frag;
		}
			break;
		case 5:
			fragment = new AboutFragment();
			break;
		default:
			break;
		}

		if (fragment != null) {
			if (prevFrag != null) {
				android.app.FragmentManager fm = getFragmentManager();
				fm.beginTransaction().remove(prevFrag).commit();
			}
			FragmentManager fragMan = getSupportFragmentManager();
			fragMan.beginTransaction()
					.replace(R.id.fragment_container, fragment).commit();

			drawerList.setItemChecked(position, true);
			drawerList.setSelection(position);
			setTitle(itemNames[position]);
			drawerLayout.closeDrawer(drawerList);
			prevSupportFlag = fragment;
		}
		fragmentNo = position;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
		// isUnlocked = true;
		// Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
		// .getDefaultDisplay();
		// int orientation = display.getRotation();
		// if(orientation == Surface.ROTATION_270 || orientation ==
		// Surface.ROTATION_270){
		//
		// }

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		isUnlocked = false;
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		// onPause();
		// onDestroy();
		finish();
		super.onBackPressed();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			// onPause();
			// onDestroy();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {

			// onPause();
			// onDestroy();
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

}
