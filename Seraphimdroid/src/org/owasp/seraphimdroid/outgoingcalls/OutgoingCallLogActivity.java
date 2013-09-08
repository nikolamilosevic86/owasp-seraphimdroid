package org.owasp.seraphimdroid.outgoingcalls;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.owasp.seraphimdroid.R;


public class OutgoingCallLogActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_outgoing_call_log);

		// Add redialButton onClickListener listener
		Button redialButton = (Button) findViewById(R.id.redialButton);
		redialButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse(
						"tel:"
						+ CallLogPrototype.getLastCallAttempt().getNumber()
				));

				OutgoingCallInterceptor.allowAll = true;    // TEMP: allow calls
				startActivity(callIntent);                  // call
			}
		});
	}
}