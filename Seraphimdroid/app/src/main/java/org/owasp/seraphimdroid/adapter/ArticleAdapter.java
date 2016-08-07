package org.owasp.seraphimdroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.model.Article;

import java.util.ArrayList;

/**
 * Created by addiittya on 04/05/16.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

//    private LayoutInflater lf;

//    TODO Cleanup Code BaseAdapter <ListView>  -->  RecyclerViewAdapter

    ArrayList<Article> mArrArticle = new ArrayList<>();
//
//    public ArticleAdapter(ArrayList arr, Context c) {
//        this.mArrArticle = arr;
//        lf = LayoutInflater.from(c);
//    }
//
//    class  ViewHolder {
//        TextView tvTitle;
//        TextView tvText;
//        TextView tvCategory;
//    }
//
//    @Override
//    public int getCount() {
//        return mArrArticle.size();
//    }
//
//    @Override
//    public Object getItem(int i) {
//        return mArrArticle.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return 0;
//    }
//
//    @Override
//    public View getView(int i, View view, ViewGroup viewGroup) {
//
//        ViewHolder vh ;
//
//        if(view == null){
//            vh = new ViewHolder();
//            view = lf.inflate(R.layout.article_item,null);
//            vh.tvTitle = (TextView) view.findViewById(R.id.title_area);
//            vh.tvText = (TextView) view.findViewById(R.id.text_area);
//            vh.tvCategory = (TextView) view.findViewById(R.id.category_area);
//            view.setTag(vh);
//        }
//        else{
//            vh = (ViewHolder) view.getTag();
//        }
//
//        Article article = mArrArticle.get(i);
//        vh.tvTitle.setText(article.getTitle());
//        vh.tvText.setText(article.getText());
//        vh.tvCategory.setText(article.getId());
//        return view;
//
//    }
//
//}

    public interface OnItemClickListener {
        void onItemClick(Article item);
    }

    private final OnItemClickListener listener;

    public ArticleAdapter(ArrayList<Article> Articles, OnItemClickListener listener) {
        this.mArrArticle = Articles;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvCategory;
        TextView tvText;
        TextView tvTags;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            tvCategory = (TextView) itemView.findViewById(R.id.txtCategory);
            tvText = (TextView) itemView.findViewById(R.id.txtText);
            tvTags = (TextView) itemView.findViewById(R.id.txtTags);
        }

        public void bind(final Article item, final OnItemClickListener listener) {
            tvTitle.setText(item.getTitle());
            tvCategory.setText(item.getCategory());
            tvTags.setText("Tagged with: " + item.getTags().toString().replace("[", "").replace("]", ""));
            tvText.setText(item.getText());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View ArticleView = inflater.inflate(R.layout.article_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(ArticleView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Article article = mArrArticle.get(position);
        viewHolder.bind(article, listener);
    }

    @Override
    public int getItemCount() {
        return mArrArticle.size();
    }

}
