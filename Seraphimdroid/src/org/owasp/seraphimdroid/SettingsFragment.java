package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SettingsFragment extends PreferenceFragment {

//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//
//		View view = inflater.inflate(R.layout.fragment_settings, container,
//				false);
//
//		LinearLayout layoutChangePassword = (LinearLayout) view
//				.findViewById(R.id.layout_change_password);
//		layoutChangePassword.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				changePassword(view);
//			}
//		});
//
//		return view;
//
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_fragment);

	}

	public void changePassword(View view) {
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS password");
		db.execSQL(DatabaseHelper.createPasswordTable);
		Intent pIntent = new Intent(getActivity(), PasswordActivity.class);

		pIntent.putExtra("PACKAGE_NAME", this.getActivity().getPackageName());
		startActivity(pIntent);
	}

}
