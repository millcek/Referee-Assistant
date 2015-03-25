package cz.vojacekmilan.refereeassistant.results;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.Utils;

import static cz.vojacekmilan.refereeassistant.Utils.getCleanTagNodes;
import static cz.vojacekmilan.refereeassistant.Utils.getSerializedHtml;

/**
 * Created by milan on 27.2.15.
 */
public class League {
    private static final String DEFAULT_FORMAT = "%empty %name %empty %wins %draws %losses %score %empty %empty %pointstruth";

    private int id;
    private String name;
    private String urlString;
    private List<Result> results;
    private List<Club> clubs;
    private List<NextMatch> nextMatches;

    public League(int id, String name, String urlString, List<Result> results, List<Club> clubs) {
        this.id = id;
        this.name = name;
        this.urlString = urlString;
        this.results = results;
        this.clubs = clubs;
    }

    public League(String name, String urlString, List<Result> results, List<Club> clubs) {
        this.name = name;
        this.urlString = urlString;
        this.results = results;
        this.clubs = clubs;
    }

    public League(String urlString) {
        this.urlString = urlString;
    }

    public League(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public League() {

    }

    public List<NextMatch> getNextMatches() {
        return nextMatches;
    }

    public void setNextMatches(List<NextMatch> nextMatches) {
        this.nextMatches = nextMatches;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public List<Club> getClubs() {
        return clubs;
    }

    public void setClubs(List<Club> clubs) {
        this.clubs = clubs;
    }

    @Override
    public String toString() {
        return "League{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", urlString='" + urlString + '\'' +
                ", \n\tresults=" + results +
                ", \n\tclubs=" + clubs +
                "\n}";
    }

    public void pairClubs() {
        for (Result result : results) {
            boolean homeSet = false;
            boolean awaySet = false;
            for (Club club : clubs) {
                try {
                    if (result.getHome().getName().equals(club.getName())) {
                        result.setHome(club);
                        homeSet = true;
                    }
                    if (result.getAway().getName().equals(club.getName())) {
                        result.setAway(club);
                        awaySet = true;
                    }
                    if (homeSet && awaySet) break;
                } catch (Exception e) {
                    Log.i("League", e.getMessage());
                }
            }
        }
    }

    public static League getLeague(String url) throws MalformedURLException, XPatherException {
        return getLeague(new League(url));
    }

    public static League getLeague(League league) throws MalformedURLException, XPatherException {
        String clubsUrl = league.getUrlString();
        String resultsUrl = league.getUrlString();
        if (clubsUrl.contains("&"))
            clubsUrl = clubsUrl.substring(0, clubsUrl.indexOf("&")) + "&show=Aktual";
        else
            clubsUrl += "&show=Aktual";
        if (resultsUrl.contains("&"))
            resultsUrl = resultsUrl.substring(0, resultsUrl.indexOf("&")) + "&show=Vysledky";
        else
            resultsUrl += "&show=Vysledky";
        TagNode rootClubs = getCleanTagNodes(new URL(clubsUrl), Results.CHARSET);
        TagNode rootResults = getCleanTagNodes(new URL(resultsUrl), Results.CHARSET);
        List<Club> clubs = getClubs(rootClubs);
        List<NextMatch> nextMatches = getNextMatches(rootClubs);
        List<Result> results = getResults(rootResults);
        league.setClubs(clubs);
        league.setResults(results);
        league.setNextMatches(nextMatches);
        league.pairClubs();
        return league;
    }

    private static List<NextMatch> getNextMatches(TagNode root) {
        List<NextMatch> nextMatches = new ArrayList<>();
        root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div//table[3]"));
        try {
            for (Object o : root.evaluateXPath("//tr[@bgcolor='bbbbbb']"))
                root.removeChild(o);
            for (Object o : root.evaluateXPath("//tr[@bgcolor='#aaaaaa']"))
                root.removeChild(o);
            for (Object o : root.evaluateXPath("//tr")) {
                TagNode trTagNode = (TagNode) o;
                int i = 0;
                for (Object ignored : trTagNode.evaluateXPath("//td"))
                    i++;
                if (i == 6) {
                    i = 0;
                    NextMatch nextMatch = new NextMatch();
                    try {
                        for (Object tdObject : trTagNode.evaluateXPath("//td/text()")) {
                            String s = tdObject.toString().trim();
                            switch (i) {
                                case 1:
                                    nextMatch.setClubsHome(s);
                                    break;
                                case 2:
                                    nextMatch.setClubsAway(s);
                                    break;
                                case 3:
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm");
                                    nextMatch.setDatetime(dateFormat.parse(s));
                                    break;
                                case 5:
                                    nextMatch.setField(s);
                            }
                            i++;
                        }
                        nextMatches.add(nextMatch);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextMatches;
    }

    private static List<Result> getResults(TagNode root) {
        List<Result> results = new ArrayList<>();
        root = getCleanTagNodes(
                getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div", "utf-8"));
        try {
            int round = 1;
            Result result = new Result();
            for (Object tableObject : root.evaluateXPath("//table")) {
                TagNode tableTagNode = (TagNode) tableObject;
                for (Object o : tableTagNode.evaluateXPath("//tr[@bgcolor='#aaaaaa']"))
                    tableTagNode.removeChild(o);
                for (Object rowObject : tableTagNode.evaluateXPath("//tr")) {
                    TagNode rowTagNode = (TagNode) rowObject;
                    int i = 0;
                    for (Object ignored : rowTagNode.evaluateXPath("//td"))
                        i++;
                    if (i == 6 && !rowTagNode.hasAttribute("bgcolor")) {
                        i = 0;
                        if (result.getHome() != null) {
                            results.add(result);
                            result = new Result();
                        }
                        result.setRound(round);
                        for (Object object : rowTagNode.evaluateXPath("//td/text()")) {
                            String s = object.toString().replace("\n", "");
                            while (s.contains("  "))
                                s = s.replace("  ", " ");
                            s = s.trim();
                            try {
                                switch (i) {
                                    case 1:
                                        result.setHome(new Club(s));
                                        break;
                                    case 2:
                                        result.setAway(new Club(s));
                                        break;
                                    case 3:
                                        result.setScore(s);
                                        break;
                                    case 4:
                                        try {
                                            result.setViewers(Integer.valueOf(s));
                                        } catch (NumberFormatException e) {
                                            result.setViewers(-1);
                                        }
                                    case 5:
                                        result.setNote(s);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    } else if (rowTagNode.getChildTags().length > 0 && rowTagNode.getChildTags()[0].hasAttribute("bgcolor")) {
                        if (result.getNote() == null || result.getNote().trim().equals(""))
                            result.setNote(Utils.getStringXpath(rowTagNode, "text()"));
                        else
                            result.setNote(result.getNote() + ", " + Utils.getStringXpath(rowTagNode, "text()"));
                        results.add(result);
                        result = new Result();
                    }
                }
                round++;
            }
            if (!results.contains(result) && result.getHome() != null)
                results.add(result);

        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return results;
    }

    private static List<Club> getClubs(TagNode root) throws MalformedURLException, XPatherException {
        List<Club> clubs = new LinkedList<>();
        root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div//table[2]", "utf-8"));
        String htmlTable = getSerializedHtml(root, "//tr[@bgcolor='#f8f8f8']") + "\n" + getSerializedHtml(root, "//tr[@bgcolor='#ffffff']");
        String format = DEFAULT_FORMAT;
        while (htmlTable.length() > 0) {
            if (!htmlTable.contains("</tr>"))
                break;
            String row = htmlTable.substring(0, htmlTable.indexOf("</tr>"));
            htmlTable = htmlTable.substring(htmlTable.indexOf("</tr>") + 5);
            String rowFormat = format;
            Club newClub = new Club();
            while (rowFormat.contains("%") && row.contains("</td>")) {
                rowFormat = rowFormat.substring(rowFormat.indexOf("%") + 1);
                String actual;
                String node = row.substring(0, row.indexOf("</td>"));
                node = node.replaceAll("<.*?>", "").replaceAll("\n", " ").trim();
                row = row.substring(row.indexOf("</td>") + 5);
                if (rowFormat.contains("%"))
                    actual = rowFormat.substring(0, rowFormat.indexOf("%")).trim().toLowerCase();
                else
                    actual = rowFormat.trim().toLowerCase();
                switch (actual) {
                    case "name":
                        newClub.setName(node);
                        break;
                    case "wins":
                        newClub.setWinnings(Integer.valueOf(node));
                        break;
                    case "draws":
                        newClub.setDraws(Integer.valueOf(node));
                        break;
                    case "losses":
                        newClub.setLosses(Integer.valueOf(node));
                        break;
                    case "score":
                        if (node.contains(":")) {
                            newClub.setScoredGoals(Integer.valueOf(node.substring(0, node.indexOf(":")).trim()));
                            newClub.setReceivedGoals(Integer.valueOf(node.substring(1 + node.indexOf(":")).trim()));
                        }
                        break;
                    case "pointstruth":
                        newClub.setPointsTruth(Integer.valueOf(node.replace("(", "").replace(")", "").trim()));
                        break;
                }
            }
            clubs.add(newClub);
        }
        return clubs;
    }

    public static void removeClubsAndResultsFromDB(SQLiteDatabase db, int id) {
        db.execSQL("DELETE FROM clubs WHERE id_leagues = " + id);
        db.execSQL("DELETE FROM results WHERE id_leagues = " + id);//TODO udelat update - u vseho, i u tymu a u dalsich 3,14covin
        db.execSQL("DELETE FROM next_matches WHERE id_leagues = " + id);//TODO udelat update - u vseho, i u tymu a u dalsich 3,14covin
    }

    public void updateClubsAndResults(SQLiteDatabase db, int id) {
        db.execSQL("UPDATE leagues SET updated = datetime('now') WHERE _id = " + id);
        removeClubsAndResultsFromDB(db, id);
        insertIntoDB(db, id);
    }

    public void insertIntoDB(SQLiteDatabase db, int id) {
        insertClubs(db, id);
        HashMap<String, Integer> clubsHashMap = new HashMap<>();
        Cursor cursor = db.rawQuery("SELECT _id, name FROM clubs WHERE id_leagues = " + id, null);
        while (cursor.moveToNext())
            clubsHashMap.put(cursor.getString(1), cursor.getInt(0));
        cursor.close();
        for (Result result : results)
            try {
                db.execSQL(result.getSqlInsert(id, clubsHashMap.get(result.getHome().getName()), clubsHashMap.get(result.getAway().getName())));
            } catch (Exception e) {
                Log.w("League error", e.getMessage());
            }
        for (NextMatch nextMatch : nextMatches)
            try {
                nextMatch.setIdClubsHome(clubsHashMap.get(nextMatch.getClubsHome()));
                nextMatch.setIdClubsAway(clubsHashMap.get(nextMatch.getClubsAway()));
                nextMatch.setIdLeagues(id);
                db.execSQL(nextMatch.getSqlInsert());
            } catch (Exception e) {
                Log.w("League error", e.getMessage());
            }
    }

    private void insertClubs(SQLiteDatabase db, int id) {
        for (Club club : clubs)
            db.execSQL(club.getSqlInsert(id));
    }
}
