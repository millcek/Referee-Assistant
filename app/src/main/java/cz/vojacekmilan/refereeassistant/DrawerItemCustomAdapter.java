package cz.vojacekmilan.refereeassistant;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by milan on 16.4.15.
 */
public class DrawerItemCustomAdapter extends ArrayAdapter<DrawerItem> {
    Context mContext;
    int layoutResourceId;
    DrawerItem data[] = null;

    public DrawerItemCustomAdapter(Context mContext, int layoutResourceId, DrawerItem[] data) {

        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
        TextView textViewName = (TextView) listItem.findViewById(R.id.text1);

        DrawerItem folder = data[position];

        if (folder.getIcon() != null)
            imageViewIcon.setImageDrawable(folder.getIcon());
        textViewName.setText(folder.getName());

        return listItem;
    }
}
