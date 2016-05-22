package org.owasp.seraphimdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.seraphimdroid.model.Article;

import java.util.Objects;

public class ArticleActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private RequestQueue mRequestQueue;
    private SwipeRefreshLayout swipeRefreshLayout;
    private JsonObjectRequest jobj;
    private ImageView imghead;
    private TextView tvTitle;
    private TextView tvCategory;
    private TextView tvContent;

    private static final String BASE_URL = "http://educate-seraphimdroid.rhcloud.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_view);

        imghead = (ImageView) findViewById(R.id.article_header);

        tvTitle = (TextView) findViewById(R.id.article_title);
        tvCategory = (TextView) findViewById(R.id.article_category);
        tvContent = (TextView) findViewById(R.id.article_content);

        mRequestQueue = Volley.newRequestQueue(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_article);

        swipeRefreshLayout.setOnRefreshListener(this);

        Intent i = getIntent();

        String url = BASE_URL + "articles/" +i.getStringExtra("id")+ ".json";

        jobj = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    String id = response.getString("id");
                    String title = response.getString("title");
                    String text = response.getString("text");

                    Article article = new Article();
                    article.setId(id);
                    article.setText(text);
                    article.setTitle(title);

                    if (!Objects.equals(response.getString("category"), "null")) {
                        JSONObject category = response.getJSONObject("category");
                        article.setCategory(category.getString("name"));
                    } else {
                        article.setCategory("Other");
                    }

                    tvTitle.setText(article.getTitle());
                    tvCategory.setText(article.getCategory());
                    tvContent.setText(article.getText());

                    swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    Log.d("Hell", error.getMessage());
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        mRequestQueue.add(jobj);
                                    }
                                }
        );

    }

    @Override
    public void onRefresh() {
        mRequestQueue.add(jobj);
    }


}
