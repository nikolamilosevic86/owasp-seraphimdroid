package org.owasp.seraphimdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutFragment extends Fragment {

	final int ProjectPageId = 1;
	final int GitHubId = 0;
	private TextView tvDevNames, tvGithub, tvProjectPage, tvGuide;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		tvDevNames = (TextView) view.findViewById(R.id.tv_dev_names);
		tvGithub = (TextView) view.findViewById(R.id.tv_project_code_link);
		tvGuide = (TextView) view.findViewById(R.id.tv_user_guide_link);
		tvGithub.setTag(GitHubId);
		tvProjectPage = (TextView) view.findViewById(R.id.tv_project_page_link);
		tvProjectPage.setTag(ProjectPageId);
		String devNames = "Nikola Milosevic \n\nFurquan Ahmed \n\nAleksandar Abu-Samra \n\nChetan Karande \n\nKartik Kohli \n\nAditya Dua";
		tvDevNames.setText(devNames);
		
		tvGithub.setText(Html.fromHtml("<u>GitHub</u>"));
		tvProjectPage.setText(Html.fromHtml("<u>Project page</u>"));

		OnClickListener listener = new WebLinkListener();
		tvProjectPage.setOnClickListener(listener);
		tvGithub.setOnClickListener(listener);

		OnClickListener guideListener = new AssetListener();
		tvGuide.setOnClickListener(guideListener);

		ImageView button = (ImageView) view.findViewById(R.id.paypal_donate);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				android.net.Uri linkUri = null;
				linkUri = android.net.Uri
						.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=paypal@owasp.org&no_shipping=0&no_note=1&currency_code=USD&tax=0&lc=US&bn=PP-DonationsBF&item_name=OWASP Seraphimdroid project");
				CharSequence text = "Thank you! We love you!";
				Toast.makeText(getView().getContext() ,text,Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(linkUri);
				startActivity(intent);
			}
		});

		return view;
	}

	private class WebLinkListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			Integer tag = (Integer) view.getTag();
			android.net.Uri linkUri = null;
			switch (tag) {
			case GitHubId:
				linkUri = android.net.Uri
						.parse("https://github.com/nikolamilosevic86/owasp-seraphimdroid");
				break;
			case ProjectPageId :
				linkUri = android.net.Uri
						.parse("https://www.owasp.org/index.php/OWASP_SeraphimDroid_Project");
				break;
			}
			if (linkUri != null) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(linkUri);
				startActivity(intent);
			}
		}

	}

	private class AssetListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(
					Uri.parse("file://" + getActivity().getFilesDir() + "/userguide.pdf"), "application/pdf");

			startActivity(intent);
		}
	}

}
