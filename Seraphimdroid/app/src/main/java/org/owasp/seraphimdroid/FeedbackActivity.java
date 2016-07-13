package org.owasp.seraphimdroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.seraphimdroid.adapter.FeedbackListAdapter;
import org.owasp.seraphimdroid.helper.ConnectionHelper;
import org.owasp.seraphimdroid.helper.DatabaseHelper;
import org.owasp.seraphimdroid.model.Feedback;

import java.util.ArrayList;

public class FeedbackActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener{

    private String TAG = FeedbackActivity.class.getSimpleName();

    private String BASE_URL = "http://educate-seraphimdroid.rhcloud.com/";
    private String addurl = BASE_URL + "questions.json";

    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedbackListAdapter adapter;
    private ArrayList<Feedback> feedbackList;

    private EditText editTextFeedback;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        ListView listView = (ListView) findViewById(R.id.feedback_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_feedback);

        feedbackList = new ArrayList<>();
        adapter = new FeedbackListAdapter(this, feedbackList);
        listView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        db = new DatabaseHelper(this);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                fetchFeedback();
            }
        });

        editTextFeedback = (EditText) findViewById(R.id.editTextFeedback);
        Button sendButton = (Button) findViewById(R.id.fbutton);
        sendButton.setOnClickListener(this);

    }

    private void sendFeedback(){
        final String feedback = editTextFeedback.getText().toString().trim();
        final String body = "{ \"title\" : \"" + feedback + "\" }";

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
                            Toast.makeText(FeedbackActivity.this, "Some Error Occured.", Toast.LENGTH_SHORT).show();
                        }
                        if(resp != null && resp.equals("success")){
                            Toast.makeText(FeedbackActivity.this, "Feedback Posted.", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    feedbackList.clear();
                                    adapter.notifyDataSetChanged();
                                    fetchFeedback();
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FeedbackActivity.this,error.toString(),Toast.LENGTH_LONG).show();
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

    @Override
    public void onRefresh() {
        feedbackList.clear();
        adapter.notifyDataSetChanged();
        fetchFeedback();
    }

    private void fetchFeedback() {
        swipeRefreshLayout.setRefreshing(true);
        String url = BASE_URL + "questions.json";
        JsonArrayRequest jar = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {

                                    JSONObject feedbackObj = response.getJSONObject(i);

                                    String title = feedbackObj.getString("title");
                                    String description = feedbackObj.getString("description");
                                    int upvotes = feedbackObj.getInt("upvotes");

                                    Feedback fb = new Feedback(title, description, upvotes);
                                    feedbackList.add(fb);

                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    Toast.makeText(FeedbackActivity.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                                }
                            }
                            db.addNewFeedback(feedbackList);
                            adapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "You are Offline", Toast.LENGTH_LONG).show();

                feedbackList.addAll(db.getAllFeedback());
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jar);
    }

    @Override
    public void onClick(View view) {
        ConnectionHelper ch = new ConnectionHelper(FeedbackActivity.this.getApplicationContext());
        if(ch.isConnectingToInternet()){
            sendFeedback();
        } else {
            Toast.makeText(FeedbackActivity.this, "You are offline", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
