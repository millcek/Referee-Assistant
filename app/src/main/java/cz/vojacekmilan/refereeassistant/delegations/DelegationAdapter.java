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
 * Created by milan on 7.7.15.
 */
public class DelegationAdapter extends ArrayAdapter<DelegationAdapter.DelegationItem> {
    Context mContext;
    int layoutResourceId;
    List<DelegationItem> data;

    public DelegationAdapter(Context mContext, int layoutResourceId, List<DelegationItem> data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View listItem = inflater.inflate(layoutResourceId, parent, false);
        DelegationItem folder = data.get(position);

        ((TextView) listItem.findViewById(R.id.text_view_league)).setText(folder.getLeague());
        ((TextView) listItem.findViewById(R.id.text_view_home)).setText(folder.getHome());
        ((TextView) listItem.findViewById(R.id.text_view_away)).setText(folder.getAway());
        ((TextView) listItem.findViewById(R.id.text_view_date)).setText(folder.getDate());
        ((TextView) listItem.findViewById(R.id.text_view_field)).setText(folder.getField());
        ((TextView) listItem.findViewById(R.id.text_view_hr)).setText(folder.getHr());
        ((TextView) listItem.findViewById(R.id.text_view_ar1)).setText(folder.getAr1());
        ((TextView) listItem.findViewById(R.id.text_view_ar2)).setText(folder.getAr2());
        ((TextView) listItem.findViewById(R.id.text_view_r4)).setText(folder.getR4());
        try {
            if (folder.getTd().trim().equals(folder.getR4().trim()) && folder.getTd().trim().isEmpty()) {
                TableRow tableRow = (TableRow) listItem.findViewById(R.id.table_row_delegates);
                tableRow.removeAllViews();
                tableRow.setPadding(0, 0, 0, 0);
            } else {
                ((TextView) listItem.findViewById(R.id.text_view_ds)).setText(folder.getDs());
                ((TextView) listItem.findViewById(R.id.text_view_td)).setText(folder.getTd());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            TableRow tableRow = (TableRow) listItem.findViewById(R.id.table_row_delegates);
            tableRow.removeAllViews();
            tableRow.setPadding(0, 0, 0, 0);
        }
        return listItem;
    }

    /**
     * Created by milan on 7.7.15.
     */
    public static class DelegationItem {
        private String league;
        private String home;
        private String away;
        private String date;
        private String hr;
        private String ar1;
        private String ar2;
        private String r4;
        private String ds;
        private String td;
        private String field;
        private int idLeague;

        public DelegationItem(String league, String home, String away, String date, String hr, String ar1, String ar2, String r4, String ds, String td, String field, int idLeague) {
            this.league = league;
            this.home = home;
            this.away = away;
            this.date = date;
            this.hr = hr;
            this.ar1 = ar1;
            this.ar2 = ar2;
            this.r4 = r4;
            this.ds = ds;
            this.td = td;
            this.field = field;
            this.idLeague = idLeague;
        }

        public int getIdLeague() {
            return idLeague;
        }

        public String getLeague() {
            return league;
        }

        public String getHr() {
            return hr;
        }

        public String getAr1() {
            return ar1;
        }

        public String getAr2() {
            return ar2;
        }

        public String getR4() {
            return r4;
        }

        public String getDs() {
            return ds;
        }

        public String getTd() {
            return td;
        }

        public String getField() {
            return field;
        }

        public String getHome() {
            return home;
        }

        public String getAway() {
            return away;
        }

        public String getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "DelegationItem{" +
                    "league='" + league + '\'' +
                    ", home='" + home + '\'' +
                    ", away='" + away + '\'' +
                    ", date='" + date + '\'' +
                    ", hr='" + hr + '\'' +
                    ", ar1='" + ar1 + '\'' +
                    ", ar2='" + ar2 + '\'' +
                    ", r4='" + r4 + '\'' +
                    ", ds='" + ds + '\'' +
                    ", td='" + td + '\'' +
                    ", field='" + field + '\'' +
                    '}';
        }
    }
}
