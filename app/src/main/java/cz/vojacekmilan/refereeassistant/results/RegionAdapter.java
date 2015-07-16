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
public class RegionAdapter extends ArrayAdapter<RegionAdapter.RegionItem> {
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
        TextView textViewName = (TextView) listItem.findViewById(R.id.text_view);

        RegionItem folder = data.get(position);

        imageViewIcon.setImageResource(folder.getIcon());
        textViewName.setText(folder.getText());

        return listItem;
    }

    /**
     * Created by milan on 24.6.15.
     */
    public static class RegionItem {
        private int id;
        private int icon;
        private String text;

        public RegionItem(int id, int icon, String text) {
            this.id = id;
            this.icon = icon;
            this.text = text;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegionItem that = (RegionItem) o;

            if (id != that.id) return false;
            if (icon != that.icon) return false;
            return !(text != null ? !text.equals(that.text) : that.text != null);

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + icon;
            result = 31 * result + (text != null ? text.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
