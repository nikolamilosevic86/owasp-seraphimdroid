package org.owasp.seraphimdroid;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.owasp.seraphimdroid.adapter.DrawerAdapter;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.model.DrawerItem;
import org.owasp.seraphimdroid.receiver.ApplicationInstallReceiver;
import org.owasp.seraphimdroid.receiver.SettingsCheckAlarmReceiver;
import org.owasp.seraphimdroid.services.CheckAppLaunchThread;
import org.owasp.seraphimdroid.services.OutGoingSmsRecepter;
import org.owasp.seraphimdroid.services.ServicesLockService;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

	private CharSequence title, drawerTitle;

	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;

	private String[] itemNames;
	private ArrayList<DrawerItem> listItems;
	private TypedArray iconList;

	private DrawerAdapter adapter;

	private boolean isUnlocked = false;
	
	private Fragment prevSupportFlag = null;
	
	public static boolean shouldReceive = true;

	private int fragmentNo = 3;

	@Override
	protected void onResume() {
		if (!isUnlocked) {
			Intent pwdIntent = new Intent(this, PasswordActivity.class);
			pwdIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pwdIntent.putExtra("PACKAGE_NAME", this.getPackageName());
			startActivity(pwdIntent);
			isUnlocked = true;
			selectFragment(fragmentNo);
		}
		else {
			if(PasswordActivity.lastUnlocked!=null && PasswordActivity.lastUnlocked.equals(getPackageName())==false) {
				finish();
			}
		}
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handler handler = new Handler(this.getMainLooper());
		CheckAppLaunchThread launchChecker = new CheckAppLaunchThread(handler,
				getApplicationContext());
		if (launchChecker.isAlive() == false) {
			launchChecker.start();
		}

	}

	int UNINSTALL_REQUEST_CODE = 1;
	private android.app.Fragment prevFrag = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		//Initiate Services and Receivers
		startService(new Intent(this, OutGoingSmsRecepter.class));
		startService(new Intent(this, ApplicationInstallReceiver.class));
		startService(new Intent(this, ServicesLockService.class));
		
		SharedPreferences defaults = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		
		//Alarm Manager for Settings Check
		alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getBaseContext(), SettingsCheckAlarmReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
		
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
		calendar.set(java.util.Calendar.MINUTE, 0);

		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
		        defaults.getInt("settings_interval", 24*60*60*1000), alarmIntent);
		
		//Set SIM id if not set
		TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String hash = null;
		if(telephony.getSimSerialNumber()!=null) {
			hash = telephony.getSimSerialNumber() + telephony.getNetworkOperator() + telephony.getNetworkCountryIso();
		}
		if(hash!=null && defaults.contains("sim_1")==false) {
			defaults.edit().putString("sim_1", hash).commit();
		}
		
		//App Uninstall Lock
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && !isUsageAccessEnabled()) {
	    	 defaults.edit().putBoolean("uninstall_locked", false).commit();
	     }
	     else {
	    	 defaults.edit().putBoolean("uninstall_locked", true).commit();
	     }
		Boolean isUninstallLocked = defaults.getBoolean("uninstall_locked", true);
		if(isUninstallLocked) {
			String pkgName = "com.android.packageinstaller";
			DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor cursor = db.rawQuery(
					"SELECT * FROM locks WHERE package_name=\'" + pkgName
						+ "\'", null);
			if (!cursor.moveToNext()) {
				ContentValues cv = new ContentValues();
				cv.put("package_name", pkgName);
				db.insert(DatabaseHelper.TABLE_LOCKS, null, cv);
			}
			cursor.close();
			db.close();
			dbHelper.close();
		}
		
		
		boolean callsBlocked = defaults.getBoolean("call_blocked_notification",
				true);
		defaults.edit().putBoolean("call_blocked_notification", callsBlocked)
				.commit();

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

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == UNINSTALL_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	            Log.d("TAG", "onActivityResult: user accepted the (un)install");
	        } else if (resultCode == RESULT_CANCELED) {
	            Log.d("TAG", "onActivityResult: user canceled the (un)install");
	        } else if (resultCode == RESULT_FIRST_USER) {
	            Log.d("TAG", "onActivityResult: failed to (un)install");
	        }
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private Boolean isUsageAccessEnabled() {
		try {
		   PackageManager packageManager = getPackageManager();
		   ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
		   AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
		   int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
		   return (mode == AppOpsManager.MODE_ALLOWED);

		} catch (PackageManager.NameNotFoundException e) {
		   return false;
		}
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
		listItems.add(new DrawerItem(itemNames[6], iconList.getResourceId(6,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[7], iconList.getResourceId(7,
				R.drawable.ic_launcher)));
		
		iconList.recycle();
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
			fragment = new SettingsCheckerFragment();
			break;
		case 2:
			fragment = new BlockerFragment();
			break;
		case 3:
			fragment = new AppLockFragment();
			break;
		case 4:
			fragment = new ServiceLockFragment();
			break;
		case 5:
			fragment = new GeoFencingFragment();
			break;
		case 6: {

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
		case 7:
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
			FragmentTransaction trans = fragMan.beginTransaction();
			trans.replace(R.id.fragment_container, fragment);
			trans.addToBackStack(null);
			trans.commit();

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
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		showExitAlert();
	}

	private void showExitAlert() {
		AlertDialog.Builder exitBuilder = new AlertDialog.Builder(MainActivity.this);
		exitBuilder.setTitle("Close application");
		exitBuilder.setMessage("Do you really want to exit the application?");
		exitBuilder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
		exitBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
						MainActivity.this.finish();
					}
				});
		exitBuilder.setIcon(R.drawable.ic_launcher_smal);
		exitBuilder.show();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

}
