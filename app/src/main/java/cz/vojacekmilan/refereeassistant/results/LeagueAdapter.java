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
public class LeagueAdapter extends ArrayAdapter<LeagueAdapter.LeagueItem> {
    Context mContext;
    int layoutResourceId;
    List<LeagueItem> data;

    public LeagueAdapter(Context mContext, int layoutResourceId, List<LeagueItem> data) {

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
        TextView textViewName = (TextView) listItem.findViewById(R.id.text_view);

        LeagueItem folder = data.get(position);

        imageViewIcon.setImageResource(folder.getIcon());
        textViewName.setText(folder.getText());
        listItem.setBackgroundColor(mContext.getResources().getColor(folder.getBackground()));

        return listItem;
    }

    /**
     * Created by milan on 24.6.15.
     */
    public static class LeagueItem {
        private int id;
        private int icon;
        private String text;
        private boolean favourite;
        private int background;

        public LeagueItem(int id, int icon, String text, boolean favourite) {
            this.id = id;
            this.icon = icon;
            this.text = text;
            this.setFavourite(favourite);
        }

        public int getId() {
            return id;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isFavourite() {
            return favourite;
        }

        public void setFavourite(boolean favourite) {
            background = favourite ? R.color.favourite : R.color.transparent;
            this.favourite = favourite;
        }

        public int getBackground() {
            return this.background;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
