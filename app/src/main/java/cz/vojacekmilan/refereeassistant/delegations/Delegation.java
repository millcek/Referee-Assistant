package cz.vojacekmilan.refereeassistant.delegations;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.vojacekmilan.refereeassistant.Utils;

public class Delegation {
    private String match;
    private String home;
    private String away;
    private Date datetime;
    private String hr;
    private String ar1;
    private String ar2;
    private String r4;
    private String ds;
    private String td;
    private String field;
    private String note;
    private int round;
    private String league;
    private int idLeague;
//    id_regions, name, hr, ar1, ar2, r4, td, ds
    public Delegation() {
    }


    public String getSqlInsert() {
        return String.format("INSERT INTO delegations (match, home, away, datetime, hr, ar1, ar2, r4, ds, td, field, note, round, id_leagues) " +
                        "VALUES ('%s','%s','%s',%d,'%s','%s','%s','%s','%s','%s','%s','%s',%d,%d)",
                match, home, away, datetime.getTime() / 1000, hr, ar1, ar2, r4, ds, td, field, note, round, idLeague);
    }

    public static List<Delegation> getAllDelegations(URL url, HashMap<String, Integer> leaguesHashMap) {
        TagNode tagNode = Utils.getCleanTagNodes(url, "iso-8859-2");
        return getAllDelegations(Utils.getCleanTagNodes(Utils.getSerializedHtml(tagNode, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div")), leaguesHashMap);
    }

    public static List<Delegation> getAllDelegations(TagNode centerDiv, HashMap<String, Integer> leaguesHashMap) {
        List<Delegation> delegations = new ArrayList<>();
        String[] leagueDelegations = Utils.getSerializedHtml(centerDiv).split("<h4>");
        TagNode[] roundTagNodes = new TagNode[leagueDelegations.length];
        String[] leagueNames = new String[leagueDelegations.length];
        for (int i = 0; i < roundTagNodes.length; i++) {
            if (leagueDelegations[i].contains("</h4>")) {
                int pos = leagueDelegations[i].indexOf("</h4>");
                leagueNames[i] = leagueDelegations[i].substring(0, pos);
                leagueDelegations[i] = leagueDelegations[i].substring(pos + 5);
            }
            roundTagNodes[i] = Utils.getCleanTagNodes(leagueDelegations[i]);
        }
        String updated = Utils.getStringXpath(roundTagNodes[0], "//text()");
        for (int i = 1; i < roundTagNodes.length; i++) {
            try {
                for (Object tableObject : roundTagNodes[i].evaluateXPath("//table")) {
                    try {
                        int leagueId = leaguesHashMap.get(leagueNames[i].replace("&quot;", "\""));
                        delegations.addAll(getDelegations((TagNode) tableObject, leagueId));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } catch (XPatherException e) {
                e.printStackTrace();
            }
        }
        return delegations;
    }

    private static List<Delegation> getDelegations(TagNode table, int idLeague) {
        List<Delegation> delegations = new ArrayList<>();
        try {
            String roundString = Utils.getStringXpath(table, "//tr/td/p[1]/text()");
            int round = 0;
            try {
                roundString = roundString.substring(0, roundString.indexOf(".")).trim();
                round = Integer.valueOf(roundString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String[] xpaths = new String[]{"//tr[@bgcolor='#f8f8f8']", "//tr[@bgcolor='#ffffff']"};
            for (String xpath : xpaths) {
                Object[] rows = table.evaluateXPath(xpath);
                for (int i = 0; i + 1 < rows.length; i = i + 2)
                    delegations.add(getDelegation((TagNode) rows[i], (TagNode) rows[i + 1], round, idLeague));
            }
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return delegations;
    }

    private static Delegation getDelegation(TagNode firstRow, TagNode secondRow, int round, int idLeague) {
        int i = 0;
        String date = "";
        Delegation delegation = new Delegation();
        try {
            for (Object node : firstRow.evaluateXPath("//td/text()")) {
                String nodeString = node.toString().replaceAll("\n", "").replaceAll("//S+", " ").trim();
                switch (i) {
                    case 0:
                        delegation.setMatch(nodeString);
                        break;
                    case 1:
                        delegation.setHome(nodeString);
                        break;
                    case 2:
                        date = nodeString;
                        break;
                    case 3:
                        delegation.setHr(nodeString);
                        break;
                    case 4:
                        delegation.setAr1(nodeString);
                        break;
                    case 5:
                        delegation.setds(nodeString);
                        break;
                    case 6:
                        delegation.setField(nodeString);
                        break;
                }
                i++;
            }
            i = 0;
            for (Object node : secondRow.evaluateXPath("//td/text()")) {
                String nodeString = node.toString().replaceAll("\n", "").replaceAll("//S+", " ").trim();
                switch (i) {
                    case 1:
                        delegation.setAway(nodeString);
                        break;
                    case 2:
                        try {
                            date += nodeString.substring(2);
                        } catch (Exception e) {
                            date = "";
                        }
                        break;
                    case 3:
                        delegation.setR4(nodeString);
                        break;
                    case 4:
                        delegation.setAr2(nodeString);
                        break;
                    case 5:
                        delegation.setTd(nodeString);
                        break;
                    case 6:
                        delegation.setNote(nodeString);
                        break;
                }
                i++;
            }
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm");
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                calendar.setTime(dateFormat.parse(date));
                calendar.set(Calendar.YEAR, year);
                delegation.setDatetime(calendar.getTime());
            } catch (ParseException e) {
                delegation.setDatetime(new Date(0));
                e.printStackTrace();
            }
            delegation.setRound(round);
            delegation.setIdLeague(idLeague);
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return delegation;
    }

    public int getIdLeague() {
        return idLeague;
    }

    public void setIdLeague(int idLeague) {
        this.idLeague = idLeague;
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

    public String getds() {
        return ds;
    }

    public void setds(String ds) {
        this.ds = ds;
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

    public String getSqlInsertOrReplace() {
        return String.format("INSERT OR REPLACE INTO delegations (_id, match, id_clubs_home, id_clubs_away, datetime, hr, ar1, ar2, r4, ds, td, field, note, round, id_leagues) " +
                        "values ((%s), '%s', (%s), (%s), %d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d, %d)",
                selectId(), match, getHomeIdSelect(), getAwayIdSelect(), (datetime == null) ? 0 : datetime.getTime() / 1000, hr, ar1, ar2, r4, ds, td, field, note, round, idLeague);
    }

    private String selectId() {
        return String.format("SELECT _id FROM delegations WHERE id_leagues = %d AND id_clubs_home = (%s) AND id_clubs_away = (%s) AND round = %d", idLeague, getHomeIdSelect(), getAwayIdSelect(), round);
    }

    public String getAwayIdSelect() {
        return String.format("SELECT _id FROM clubs WHERE id_leagues = %d AND name = '%s'", idLeague, away);
    }

    public String getHomeIdSelect() {
        return String.format("SELECT _id FROM clubs WHERE id_leagues = %d AND name = '%s'", idLeague, home);
    }

    @Override
    public String toString() {
        return "Delegation{" +
                "match='" + match + '\'' +
                ", home='" + home + '\'' +
                ", away='" + away + '\'' +
                ", datetime=" + datetime +
                ", hr='" + hr + '\'' +
                ", ar1='" + ar1 + '\'' +
                ", ar2='" + ar2 + '\'' +
                ", r4='" + r4 + '\'' +
                ", ds='" + ds + '\'' +
                ", td='" + td + '\'' +
                ", field='" + field + '\'' +
                ", note='" + note + '\'' +
                ", round=" + round +
                ", league='" + league + '\'' +
                ", idLeague=" + idLeague +
                '}';
    }

}
