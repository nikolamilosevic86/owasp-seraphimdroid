package org.owasp.seraphimdroid.adapter;

/**
 * Created by addiittya on 30/06/16.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.owasp.seraphimdroid.R;
import org.owasp.seraphimdroid.model.Feedback;

import java.util.List;

public class FeedbackListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Feedback> fbList;

    public FeedbackListAdapter(Activity activity, List<Feedback> movieList) {
        this.activity = activity;
        this.fbList = movieList;
    }

    @Override
    public int getCount() {
        return fbList.size();
    }

    @Override
    public Object getItem(int location) {
        return fbList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feedback_item, null);

        TextView question = (TextView) convertView.findViewById(R.id.question);
        TextView description = (TextView) convertView.findViewById(R.id.description);

        question.setText(fbList.get(position).getTitle());
        if(fbList.get(position).getDescription().equals("null")){
            description.setText("");
        }else{
            description.setText(fbList.get(position).getDescription());
        }

        return convertView;
    }

}