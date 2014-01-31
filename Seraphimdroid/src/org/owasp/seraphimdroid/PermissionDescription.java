package org.owasp.seraphimdroid;

import java.util.concurrent.ExecutionException;

import org.owasp.seraphimdroid.customclasses.PermissionData;
import org.owasp.seraphimdroid.customclasses.AsyncPermissionGetter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PermissionDescription extends Activity {

	private TextView tvPermissionHeader;
	private TextView tvPermissionDescription;
	private TextView tvMaliciousUseDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission_description);

		// Initializing views.
		tvPermissionHeader = (TextView) this
				.findViewById(R.id.tvPermissionHeader);
		tvPermissionDescription = (TextView) this
				.findViewById(R.id.tvPermissionDescription);
		tvMaliciousUseDescription = (TextView) this
				.findViewById(R.id.tvMaliciousUseDescription);

		// Getting required Data
		String permission = getIntent().getStringExtra("PERMISSION_NAME");
		PermissionData pd = null;
		try {
			pd = new AsyncPermissionGetter().execute(permission).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
