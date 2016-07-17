package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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


        final Intent intent = getIntent();
//        url = BASE_URL + "articles/" + i.getStringExtra("id");

        getActionBar().setTitle(intent.getStringExtra("header"));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mWebView = (WebView) findViewById(R.id.wv);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

//        main_layout = (RelativeLayout) findViewById(R.id.main_layout);

//        title = drawerTitle = getTitle();
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawerList = (ListView) findViewById(R.id.drawer_list);
//
//        itemNames = getResources().getStringArray(R.array.item_names);
//        listItems = new ArrayList<>();
//        iconList = getResources().obtainTypedArray(R.array.drawer_icons);
//
//        MainActivity.populateList();
//
//        adapter = new DrawerAdapter(this, listItems);
//        drawerList.setAdapter(adapter);
//
//        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                if (position == 6){
//                    getIntent().removeExtra("tags");
//                    MainActivity.selectFragment(6);
//                } else {
//                    MainActivity.selectFragment(position);
//                }
//            }
//        });
//
//        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
//                R.drawable.ic_drawer, android.R.string.ok, android.R.string.ok) {
//            public void onDrawerClosed(View view) {
//                getActionBar().setTitle(title);
//                invalidateOptionsMenu();
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle(drawerTitle);
//                invalidateOptionsMenu();
//            }
//        };
//        drawerLayout.setDrawerListener(drawerToggle);

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
                mWebView.saveWebArchive(getFilesDir().getAbsolutePath() + File.separator + intent.getStringExtra("id") + "page.mht");
                Log.i("hello", "onPageFinished: page saved");
                Log.i("hello", "onPageFinished: " + getCacheDir().getAbsolutePath() + File.separator + intent.getStringExtra("id") + "page.mht");

                if (mProgressBar.getVisibility() == View.VISIBLE)
                    mProgressBar.setVisibility(View.GONE);

            }

        });

        mWebView.loadUrl(intent.getStringExtra("url"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
