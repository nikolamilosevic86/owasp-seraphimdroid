package org.owasp.seraphimdroid;

import java.util.ArrayList;
import java.util.List;

import org.owasp.seraphimdroid.database.DatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomUSSDActivity extends Activity {

	private DatabaseHelper dbHelper;
	private ListView lvUSSDLogs;
	private CustomUSSDAdapter adapter;
	List<BlockedUSSD> blockedUSSDList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_ussd);
		initAddButton();
		
		dbHelper = new DatabaseHelper(CustomUSSDActivity.this);
		lvUSSDLogs = (ListView) findViewById(R.id.ussd);

		initListView();
		
	}
	
	private void initListView() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String sql = "SELECT * from " + DatabaseHelper.TABLE_BLOCKED_USSD
				+ " ORDER BY _id";
		Cursor cursor = db.rawQuery(sql, null);

		blockedUSSDList = new ArrayList<BlockedUSSD>();
	    
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BlockedUSSD bl = new BlockedUSSD(cursor.getString(1), cursor.getString(2), cursor.getString(3));
                bl.setId(cursor.getInt(0));
                blockedUSSDList.add(bl);
            } while (cursor.moveToNext());
        }
		
		adapter = new CustomUSSDAdapter(getBaseContext(), 0);
		lvUSSDLogs.setAdapter(adapter);
	}
	
	private void initAddButton() {		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				CustomUSSDActivity.this);
		View promptsView = LayoutInflater.from(getBaseContext()).inflate(R.layout.custom_ussd_add_dialog, null);
		
		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText USSDNumber = (EditText) promptsView
				.findViewById(R.id.ussd_number);
		final EditText USSDDescription = (EditText) promptsView
				.findViewById(R.id.ussd_desc);
		
		// set dialog message
		alertDialogBuilder
	    .setTitle("Block USSD Code")
	    .setMessage("Please provide Number & Description blow")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				BlockedUSSD bl = new BlockedUSSD(USSDNumber.getText().toString(), USSDDescription.getText().toString(), "custom");
				ContentValues cv = new ContentValues();
	    		cv.put("number", bl.number);
	    		cv.put("desc", bl.desc);
	    		cv.put("type", "custom");
	    		db.insert(DatabaseHelper.TABLE_BLOCKED_USSD, null, cv);
	    		db.close();
	    		Toast.makeText(getBaseContext(), "USSD Code added Successfully", Toast.LENGTH_SHORT).show();
	    		initListView();
	        }
	     })
	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            dialog.cancel();
	        }
	     })
	    .setIcon(R.drawable.ic_launcher_smal);
		
		// create alert dialog
		final AlertDialog alertDialog = alertDialogBuilder.create();
		
		//Add Listener to "Add" button
		((ImageView) findViewById(R.id.add)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alertDialog.show();
			}
		});
	}
	
	class CustomUSSDAdapter extends ArrayAdapter<String> {

		private Context context;
		float imageDimension;
		private AlertDialog alertDialog;
		
		class ViewHolder {
			TextView numberTv,descriptionTv;
			ImageView editButtonIv,deleteButtonIv;
		}
		
		public CustomUSSDAdapter(Context context, int resource) {
			super(context, resource);
			this.context = context;
			imageDimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		}
		
		@Override
		public int getCount() {
			return blockedUSSDList.size();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder view_holder = null;
			LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if(convertView==null) {
				convertView = inflator.inflate(R.layout.custom_ussd_item, parent, false);
				view_holder = new ViewHolder();
				
				view_holder.numberTv = (TextView) convertView.findViewById(R.id.blocked_ussd_number);
				view_holder.descriptionTv = (TextView) convertView.findViewById(R.id.blocked_ussd_desc);
				view_holder.editButtonIv = (ImageView) convertView.findViewById(R.id.edit);
				view_holder.deleteButtonIv = (ImageView) convertView.findViewById(R.id.delete);
				convertView.setTag(view_holder);
			}
			else {
				view_holder = (ViewHolder) convertView.getTag();
			}
			
			final BlockedUSSD bl = blockedUSSDList.get(position);
			view_holder.numberTv.setText(bl.number);
			view_holder.descriptionTv.setText(bl.desc);
			
			if(bl.type.equals("default")) {
				view_holder.editButtonIv.setBackgroundResource(R.drawable.icon_edit_disabled);
				view_holder.editButtonIv.setOnClickListener(null);
				view_holder.deleteButtonIv.setBackgroundResource(R.drawable.icon_delete_disabled);
				view_holder.deleteButtonIv.setOnClickListener(null);
			}
			else {
				view_holder.editButtonIv.setBackgroundResource(R.drawable.icon_edit);
				view_holder.editButtonIv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						createDialog(position);
						alertDialog.show();
					}
				});
				
				view_holder.deleteButtonIv.setBackgroundResource(R.drawable.icon_delete);
				view_holder.deleteButtonIv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						final Builder builder = new AlertDialog.Builder(CustomUSSDActivity.this);
						builder
					    .setTitle("Confirm Delete")
					    .setMessage("Are you sure you want to Delete?")
					    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) {
					        	SQLiteDatabase db = dbHelper.getWritableDatabase();
								db.delete(DatabaseHelper.TABLE_BLOCKED_USSD, "_id" + "=" + bl.getId(), null);
								db.close();
								Toast.makeText(context, "USSD Code removed Successfully", Toast.LENGTH_SHORT).show();
								initListView();
					        }
					     })
					    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            dialog.cancel();
					        }
					     })
					    .setIcon(R.drawable.ic_launcher_smal)
					    .create().show();

					}
				});
			}
			
			view_holder.editButtonIv.getLayoutParams().width = (int) imageDimension;
			view_holder.editButtonIv.getLayoutParams().height = (int) imageDimension;
			view_holder.editButtonIv.requestLayout();
			
			return convertView;
		}
		
		private void createDialog(final int id) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					CustomUSSDActivity.this);
			View inputView = LayoutInflater.from(getBaseContext()).inflate(R.layout.custom_ussd_add_dialog, null);
			
			alertDialogBuilder.setView(inputView);
			final BlockedUSSD bl = blockedUSSDList.get(id);
			
			final EditText USSDNumberEdit = (EditText) inputView
					.findViewById(R.id.ussd_number);
			USSDNumberEdit.setText(bl.number);
			final EditText USSDDescriptionEdit = (EditText) inputView
					.findViewById(R.id.ussd_desc);
			USSDDescriptionEdit.setText(bl.desc);
			
			// set dialog message
			alertDialogBuilder
		    .setTitle("Edit USSD Code")
		    .setMessage("Please provide Number & Description blow")
		    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					ContentValues cv = new ContentValues();
		    		cv.put("number", USSDNumberEdit.getText().toString());
		    		cv.put("desc", USSDDescriptionEdit.getText().toString());
					db.update(DatabaseHelper.TABLE_BLOCKED_USSD, cv, "_id" + "=" + bl.getId(), null);
					db.close();
					Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
					initListView();
		        }
		     })
		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.cancel();
		        }
		     })
		    .setIcon(R.drawable.ic_launcher_smal);
			
			// create alert dialog
			alertDialog = alertDialogBuilder.create();

		}
	}
	
	public class BlockedUSSD {
		private int id;
		public String number,desc,type;
		public BlockedUSSD(String n,String d,String t) {
			this.number = n;
			this.desc = d;
			this.type = t;
		}
		public int getId() {
			return this.id;
		}
		public void setId(int id) {
			this.id = id;
		}
	}
	
}
