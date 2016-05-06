package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.owasp.seraphimdroid.R;

import java.util.ArrayList;

import model.Article;

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


    public ArticleAdapter(ArrayList<Article> Articles) {
        mArrArticle = Articles;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvText;
        TextView tvCategory;

        public ViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.title_area);
            tvText = (TextView) itemView.findViewById(R.id.text_area);
            tvCategory = (TextView) itemView.findViewById(R.id.category_area);
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

        Article Question = mArrArticle.get(position);

        TextView titleView = viewHolder.tvTitle;
        TextView textView = viewHolder.tvText;
        TextView categoryView = viewHolder.tvCategory;

        titleView.setText(Question.getTitle());
        textView.setText(Question.getText());
        categoryView.setText(Question.getCategory());

    }

    @Override
    public int getItemCount() {
        return mArrArticle.size();
    }

}
