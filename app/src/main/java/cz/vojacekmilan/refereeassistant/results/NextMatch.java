package cz.vojacekmilan.refereeassistant.results;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by milan on 24.3.15.
 */
public class NextMatch {
    private int idClubsHome;
    private int idClubsAway;
    private String clubsHome;
    private String clubsAway;
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
                    this.setClubsHome(s);
                    break;
                case 2:
                    this.setClubsAway(s);
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

    public String getClubsHome() {
        return clubsHome;
    }

    public void setClubsHome(String clubsHome) {
        this.clubsHome = clubsHome;
    }

    public String getClubsAway() {
        return clubsAway;
    }

    public void setClubsAway(String clubsAway) {
        this.clubsAway = clubsAway;
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
        clubsHome = selectClubName(db, idClubsHome);
        clubsAway = selectClubName(db, idClubsAway);
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
                ", clubsHome='" + clubsHome + '\'' +
                ", clubsAway='" + clubsAway + '\'' +
                ", datetime=" + datetime +
                ", field='" + field + '\'' +
                ", idLeagues=" + idLeagues +
                "}\n";
    }
}
