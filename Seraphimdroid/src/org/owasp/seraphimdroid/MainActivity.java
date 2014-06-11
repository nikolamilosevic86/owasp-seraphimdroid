package org.owasp.seraphimdroid;

import java.util.ArrayList;

import org.owasp.seraphimdroid.adapter.DrawerAdapter;
import org.owasp.seraphimdroid.model.DrawerItem;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.owasp.seraphimdroid.R;


public class MainActivity extends FragmentActivity{

	private CharSequence title, drawerTitle;

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;

	private String[] itemNames;
	private ArrayList<DrawerItem> listItems;
	private TypedArray iconList;

	private DrawerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initializing variables.
		title = drawerTitle = getTitle();
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.drawer_list);

		itemNames = getResources().getStringArray(R.array.item_names);
		listItems = new ArrayList<DrawerItem>();
		iconList = getResources().obtainTypedArray(R.array.drawer_icons);

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

		if (savedInstanceState == null) {
			selectFragment(1);
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

	}

	public void setTitle(CharSequence title) {
		this.title = title;
		getActionBar().setTitle(title);
	}

	public void selectFragment(int position) {
		Fragment fragment = null;
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
		case 4:
			fragment = new SettingsFragment();
			break;
		case 5:
			fragment = new AboutFragment();
			break;
		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragMan = getSupportFragmentManager();
			fragMan.beginTransaction()
					.replace(R.id.fragment_container, fragment).commit();

			drawerList.setItemChecked(position, true);
			drawerList.setSelection(position);
			setTitle(itemNames[position]);
			drawerLayout.closeDrawer(drawerList);
		}

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
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

}
