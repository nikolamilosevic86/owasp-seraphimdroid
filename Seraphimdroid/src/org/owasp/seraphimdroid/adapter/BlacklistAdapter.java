package org.owasp.seraphimdroid.adapter;

import java.io.InputStream;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.database.DatabaseHelper;
import org.owasp.seraphimdroid.receiver.CallRecepter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class BlacklistAdapter extends CursorAdapter {

	private Context mContext;

	public BlacklistAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tvName = (TextView) view
				.findViewById(R.id.tv_blacklist_contact_name);
		TextView tvNumber = (TextView) view
				.findViewById(R.id.tv_blacklist_contact_number);
		ImageView imgContactIcon = (ImageView) view
				.findViewById(R.id.img_blacklist_contact_icon);
		ImageButton imgDelete = (ImageButton) view
				.findViewById(R.id.img_delete_number);

		final String number = cursor.getString(1);
		imgDelete.setTag(number);

		if (CallRecepter.contactExists(context, number)) {
			String name = null;
			String contactId = null;
			InputStream input = null;

			// Columns for the query.
			String[] projection = new String[] {
					ContactsContract.PhoneLookup.DISPLAY_NAME,
					ContactsContract.PhoneLookup._ID };

			// Encode phone number to build the uri
			Uri contactUri = Uri.withAppendedPath(
					ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(number));

			// Placing query
			Cursor c = context.getContentResolver().query(contactUri,
					projection, null, null, null);
			if (c.moveToFirst()) {
				// Get values from the contacts database
				contactId = c.getString(c
						.getColumnIndex(ContactsContract.PhoneLookup._ID));
				name = c.getString(c
						.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

				// Get phone stream for the contact photo.
				Uri photoUri = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI,
						Long.parseLong(contactId));
				input = ContactsContract.Contacts.openContactPhotoInputStream(
						context.getContentResolver(), photoUri);
			}
			
			c.close();
			if (name != null && !name.equals("")) {
				tvName.setText(name);
			} else
				tvName.setText("<no name>");
			if (input != null) {
				imgContactIcon
						.setImageBitmap(BitmapFactory.decodeStream(input));
			}else {
				imgContactIcon.setImageResource(R.drawable.contact);
			}

		}

		tvNumber.setText(number);
		imgDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (view.getTag().equals(number)) {
					DatabaseHelper dbHelper = new DatabaseHelper(mContext);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					db.delete(DatabaseHelper.TABLE_BLACKLIST, "number=?",
							new String[] { number });
					BlacklistAdapter.this.notifyDataSetInvalidated();
					BlacklistAdapter.this.swapCursor(db.rawQuery(
							"SELECT * FROM " + DatabaseHelper.TABLE_BLACKLIST,
							null));
					BlacklistAdapter.this.notifyDataSetChanged();
				}
			}
		});

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.blacklist_item, parent, false);
	}

}
