package org.owasp.seraphimdroid;

import org.owasp.seraphimdroid.model.PermissionData;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PermissionDescription extends Activity {

	private TextView tvPermissionHeader;
	private TextView tvPermissionDescription;
	private TextView tvMaliciousUseDescription;
	
	private PermissionGetter permissionGetter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission_description);
		this.setFinishOnTouchOutside(false);
		this.setTitle("Description");

		// Initializing views.
		tvPermissionHeader = (TextView) this
				.findViewById(R.id.tvPermissionHeader);
		tvPermissionDescription = (TextView) this
				.findViewById(R.id.tvPermissionDescription);
		tvMaliciousUseDescription = (TextView) this
				.findViewById(R.id.tvMaliciousUseDescription);
		
		permissionGetter = new PermissionGetter(getPackageManager(), this);

		// Getting required Data
		String permission = getIntent().getStringExtra("PERMISSION_NAME");
		PermissionData pd = null;
		pd = permissionGetter.generatePermissionData(permission);

		// Setting view properties.
		if (pd != null) {
			tvPermissionHeader.setText(pd.getPermissionName());
			tvPermissionDescription.setText(pd.getDescription());
			tvMaliciousUseDescription.setText(pd.getMaliciousUseDescription());
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
