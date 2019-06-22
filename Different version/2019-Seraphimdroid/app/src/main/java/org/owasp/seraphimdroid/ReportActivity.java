package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.seraphimdroid.helper.ConnectionHelper;

public class ReportActivity extends Activity implements View.OnClickListener{

    private String TAG = ReportActivity.class.getSimpleName();

    private String BASE_URL = "http://educate-seraphimdroid.rhcloud.com/";
    private String addurl = BASE_URL + "questions.json";

//    private SwipeRefreshLayout swipeRefreshLayout;
//    private FeedbackListAdapter adapter;
//    private ArrayList<Feedback> feedbackList;

    private EditText title_editText;
    private EditText desc_editText;
    String append_text;
    String app_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        PackageManager pkgmanager = getPackageManager();

//        ListView listView = (ListView) findViewById(R.id.feedback_list);
//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_feedback);
//
//        feedbackList = new ArrayList<>();
//        adapter = new FeedbackListAdapter(this, feedbackList);
//        listView.setAdapter(adapter);
//
//        swipeRefreshLayout.setOnRefreshListener(this);
//        db = new DatabaseHelper(this);
//
//        swipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//                fetchFeedback();
//            }
//        });

        getActionBar().setTitle("Report an App");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        title_editText = (EditText) findViewById(R.id.report_title);
        desc_editText = (EditText) findViewById(R.id.report_desc);

        String package_name = getIntent().getStringExtra("package");
        if(getIntent().getBooleanExtra("malicious", false)){
            append_text = " This app was reported Malicious.";
        }else{
            append_text = " This app was not flagged Malicious.";
        }

        if(getIntent().getBooleanExtra("report", false)){
            title_editText.setText(package_name);
        }

        try {
            app_name = pkgmanager
                    .getApplicationInfo(package_name,
                            PackageManager.GET_META_DATA)
                    .loadLabel(pkgmanager).toString();
        } catch (PackageManager.NameNotFoundException ignored) {}

        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);

    }

    private void sendReport(){
        final String feedback = title_editText.getText().toString().trim();
        final String desc = "App Name : " + app_name +" . "+ desc_editText.getText().toString().trim().concat(append_text);
        final String body = "{" +
                "\"title\" : \"" + feedback + "\", " +
                "\"description\" : \"" + desc + "\", " +
                "\"report\" : 1" +
                "}";

        JSONObject header = new JSONObject();
        try{ header.put("Content-Type", "application/json"); } catch (JSONException ignored){}

        JsonObjectRequest addfb = new JsonObjectRequest(Request.Method.POST, addurl, header,
                new Response.Listener<JSONObject>() {
                    String resp;
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            resp = response.getString("status");
                        } catch (JSONException e){
                            Toast.makeText(ReportActivity.this, "Some Error Occured.", Toast.LENGTH_SHORT).show();
                        }
                        if(resp != null && resp.equals("success")){
                            Toast.makeText(ReportActivity.this, "Feedback Posted.", Toast.LENGTH_SHORT).show();
                            finish();
//                            swipeRefreshLayout.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    feedbackList.clear();
//                                    adapter.notifyDataSetChanged();
//                                    fetchFeedback();
//                                }
//                            });
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ReportActivity.this,error.toString(),Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public byte[] getBody() {
                return body.getBytes();
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(addfb);
    }

//    @Override
//    public void onRefresh() {
//        feedbackList.clear();
//        adapter.notifyDataSetChanged();
//        fetchFeedback();
//    }

//    private void fetchFeedback() {
//        swipeRefreshLayout.setRefreshing(true);
//        String url = BASE_URL + "questions.json";
//        JsonArrayRequest jar = new JsonArrayRequest(url,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        if (response.length() > 0) {
//                            for (int i = 0; i < response.length(); i++) {
//                                try {
//
//                                    JSONObject feedbackObj = response.getJSONObject(i);
//
//                                    String title = feedbackObj.getString("title");
//                                    String description = feedbackObj.getString("description");
//                                    int upvotes = feedbackObj.getInt("upvotes");
//
//                                    Feedback fb = new Feedback(title, description, upvotes);
//                                    feedbackList.add(fb);
//
//                                } catch (JSONException e) {
//                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
//                                    Toast.makeText(ReportActivity.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            db.addNewFeedback(feedbackList);
//                            adapter.notifyDataSetChanged();
//                        }
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(), "You are Offline", Toast.LENGTH_LONG).show();
//
//                feedbackList.addAll(db.getAllFeedback());
//                adapter.notifyDataSetChanged();
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(jar);
//    }

    @Override
    public void onClick(View view) {
        ConnectionHelper ch = new ConnectionHelper(ReportActivity.this.getApplicationContext());
        if(ch.isConnectingToInternet()){
            sendReport();
        } else {
            Toast.makeText(ReportActivity.this, "You are offline", Toast.LENGTH_SHORT).show();
        }
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
