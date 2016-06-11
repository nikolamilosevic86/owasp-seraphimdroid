package org.owasp.seraphimdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.seraphimdroid.adapter.ArticleAdapter;
import org.owasp.seraphimdroid.helper.ConnectionHelper;
import org.owasp.seraphimdroid.helper.DatabaseHelper;
import org.owasp.seraphimdroid.model.Article;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class EducateFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String TAG = this.getClass().getSimpleName();
    private RecyclerView lstView;
    private RequestQueue mRequestQueue;
    private ArrayList<Article> mArrArticle;
    private ArticleAdapter va;
    private SwipeRefreshLayout swipeRefreshLayout;
    private JsonArrayRequest jar;

    private static final String BASE_URL = "http://educate-seraphimdroid.rhcloud.com/";

    private static final String url = BASE_URL + "articles.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_educate, container, false);

        mArrArticle = new ArrayList<>();

        final DatabaseHelper db = new DatabaseHelper(getActivity());

        final ConnectionHelper ch = new ConnectionHelper(getActivity().getApplicationContext());

        va = new ArticleAdapter(mArrArticle, new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article item) {
//                Intent i = new Intent(getActivity(), ArticleActivity.class);
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("id", item.getId());
                if (ch.isConnectingToInternet()) {
                    i.putExtra("url", BASE_URL + "articles/" + item.getId());
                    startActivity(i);
                } else {
                    FileInputStream fis = null;
                    try {
//                        fis = new FileInputStream(new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + item.getId() + File.separator + "page.mht"));
                        fis = new FileInputStream(new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + item.getId() + "page.mht"));
                        if (fis.read() == 0) {
                            throw new FileNotFoundException();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "NO Internet", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Some Error Occurred.", Toast.LENGTH_SHORT).show();
                    }
//                    i.putExtra("url", "file:///" + getActivity().getFilesDir().getAbsolutePath() + File.separator + item.getId() + File.separator + "page.mht");
                    i.putExtra("url", "file:///" + getActivity().getFilesDir().getAbsolutePath() + File.separator + item.getId() + "page.mht");
                    startActivity(i);
                }
            }
        });

        lstView = (RecyclerView) view.findViewById(R.id.recycle_articles);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        lstView.setLayoutManager(linearLayoutManager);

        lstView.setAdapter(va);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        jar = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject pjo = (JSONObject) response.get(i);
                        String id = pjo.getString("id");
                        String title = pjo.getString("title");
                        String text = pjo.getString("text");

                        Article article = new Article();
                        article.setId(id);
                        article.setText(text);
                        article.setTitle(title);
//                        article.setCachefile(getActivity().getFilesDir().getAbsolutePath() + File.separator + id + File.separator + "page.mht");
                        article.setCachefile(getActivity().getFilesDir().getAbsolutePath() + File.separator + id + "page.mht");

                        if (!Objects.equals(pjo.getString("category"), "null")){
                            JSONObject category = pjo.getJSONObject("category");
                            article.setCategory(category.getString("name"));
                        }
                        else{
                            article.setCategory("Other");
                        }

                        mArrArticle.add(article);

                    }

                    db.addNewArticles(mArrArticle);

                    va.notifyDataSetChanged();

//                    FileWriter file = new FileWriter(getActivity().getFilesDir().getAbsolutePath() + File.separator + "saved_list.json");
//                    file.write(response.toString());
//                    Log.i(TAG, "onResponse: Written to file");
//                    FileInputStream fis = new FileInputStream (new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + "saved_list.json"));
//                    Log.i(TAG, "onResponse: " + fis.read());

                    swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Some Error Occurred.", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(getActivity(),
//                            "Error: " + e.getMessage(),
//                            Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
//                    Log.i(TAG, error.getMessage());
//                    Toast.makeText(getActivity(),
//                            "Error: " + error.getMessage(),
//                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), "Please Connect to the internet", Toast.LENGTH_LONG).show();

                    mArrArticle.addAll(db.getAllArticles());
//                    db.getAllArticles();

//                    Log.i(TAG, "onErrorResponse: " + mArrArticle.toString());

                    va.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);

                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        mRequestQueue.add(jar);
                                    }
                                }
        );

        return view;

    }

    @Override
    public void onRefresh() {
        mArrArticle.clear();
        mRequestQueue.add(jar);
    }


}


//    TODO: Update this fragment with questions and content from the knowledge API

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }