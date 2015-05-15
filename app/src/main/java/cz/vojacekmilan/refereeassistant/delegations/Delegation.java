package cz.vojacekmilan.refereeassistant.delegations;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by milan on 8.4.15.
 */
public class Delegation {
    private String match;
    private String home;
    private String away;
    private Date datetime;
    private String hr;
    private String ar1;
    private String ar2;
    private String r4;
    private String delegate;
    private String td;
    private String field;
    private String note;
    private int round;
    private String league;

    public Delegation() {
    }

    public String getSqlInsert(int idLeague) {
        return String.format("INSERT INTO delegations (match, home, away, datetime, hr, ar1, ar2, r4, delegate, td, field, note, round, id_leagues) " +
                        "VALUES ('%s','%s','%s',%d,'%s','%s','%s','%s','%s','%s','%s','%s',%d,%d)",
                match, home, away, datetime.getTime() / 1000, hr, ar1, ar2, r4, delegate, td, field, note, round, idLeague);
    }

    public static Delegation getDelegation(TagNode firstRow, TagNode secondRow, int round) {
        int i = 0;
        String date = "";
        Delegation delegation = new Delegation();
        try {
            for (Object node : firstRow.evaluateXPath("//td/text()")) {
                switch (i) {
                    case 0:
                        delegation.setMatch(node.toString());
                        break;
                    case 1:
                        delegation.setHome(node.toString());
                        break;
                    case 2:
                        date = node.toString();
                        break;
                    case 3:
                        delegation.setHr(node.toString());
                        break;
                    case 4:
                        delegation.setAr1(node.toString());
                        break;
                    case 5:
                        delegation.setDelegate(node.toString());
                        break;
                    case 6:
                        delegation.setField(node.toString());
                        break;
                }
                i++;
            }
            i = 0;
            for (Object node : secondRow.evaluateXPath("//td/text()")) {
                switch (i) {
                    case 1:
                        delegation.setAway(node.toString());
                        break;
                    case 2:
                        try {
                            date += node.toString().substring(2);
                        } catch (Exception e) {
                            date = "";
                        }
                        break;
                    case 3:
                        delegation.setR4(node.toString());
                        break;
                    case 4:
                        delegation.setAr2(node.toString());
                        break;
                    case 5:
                        delegation.setTd(node.toString());
                        break;
                    case 6:
                        delegation.setNote(node.toString());
                        break;
                }
                i++;
            }
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm");
                delegation.setDatetime((dateFormat.parse(date)));
            } catch (ParseException e) {
                delegation.setDatetime(new Date(0));
                e.printStackTrace();
            }
            delegation.setRound(round);
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return delegation;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
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

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) {
        this.hr = hr;
    }

    public String getAr1() {
        return ar1;
    }

    public void setAr1(String ar1) {
        this.ar1 = ar1;
    }

    public String getAr2() {
        return ar2;
    }

    public void setAr2(String ar2) {
        this.ar2 = ar2;
    }

    public String getR4() {
        return r4;
    }

    public void setR4(String r4) {
        this.r4 = r4;
    }

    public String getDelegate() {
        return delegate;
    }

    public void setDelegate(String delegate) {
        this.delegate = delegate;
    }

    public String getTd() {
        return td;
    }

    public void setTd(String td) {
        this.td = td;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    @Override
    public String toString() {
        return "Delegation{" +
                "match='" + match + '\'' +
                ", home='" + home + '\'' +
                ", away='" + away + '\'' +
                ", datetime=" + ((datetime == null) ? "" : datetime) +
                ", hr='" + hr + '\'' +
                ", ar1='" + ar1 + '\'' +
                ", ar2='" + ar2 + '\'' +
                ", r4='" + r4 + '\'' +
                ", delegate='" + delegate + '\'' +
                ", td='" + td + '\'' +
                ", field='" + field + '\'' +
                ", note='" + note + '\'' +
                ", round=" + round +
                ", league='" + league + '\'' +
                '}';
    }
}
