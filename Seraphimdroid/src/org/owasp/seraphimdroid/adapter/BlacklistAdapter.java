package org.owasp.seraphimdroid.adapter;

import org.owasp.seraphimdroid.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BlacklistAdapter extends CursorAdapter {

	public BlacklistAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tvName = (TextView) view
				.findViewById(R.id.tv_blacklist_contact_name);
		TextView tvNumber = (TextView) view
				.findViewById(R.id.tv_blacklist_contact_number);
		ImageView imgContactIcon = (ImageView) view
				.findViewById(R.id.img_blacklist_contact_icon);

		
		tvName.setText("<no name>");
		tvNumber.setText(cursor.getString(1));
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.blacklist_item, parent, false);
	}

}
