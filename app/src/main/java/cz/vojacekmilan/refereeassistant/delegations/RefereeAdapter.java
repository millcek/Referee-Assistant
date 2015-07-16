package cz.vojacekmilan.refereeassistant.delegations;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import cz.vojacekmilan.refereeassistant.R;

/**
 * Created by milan on 13.7.15.
 */
public class RefereeAdapter extends ArrayAdapter<RefereeAdapter.RefereeItem> {
    Context mContext;
    int layoutResourceId;
    List<RefereeItem> data;

    public RefereeAdapter(Context mContext, int layoutResourceId, List<RefereeItem> data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View listItem = inflater.inflate(layoutResourceId, parent, false);
        RefereeItem folder = data.get(position);

        ((TextView) listItem.findViewById(R.id.text_view_name)).setText(folder.getName());
        ((TextView) listItem.findViewById(R.id.text_view_region)).setText(folder.getRegion());

        return listItem;
    }

    public static class RefereeItem {
        private int id;
        private String name;
        private String region;

        public RefereeItem(int id, String name, String region) {
            this.id = id;
            this.name = name;
            this.region = region;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRegion() {
            return region;
        }
    }
}
