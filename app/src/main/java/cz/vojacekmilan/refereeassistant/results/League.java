package cz.vojacekmilan.refereeassistant.results;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cz.vojacekmilan.refereeassistant.Utils;

import static cz.vojacekmilan.refereeassistant.Utils.getCleanTagNodes;
import static cz.vojacekmilan.refereeassistant.Utils.getSerializedHtml;

/**
 * Created by milan on 27.2.15.
 */
public class League {

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

    public List<Result> getRoundResults(int round) {
        List<Result> out = new LinkedList<>();
        for (Result result : results)
            if (result.getRound() == round)
                out.add(result);
        return out;
    }

    public int getLastRound() {
        int max = 0;
        for (Result result : results)
            if (result.getRound() > max)
                max = result.getRound();
        return max;
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

    public static League getLeague(SQLiteDatabase db, int id) {
        Cursor leagueCursor = db.rawQuery("SELECT url FROM leagues WHERE _id = " + id, null);
        String url = null;
        if (leagueCursor.moveToNext())
            url = leagueCursor.getString(0);
        leagueCursor.close();
        try {
            return getLeague(url, getLastCompleteRound(db, id));
        } catch (MalformedURLException | XPatherException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static League getLeague(String url, int lastCompleteRound) throws MalformedURLException, XPatherException {
        return getLeague(new League(url), lastCompleteRound);
    }

    public static League getLeague(final League league, final int lastCompleteRound) throws MalformedURLException, XPatherException {
        String clubsUrl = league.getUrlString();
        clubsUrl = (clubsUrl.contains("&") ? clubsUrl.substring(0, clubsUrl.indexOf("&")) : clubsUrl) + "&show=Aktual";
        final TagNode rootClubs = getCleanTagNodes(new URL(clubsUrl), Results.CHARSET);
        final CountDownLatch latch = new CountDownLatch(3);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    league.setClubs(getClubs(rootClubs));
                    latch.countDown();
                } catch (MalformedURLException | XPatherException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                league.setNextMatches(getNextMatches(rootClubs));
                latch.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultsUrl = league.getUrlString();
                resultsUrl = (resultsUrl.contains("&") ? resultsUrl.substring(0, resultsUrl.indexOf("&")) : resultsUrl) + "&show=Vysledky";
                try {
                    TagNode rootResults = getCleanTagNodes(new URL(resultsUrl), Results.CHARSET);
                    league.setResults(getResults(rootResults, lastCompleteRound));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        }).start();
        try {
            latch.await();
            return league;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<NextMatch> getNextMatches(TagNode root) {
        List<NextMatch> nextMatches = new ArrayList<>();
        root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div//table[3]"));
        try {
            for (Object o : root.evaluateXPath("//tr[@bgcolor='#f8f8f8']"))
                nextMatches.add(new NextMatch(((TagNode) o).evaluateXPath("//td/text()")));
            for (Object o : root.evaluateXPath("//tr[@bgcolor='#ffffff']"))
                nextMatches.add(new NextMatch(((TagNode) o).evaluateXPath("//td/text()")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextMatches;
    }

    private static List<Result> getResults(TagNode root, int lastCompleteRound) {
        final List<Result> results = new ArrayList<>();
        root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div", "utf-8"));
        List<Thread> threads = new LinkedList<>();
        try {
            int round = 1;
            Log.i("getResults", "lastCompleteRound = " + lastCompleteRound);
            for (final Object tableObject : root.evaluateXPath("//table")) {
                if (round > lastCompleteRound) {
                    final int finalRound = round;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            results.addAll(getResultsFromTable((TagNode) tableObject, finalRound));
                        }
                    });
                    threads.add(thread);
                    thread.start();
                }
                round++;
            }

        } catch (XPatherException e) {
            e.printStackTrace();
        }
        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        return results;
    }

    private static List<Result> getResultsFromTable(TagNode tableTagNode, int round) {
        List<Result> results = new LinkedList<>();
        Result result = new Result();
        try {
            for (Object o : tableTagNode.evaluateXPath("//tr[@bgcolor='#aaaaaa']"))
                tableTagNode.removeChild(o);
            for (Object rowObject : tableTagNode.evaluateXPath("//tr")) {
                TagNode rowTagNode = (TagNode) rowObject;
                int i = 0;
                for (Object ignored : rowTagNode.evaluateXPath("//td"))
                    i++;
                if (i == 6 && !rowTagNode.hasAttribute("bgcolor")) {
                    if (result.getHome() != null)
                        results.add(result);
                    result = new Result(rowTagNode.evaluateXPath("//td/text()"));
                    result.setRound(round);
                } else if (rowTagNode.getChildTags().length > 0 && rowTagNode.getChildTags()[0].hasAttribute("bgcolor"))
                    result.setNote((result.getNote() == null || result.getNote().trim().equals("")) ?
                            Utils.getStringXpath(rowTagNode, "text()") :
                            result.getNote() + ", " + Utils.getStringXpath(rowTagNode, "text()"));
            }
            if (!results.contains(result) && result.getHome() != null)
                results.add(result);
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return results;
    }

    private static List<Club> getClubs(TagNode root) throws MalformedURLException, XPatherException {
        final List<Club> clubs = new LinkedList<>();
        root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div//table[2]", "utf-8"));
        String htmlTable = "<html><head></head><body><table><tbody>" + getSerializedHtml(root, "//tr[@bgcolor='#f8f8f8']") + getSerializedHtml(root, "//tr[@bgcolor='#ffffff']") + "</tbody></table></body></html>";
        TagNode htmlTableTagNode = getCleanTagNodes(htmlTable);
        List<Thread> threads = new ArrayList<>();
        for (Object o : htmlTableTagNode.evaluateXPath("//tr")) {
            final TagNode tn = (TagNode) o;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    clubs.add(getClubFromRow(tn));
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        return clubs;
    }

    private static Club getClubFromRow(TagNode rowTagNode) {
        Club newClub = new Club();
        int i = 0;
        try {
            for (Object o : rowTagNode.evaluateXPath("//td/text()")) {
                String node = o.toString();
                switch (i) {
                    case 1:
                        newClub.setName(node);
                        break;
                    case 3:
                        newClub.setWinnings(Integer.valueOf(node));
                        break;
                    case 4:
                        newClub.setDraws(Integer.valueOf(node));
                        break;
                    case 5:
                        newClub.setLosses(Integer.valueOf(node));
                        break;
                    case 6:
                        if (node.contains(":")) {
                            newClub.setScoredGoals(Integer.valueOf(node.substring(0, node.indexOf(":")).trim()));
                            newClub.setReceivedGoals(Integer.valueOf(node.substring(1 + node.indexOf(":")).trim()));
                        }
                        break;
                    case 9:
                        newClub.setPointsTruth(Integer.valueOf(node.replace("(", "").replace(")", "").trim()));
                        break;
                }
                i++;
            }
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        return newClub;
    }

    private static int getLastCompleteRound(SQLiteDatabase db, int idLeague) {
        Cursor cursor = db.rawQuery("SELECT _id FROM clubs WHERE id_leagues = " + idLeague, null);
        int matchesCount = cursor.getCount() / 2;
        cursor = db.rawQuery("SELECT MAX(round) FROM results WHERE id_leagues = " + idLeague, null);
        int lastRound = 0;
        if (cursor != null && cursor.moveToNext())
            lastRound = cursor.getInt(0);
        if (matchesCount == 0 || lastRound == 0) return 0;
        for (int i = 1; true; i++) {
            cursor = db.rawQuery("SELECT round FROM results WHERE round = " + i + " AND id_leagues = " + idLeague, null);
            if (cursor.getCount() < matchesCount || i == lastRound) {
                cursor.close();
                db.execSQL("DELETE FROM results WHERE round > " + i + " AND id_leagues = " + idLeague);
                return i;
            }
        }
    }

    public void updateClubsAndResults(SQLiteDatabase db, int id) {
        db.execSQL("UPDATE leagues SET updated = datetime('now') WHERE _id = " + id);
        db.execSQL("DELETE FROM next_matches WHERE id_leagues = " + id);
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
                db.execSQL(result.getSqlInsert(id, clubsHashMap.get(result.getHome()), clubsHashMap.get(result.getAway())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        for (NextMatch nextMatch : nextMatches)
            try {
                nextMatch.setIdClubsHome(clubsHashMap.get(nextMatch.getClubsHome()));
                nextMatch.setIdClubsAway(clubsHashMap.get(nextMatch.getClubsAway()));
                nextMatch.setIdLeagues(id);
                db.execSQL(nextMatch.getSqlInsert());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void insertClubs(SQLiteDatabase db, int id) {
        Cursor cursor = db.rawQuery("SELECT _id FROM clubs WHERE id_leagues = " + id, null);
        if (cursor.getCount() == 0)
            for (Club club : clubs)
                db.execSQL(club.getSqlInsert(id));
        else
            for (Club club : clubs)
                db.execSQL(club.getSqlUpdate(id));
        cursor.close();
    }
}
