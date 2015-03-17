package cz.vojacekmilan.refereeassistant.results;

import org.joda.time.DateTime;

/**
 * Created by milan on 27.2.15.
 */
public class Result {
    private Club home;
    private Club away;
    private int homeScore;
    private int awayScore;
    private int homeScoreHalf;
    private int awayScoreHalf;
    private DateTime dateTime;
    private int viewers;
    private String note;
    private int round;


    public Result(Club home, Club away, int homeScore, int awayScore, int homeScoreHalf, int awayScoreHalf, DateTime dateTime) {
        this.home = home;
        this.away = away;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeScoreHalf = homeScoreHalf;
        this.awayScoreHalf = awayScoreHalf;
        this.dateTime = dateTime;
    }

    public Result(String home, String away, int homeScore, int awayScore, int homeScoreHalf, int awayScoreHalf, DateTime dateTime) {
        this.home = new Club();
        this.home.setName(home);
        this.away = new Club();
        this.away.setName(away);
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeScoreHalf = homeScoreHalf;
        this.awayScoreHalf = awayScoreHalf;
        this.dateTime = dateTime;
    }

    public Result() {

    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getViewers() {
        return viewers;
    }

    public void setViewers(int viewers) {
        this.viewers = viewers;
    }

    public Club getHome() {
        return home;
    }

    public void setHome(Club home) {
        this.home = home;
    }

    public Club getAway() {
        return away;
    }

    public void setAway(Club away) {
        this.away = away;
    }

    public void setAway(String away) {
        this.away = new Club(away);
    }

    public void setScore(String score) {
        if (score.contains("(")) {
            String halfScore = score.substring(score.indexOf("("));
            score = score.substring(0, score.indexOf("("));
            if (halfScore.length() > 4) {
                try {
                    halfScore = halfScore.replace("(", "");
                    halfScore = halfScore.replace(")", "");
                    halfScore = halfScore.replace(" ", "");
                    halfScore = halfScore.replace("\n", "");
                    int pos = halfScore.indexOf(":");
                    this.homeScoreHalf = Integer.valueOf(halfScore.substring(0, pos));
                    this.awayScoreHalf = Integer.valueOf(halfScore.substring(pos + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                    this.homeScoreHalf = 0;
                    this.awayScoreHalf = 0;
                }
            }
            try {
                score = score.replace("(", "").replace(")", "").replace(" ", "").trim();
                int pos = score.indexOf(":");
                this.homeScore = Integer.valueOf(score.substring(0, pos));
                this.awayScore = Integer.valueOf(score.substring(pos + 1));
            } catch (Exception e) {
                e.printStackTrace();
                this.homeScore = 0;
                this.awayScore = 0;
            }
        }
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public int getHomeScoreHalf() {
        return homeScoreHalf;
    }

    public void setHomeScoreHalf(int homeScoreHalf) {
        this.homeScoreHalf = homeScoreHalf;
    }

    public int getAwayScoreHalf() {
        return awayScoreHalf;
    }

    public void setAwayScoreHalf(int awayScoreHalf) {
        this.awayScoreHalf = awayScoreHalf;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getSqlInsert(int idLeague, int idClubsHome, int idClubsAway) {
        return String.format("INSERT INTO results (id_clubs_home, id_clubs_away, home_score, away_score, away_score_half, home_score_half, round, datetime, note, id_leagues) VALUES (%d, %d, %d, %d, %d, %d, %d, %d,'%s', %d);",
                idClubsHome, idClubsAway, homeScore, awayScore, awayScoreHalf, homeScoreHalf, round, (int) (dateTime.getMillis() / 1000), note, idLeague);
    }

    @Override
    public String toString() {
        return "\nResult{" +
                "home=" + home +
                ", away=" + away +
                ", homeScore=" + homeScore +
                ", awayScore=" + awayScore +
                ", homeScoreHalf=" + homeScoreHalf +
                ", awayScoreHalf=" + awayScoreHalf +
                ", dateTime=" + dateTime +
                "}\n";
    }
}
