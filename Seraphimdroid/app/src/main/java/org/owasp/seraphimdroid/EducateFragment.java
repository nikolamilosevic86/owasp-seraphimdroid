package org.owasp.seraphimdroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.lucene.queryparser.classic.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.lucenedroid.Lucene;
import org.owasp.lucenedroid.Search;
import org.owasp.lucenedroid.SearchResult;
import org.owasp.seraphimdroid.adapter.ArticleAdapter;
import org.owasp.seraphimdroid.helper.ConnectionHelper;
import org.owasp.seraphimdroid.helper.DatabaseHelper;
import org.owasp.seraphimdroid.model.Article;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class EducateFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RequestQueue mRequestQueue;
    private ArrayList<Article> mArrArticle;
    private ArticleAdapter va;
    private SwipeRefreshLayout swipeRefreshLayout;
    private JsonArrayRequest jar;
    private String tags;
    private DatabaseHelper db;
    private ConnectionHelper ch;
    private SharedPreferences mSharedPreferences;
    private SearchView searchView;
    private MenuItem searchItem;

    private static final String BASE_URL = "http://educate-seraphimdroid.rhcloud.com/";
    private static final String url = BASE_URL + "articles.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_educate, container, false);

        db = new DatabaseHelper(getActivity());
        ch = new ConnectionHelper(getActivity().getApplicationContext());

        Intent i = getActivity().getIntent();
        tags = i.getStringExtra("tags");
        mSharedPreferences = getActivity().getSharedPreferences("article_reads", Context.MODE_PRIVATE);

        mArrArticle = new ArrayList<>();
        va = new ArticleAdapter(mArrArticle, new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article item) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("id", item.getId());
                if (ch.isConnectingToInternet()) {
                    i.putExtra("url", BASE_URL + "articles/" + item.getId());
                    i.putExtra("header", "Article from " + item.getCategory() +  " Category");
                    ArrayList<Article> arr = getOfflineReadArticles();
                    if (!arr.isEmpty()) {
                        for (Article article: arr){
                            uploadOfflineStats(Integer.parseInt(article.getId()));
                        }
//                        markUploaded(arr);
                    }
                    startActivity(i);
                } else {
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + item.getId() + "page.mht"));
                        if (fis.read() == 0) {
                            throw new FileNotFoundException();
                        } else {
                            i.putExtra("url", "file:///" + getActivity().getFilesDir().getAbsolutePath() + File.separator + item.getId() + "page.mht");
                            i.putExtra("header", "Article from " + item.getCategory() + " Category");
                            addOfflineRead(Integer.parseInt(item.getId()));
                            startActivity(i);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "This Article is Not Available Offline.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Some Error Occurred.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

//      JsonArrayRequest was here

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView lstView = (RecyclerView) view.findViewById(R.id.recycle_articles);

//        TODO RecylerView Management Bug
//        lstView.setOnScrollListener(new RecyclerView.OnScrollListener(){
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                int topRowVerticalPosition =
//                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
//                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
//
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//        });


        lstView.setLayoutManager(linearLayoutManager);
        lstView.setAdapter(va);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                if (tags != null){
                    mArrArticle.addAll(db.getArticlesWithTag(tags));
                    va.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    getAPIData();
//                    rebuildIndex();
                }
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (!searchView.isIconified()) {
                    if(keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        if (i == KeyEvent.KEYCODE_BACK) {
                            searchView.setQuery("", false);
                            searchItem.collapseActionView();
//                            rebuildIndex();
                            mArrArticle.clear();
                            mArrArticle.addAll(db.getAllArticles());
                            va.notifyDataSetChanged();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feedback_educate:
                Log.d("Feedback", "onOptionsItemSelected: Feedback selected");
                Intent i = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_educate, menu);

        searchItem = menu.findItem(R.id.action_search);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mArrArticle.clear();
                mArrArticle.addAll(db.getAllArticles());
                va.notifyDataSetChanged();
                return true;
            }
        });

        searchView = new SearchView(getActivity().getActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(searchItem, searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                try {
                    Search searcher = new Search(getIndexRootDir().getAbsolutePath());
                    SearchResult result = searcher.search(s, 20);
                    ArrayList<Article> results = Result.fromSearchResult(result);
                    searcher.close();

                    mArrArticle.clear();
                    mArrArticle.addAll(results);
                    va.notifyDataSetChanged();

                    searchView.clearFocus();
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    void getAPIData() {
        jar = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    OutputStream outputStream = getActivity().openFileOutput("articles-index.json", Context.MODE_APPEND);
                    outputStream.write(response.toString().getBytes());
                    outputStream.close();
                    for (int i = 0; i < response.length(); i++) {

                        JSONObject pjo = (JSONObject) response.get(i);
                        String id = pjo.getString("id");
                        String title = pjo.getString("title");
                        String text = pjo.getString("text");

                        Article article = new Article();
                        article.setId(id);
                        article.setText(text);
                        article.setTitle(title);
                        article.setCachefile(getActivity().getFilesDir().getAbsolutePath() + File.separator + id + "page.mht");

                        if (!Objects.equals(pjo.getString("category"), "null")){
                            JSONObject category = pjo.getJSONObject("category");
                            article.setCategory(category.getString("name"));
                        }
                        else{
                            article.setCategory("Other");
                        }

                        if (!Objects.equals(pjo.getString("tags"), "null")){
                            JSONArray tags = pjo.getJSONArray("tags");
                            ArrayList<String> taglist = new ArrayList<>();
                            for (int j=0; j < tags.length(); j++){
                                JSONObject tag = tags.getJSONObject(j);
                                String tag_name = tag.getString("name");
                                taglist.add(tag_name);
                            }
                            article.setTags(taglist);
                        }
                        else{
                            article.setTags(new ArrayList<String>());
                        }
                        mArrArticle.add(article);
                    }

                    db.addNewArticles(mArrArticle);

                    va.notifyDataSetChanged();

                    rebuildIndex();
//                    swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Some Error Occurred.", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    Toast.makeText(getActivity(), "Please Connect to the internet", Toast.LENGTH_LONG).show();
                    mArrArticle.addAll(db.getAllArticles());
                    va.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        mRequestQueue.add(jar);

    }

    File getIndexRootDir() {
        return new File(getActivity().getCacheDir(), "index");
    }

    void rebuildIndex() {
//        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Building Index", "Please Wait..", true);
        swipeRefreshLayout.setRefreshing(true);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
//                    InputStream is = getActivity().getFilesDir().
                    InputStream is = getActivity().openFileInput("articles-index.json");
                    Lucene.importData(is, getIndexRootDir().getAbsolutePath(), false);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
//                dialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);

//                if (result) {
//                    Toast.makeText(getActivity(), "Index Built", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getActivity(), "Failed to build Index", Toast.LENGTH_SHORT).show();
//                }
            }
        }.execute();
    }

//    void rebuildIndexIfNotExists() {
//        if (!getIndexRootDir().exists()) {
//            rebuildIndex();
//        }
//    }

    static class Result {
        final SearchResult searchResult;
        final Article article;

        Result(SearchResult searchResult, Article article) {
            this.searchResult = searchResult;
            this.article = article;
        }

        static ArrayList<Article> fromSearchResult(SearchResult searchResult) {
            ArrayList<Article> results = new ArrayList<>();
            for (org.owasp.lucenedroid.Article doc : searchResult.documents) {
                results.add(new Article(doc.getId(), doc.getTitle(), doc.getText(), doc.getCategory(), doc.getTags()));
            }
            return results;
        }
    }

    @Override
    public void onRefresh() {
        mArrArticle.clear();
        va.notifyDataSetChanged();
        getAPIData();
//        rebuildIndex();
        tags=null;
    }

    public void uploadOfflineStats(final int id) {
        final String addurl = "http://educate-seraphimdroid.rhcloud.com/articles/"+Integer.toString(id)+".json";
        int reads = getOfflineReads(id);
        final Map<String, String> mHeaders = new ArrayMap<>();
        mHeaders.put("many", Integer.toString(reads));

        JsonObjectRequest addUsage = new JsonObjectRequest(Request.Method.GET, addurl, null,
                new Response.Listener<JSONObject>() {
                    int resp;
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            resp = response.getInt("id");
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "Some Error Occured.", Toast.LENGTH_SHORT).show();
                        }
                        if (resp != 0) {
//                            Log.i("Usage", "onResponse: Analyzed");
                            markUploaded(id);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: "+"Error Response");
            }
        }) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(addUsage);
    }

    private ArrayList<Article> getOfflineReadArticles() {
        ArrayList<Article> articlesList = new ArrayList<>();
        for(Article article: db.getAllArticles()){
            if(getOfflineReads(Integer.parseInt(article.getId())) != 0){
                articlesList.add(article);
            }
        }

        return articlesList;
    }

    public void addOfflineRead(int id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        int new_val = getOfflineReads(id)+1;
        editor.putInt("article"+id, new_val);
        editor.apply();
    }

    public int getOfflineReads(int id) {
        return mSharedPreferences.getInt("article"+id, 0);
    }

    private void markUploaded(int id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("article"+id, 0);
        editor.apply();
    }

}
