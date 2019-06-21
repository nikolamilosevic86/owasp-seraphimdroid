package org.owasp.seraphimdroid;
//package org.anothermonitor;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.seraphimdroid.adapter.DrawerAdapter;
import org.owasp.seraphimdroid.helper.ConnectionHelper;
import org.owasp.seraphimdroid.helper.DatabaseHelper;
import org.owasp.seraphimdroid.model.DrawerItem;
import org.owasp.seraphimdroid.receiver.ApplicationInstallReceiver;
import org.owasp.seraphimdroid.receiver.SettingsCheckAlarmReceiver;
import org.owasp.seraphimdroid.services.CheckAppLaunchThread;
import org.owasp.seraphimdroid.services.OutGoingSmsRecepter;
import org.owasp.seraphimdroid.services.ServicesLockService;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

	private static boolean isUnlocked = false;

	private Fragment prevSupportFlag = null;

	public static boolean shouldReceive = true;

	private int fragmentNo = 3;

	@Override
	protected void onResume() {
		if (!isUnlocked) {
			Intent pwdIntent = new Intent(this, org.owasp.seraphimdroid.PasswordActivity.class);
			pwdIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pwdIntent.putExtra("PACKAGE_NAME", this.getPackageName());
			if(fragmentNo == 6){
				selectFragment(fragmentNo);
			} else {
				startActivity(pwdIntent);
				isUnlocked = true;
				selectFragment(fragmentNo);
			}
		}
		else {
			if(org.owasp.seraphimdroid.PasswordActivity.lastUnlocked!=null && org.owasp.seraphimdroid.PasswordActivity.lastUnlocked.equals(getPackageName())==false) {
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
		if (!launchChecker.isAlive()) {
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
		CopyGuideAssets();

		boolean isFirstTimeUser = defaults.getBoolean("first_time_user", true);
		if (isFirstTimeUser) {
			AlertDialog.Builder builder;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
			} else {
				builder = new AlertDialog.Builder(MainActivity.this);
			}
			String alert_message = "This application requires Admin Permissions for GeoFencing feature, specifically for enabling user-controlled remote wipe ";
			builder.setTitle("Admin Permissions Usage")
					.setMessage(alert_message)
					.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
			defaults.edit().putBoolean("first_time_user", false).apply();
		}

		//Alarm Manager for Settings Check
		alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		final Intent intent = new Intent(getBaseContext(), SettingsCheckAlarmReceiver.class);
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
			defaults.edit().putString("sim_1", hash).apply();
		}

		//App Uninstall Lock
		if (android.os.Build.VERSION.SDK_INT >= 21 && !isUsageAccessEnabled()) {
			defaults.edit().putBoolean("uninstall_locked", false).apply();
		}
		else {
			defaults.edit().putBoolean("uninstall_locked", true).apply();
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
				.apply();

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
		listItems = new ArrayList<>();
		iconList = getResources().obtainTypedArray(R.array.drawer_icons);

		populateList();

		adapter = new DrawerAdapter(this, listItems);
		drawerList.setAdapter(adapter);
		drawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				if (position == 6){
					getIntent().removeExtra("tags");
					selectFragment(6);
				} else{
					selectFragment(position);
				}
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
			int mode = appOpsManager.checkOpNoThrow( "android:get_usage_stats", applicationInfo.uid, applicationInfo.packageName);
			return (mode == AppOpsManager.MODE_ALLOWED);

		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	// Setter for 'unlocked' variable
	public static void setUnlocked(boolean unlocked) {
		isUnlocked = unlocked;
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
		listItems.add(new DrawerItem(itemNames[8], iconList.getResourceId(8,
				R.drawable.ic_launcher)));
		listItems.add(new DrawerItem(itemNames[9], iconList.getResourceId(9,
				R.drawable.ic_launcher)));

		iconList.recycle();
	}

	public void setTitle(CharSequence title) {
		this.title = title;
		getActionBar().setTitle(title);
	}

	private void recordUsage(int id) {
		ConnectionHelper ch = new ConnectionHelper(getApplicationContext());
		DatabaseHelper db = new DatabaseHelper(this);
		if(ch.isConnectingToInternet()){
			sendUsage(id);
		}else{
			db.addFeatureUsage(id);
		}
	}

	private void sendUsage(final int id){
		final String addurl = "http://educate-seraphimdroid.rhcloud.com/features/"+Integer.toString(id)+"/use.json";
		final DatabaseHelper db = new DatabaseHelper(this);
		int usage = db.getFeatureUsage(id);
		JSONObject header = new JSONObject();
		try {
			header.put("Content-Type", "application/json");
		} catch (JSONException e) {
			Log.d("", "sendUsage: Hellno");
		}
		if (usage > 0) {
			final String body = "{ \"many\" : " + usage + " }";
			JsonObjectRequest addManyUsage = new JsonObjectRequest(Request.Method.PUT, addurl, header,
					new Response.Listener<JSONObject>() {
						String resp;
						@Override
						public void onResponse(JSONObject response) {
							try { resp = response.getString("status"); } catch (JSONException e) { Toast.makeText(MainActivity.this, "Some Error Occured.", Toast.LENGTH_SHORT).show(); }
							if (resp != null && resp.equals("ok")) {
								Log.i("Usage", "onResponse: Analyzed");
								db.removeFeatureUsage(id);
							}
						}
					}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.d("TAG", "onErrorResponse: "+"Error Response");
				}
			}) {
				@NonNull
				@Override
				public byte[] getBody() {
					return body.getBytes();
				}
			};
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			requestQueue.add(addManyUsage);
		} else {
			JsonObjectRequest addUsage = new JsonObjectRequest(Request.Method.PUT, addurl, header,
					new Response.Listener<JSONObject>() {
						String resp;
						@Override
						public void onResponse(JSONObject response) {
							try { resp = response.getString("status"); } catch (JSONException e) { Toast.makeText(MainActivity.this, "Some Error Occured.", Toast.LENGTH_SHORT).show(); }
							if (resp != null && resp.equals("ok")) {
								Log.i("Usage", "onResponse: Analyzed");
							}
						}
					}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.d("TAG", "onErrorResponse: "+"Error Response");
				}
			});

			RequestQueue requestQueue = Volley.newRequestQueue(this);
			requestQueue.add(addUsage);
		}
	}


	public void selectFragment(int position) {
		Fragment fragment = null;
		switch (position) {
			case 0:
				recordUsage(1);
				fragment = new org.owasp.seraphimdroid.PermissionScannerFragment();
				break;
			case 1:
				recordUsage(2);
				fragment = new org.owasp.seraphimdroid.SettingsCheckerFragment();
				break;
			case 2:
				recordUsage(3);
				fragment = new org.owasp.seraphimdroid.BlockerFragment();
				break;
			case 3:
				recordUsage(4);
				fragment = new org.owasp.seraphimdroid.AppLockFragment();
				break;
			case 4:
				recordUsage(5);
				fragment = new org.owasp.seraphimdroid.ServiceLockFragment();
				break;
			case 5:
				recordUsage(6);
				fragment = new org.owasp.seraphimdroid.GeoFencingFragment();
				break;
			case 6:
				recordUsage(7);
				fragment = new org.owasp.seraphimdroid.EducateFragment();
				break;
			case 7:
				//Intent intent = null;

				break;
			case 8: {
				if (prevSupportFlag != null) {
					FragmentManager fragMan = getSupportFragmentManager();
					fragMan.beginTransaction().remove(prevSupportFlag).commit();
				}
				android.app.Fragment frag = new org.owasp.seraphimdroid.SettingsFragment();
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
			case 9:
				fragment = new org.owasp.seraphimdroid.AboutFragment();
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
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
						MainActivity.setUnlocked(false);
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

	private void CopyGuideAssets() {
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		File file = new File(getFilesDir(), "userguide.pdf");
		try {
			in = assetManager.open("userguide.pdf");
			out = openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

			copyGuide(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;

		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}
	}

	private void copyGuide(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, read);
		}
	}

}
