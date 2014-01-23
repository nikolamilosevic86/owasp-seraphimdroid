package org.owasp.seraphimdroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PermissionDescription extends Activity {

	private TextView tvPermissionHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission_description);
		
		//Initializing views.
		tvPermissionHeader = (TextView) this.findViewById(R.id.tvPermissionHeader);	
		
		//Getting required Data
		String permissionName = getIntent().getStringExtra("PERMISSION_NAME");
		
		//Setting view properties.
		tvPermissionHeader.setText(permissionName);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
