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
    private DateTime dateTime;

    public Result(Club home, Club away, int homeScore, int awayScore, DateTime dateTime) {
        this.home = home;
        this.away = away;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.dateTime = dateTime;
    }

    public Club getHome() {
        return home;
    }

    public Club getAway() {
        return away;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public DateTime getDateTime() {
        return dateTime;
    }
}
