package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.vojacekmilan.refereeassistant.R;

/**
 * Created by milan on 16.4.15.
 */
public class RegionAdapter extends ArrayAdapter<RegionItem> {
    Context mContext;
    int layoutResourceId;
    List<RegionItem> data;

    public RegionAdapter(Context mContext, int layoutResourceId, List<RegionItem> data) {

        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.icon);
        TextView textViewName = (TextView) listItem.findViewById(R.id.textView);

        RegionItem folder = data.get(position);

        imageViewIcon.setImageResource(folder.getIcon());
        textViewName.setText(folder.getText());

        return listItem;
    }
}
