package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.File;

public class WebViewActivity extends Activity {


    WebView mWebView;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_webview);

        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        final Intent i = getIntent();
//        url = BASE_URL + "articles/" + i.getStringExtra("id");

        mWebView = (WebView) findViewById(R.id.wv);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

//        main_layout = (RelativeLayout) findViewById(R.id.main_layout);

        mWebView.setFocusable(true);
        mWebView.setFocusableInTouchMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setDatabasePath(
                this.getFilesDir().getPath() + this.getPackageName()
                        + "/databases/");

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return false;
                if (url != null && url.startsWith("http://")) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mProgressBar.getVisibility() == View.GONE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

//                mWebView.saveWebArchive(getCacheDir().getAbsolutePath() + File.separator + i.getStringExtra("id") + File.separator + "page.mht");
                mWebView.saveWebArchive(getFilesDir().getAbsolutePath() + File.separator + i.getStringExtra("id") + "page.mht");
                Log.i("hello", "onPageFinished: page saved");
                Log.i("hello", "onPageFinished: " + getCacheDir().getAbsolutePath() + File.separator + i.getStringExtra("id") + "page.mht");

                if (mProgressBar.getVisibility() == View.VISIBLE)
                    mProgressBar.setVisibility(View.GONE);

            }

        });

        mWebView.loadUrl(i.getStringExtra("url"));

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
