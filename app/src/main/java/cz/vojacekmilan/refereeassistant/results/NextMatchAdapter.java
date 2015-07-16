package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.vojacekmilan.refereeassistant.R;

/**
 * Created by milan on 14.7.15.
 */
public class NextMatchAdapter extends ArrayAdapter<NextMatchAdapter.NextMatch> {
    Context mContext;
    int layoutResourceId;
    List<NextMatch> data;

    public NextMatchAdapter(Context mContext, int layoutResourceId, List<NextMatch> data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View listItem = inflater.inflate(layoutResourceId, parent, false);

        NextMatch result = data.get(position);

        ((TextView) listItem.findViewById(R.id.text_view_home)).setText(result.getHome());
        ((TextView) listItem.findViewById(R.id.text_view_away)).setText(result.getAway());
        ((TextView) listItem.findViewById(R.id.text_view_result)).setText(new SimpleDateFormat("dd.MM. HH:mm").format(result.getDatetime()));
        ((TextView) listItem.findViewById(R.id.text_view_result_half)).setText(result.getField());
        return listItem;
    }

    public static class NextMatch {
        private int idClubsHome;
        private int idClubsAway;
        private String home;
        private String away;
        private Date datetime;
        private String field;
        private int idLeagues;

        public NextMatch() {
        }

        public NextMatch(Object[] values) {
            int i = 0;
            for (Object tdObject : values) {
                String s = tdObject.toString().trim();
                switch (i) {
                    case 1:
                        this.setHome(s);
                        break;
                    case 2:
                        this.setAway(s);
                        break;
                    case 3:
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm");
                        try {
                            this.setDatetime(dateFormat.parse(s));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 5:
                        this.setField(s);
                }
                i++;
            }
        }

        public String getHome() {
            return home;
        }

        public void setHome(String home) {
            this.home = home;
        }

        public String getAway() {
            return away;
        }

        public void setAway(String away) {
            this.away = away;
        }

        public int getIdClubsHome() {
            return idClubsHome;
        }

        public void setIdClubsHome(int idClubsHome) {
            this.idClubsHome = idClubsHome;
        }

        public int getIdClubsAway() {
            return idClubsAway;
        }

        public void setIdClubsAway(int idClubsAway) {
            this.idClubsAway = idClubsAway;
        }

        public Date getDatetime() {
            return datetime;
        }

        public void setDatetime(Date datetime) {
            this.datetime = datetime;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public int getIdLeagues() {
            return idLeagues;
        }

        public void setIdLeagues(int idLeagues) {
            this.idLeagues = idLeagues;
        }

        public void findClubs(SQLiteDatabase db) {
            home = selectClubName(db, idClubsHome);
            away = selectClubName(db, idClubsAway);
        }

        private String selectClubName(SQLiteDatabase db, int id) {
            Cursor cursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + id + " AND id_leagues = " + idLeagues, null);
            String s = null;
            if (cursor.moveToNext())
                s = cursor.getString(0);
            cursor.close();
            return s;
        }

        public String getSqlInsert() {
            return String.format("INSERT INTO next_matches (id_clubs_home, id_clubs_away, datetime, field, id_leagues) " +
                    "VALUES (%d, %d, %d, '%s', %d)", idClubsHome, idClubsAway, datetime.getTime() / 1000, field, idLeagues);
        }

        @Override
        public String toString() {
            return "NextMatch{" +
                    "idClubsHome=" + idClubsHome +
                    ", idClubsAway=" + idClubsAway +
                    ", home='" + home + '\'' +
                    ", away='" + away + '\'' +
                    ", datetime=" + datetime +
                    ", field='" + field + '\'' +
                    ", idLeagues=" + idLeagues +
                    "}\n";
        }
    }
}
