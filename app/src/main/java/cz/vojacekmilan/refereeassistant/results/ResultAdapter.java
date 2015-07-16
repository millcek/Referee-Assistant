package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cz.vojacekmilan.refereeassistant.R;

/**
 * Created by milan on 16.4.15.
 */
public class ResultAdapter extends ArrayAdapter<Result> {
    Context mContext;
    int layoutResourceId;
    List<Result> data;

    public ResultAdapter(Context mContext, int layoutResourceId, List<Result> data) {

        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View listItem = inflater.inflate(layoutResourceId, parent, false);

        Result result = data.get(position);

        ((TextView) listItem.findViewById(R.id.text_view_home)).setText(result.getHome());
        ((TextView) listItem.findViewById(R.id.text_view_away)).setText(result.getAway());
        if (result.getHomeScore() > result.getAwayScore())
            ((TextView) listItem.findViewById(R.id.text_view_home)).setTypeface(null, Typeface.BOLD);
        else if (result.getHomeScore() < result.getAwayScore())
            ((TextView) listItem.findViewById(R.id.text_view_away)).setTypeface(null, Typeface.BOLD);
        ((TextView) listItem.findViewById(R.id.text_view_result)).setText(result.getResult());
        ((TextView) listItem.findViewById(R.id.text_view_result_half)).setText(result.getResultHalf());
        return listItem;
    }
}
