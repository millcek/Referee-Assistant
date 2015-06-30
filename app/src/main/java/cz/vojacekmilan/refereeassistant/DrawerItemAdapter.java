package cz.vojacekmilan.refereeassistant;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by milan on 16.4.15.
 */
public class DrawerItemAdapter extends ArrayAdapter<DrawerItem> {
    Context mContext;
    int layoutResourceId;
    DrawerItem data[] = null;

    public DrawerItemAdapter(Context mContext, int layoutResourceId, DrawerItem[] data) {

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

        DrawerItem folder = data[position];
        imageViewIcon.setImageResource(folder.getIcon());
        textViewName.setText(folder.getName());
        textViewName.setTextColor(mContext.getResources().getColor(folder.isActive() ? R.color.primary_dark : R.color.black));
        imageViewIcon.setColorFilter(mContext.getResources().getColor(folder.isActive() ? R.color.primary_dark : R.color.black));
        textViewName.setTypeface(null, folder.isActive() ? Typeface.BOLD : Typeface.NORMAL);

        return listItem;
    }
}
