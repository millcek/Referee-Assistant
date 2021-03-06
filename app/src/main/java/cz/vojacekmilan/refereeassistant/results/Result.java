package cz.vojacekmilan.refereeassistant.results;

/**
 * Created by milan on 27.2.15.
 */
public class Result {
    private String home;
    private String away;
    private int idHome;
    private int idAway;
    private int homeScore;
    private int awayScore;
    private int homeScoreHalf;
    private int awayScoreHalf;
    private int viewers;
    private String note;
    private int round;

    public Result(String home, String away, int homeScore, int awayScore, int awayScoreHalf, int homeScoreHalf, int viewers, String note) {
        this.home = home;
        this.away = away;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.awayScoreHalf = awayScoreHalf;
        this.homeScoreHalf = homeScoreHalf;
        this.viewers = viewers;
        this.note = note;
    }

    public Result(Object[] values) {
        int i = 0;
        for (Object object : values) {
            String s = object.toString().replace("\n", "");
            while (s.contains("  "))
                s = s.replace("  ", " ");
            s = s.trim();
            try {
                switch (i) {
                    case 1:
                        this.setHome(s);
                        break;
                    case 2:
                        this.setAway(s);
                        break;
                    case 3:
                        this.setScore(s);
                        break;
                    case 4:
                        try {
                            this.setViewers(Integer.valueOf(s));
                        } catch (NumberFormatException e) {
                            this.setViewers(-1);
                        }
                    case 5:
                        this.setNote(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public Result() {

    }

    public int getIdHome() {
        return idHome;
    }

    public void setIdHome(int idHome) {
        this.idHome = idHome;
    }

    public int getIdAway() {
        return idAway;
    }

    public void setIdAway(int idAway) {
        this.idAway = idAway;
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

    public void setScore(String score) {
        this.homeScoreHalf = -1;
        this.awayScoreHalf = -1;
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
                }
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

    public String getSqlInsert(int idLeague, int idClubsHome, int idClubsAway) {
        return String.format("INSERT INTO results (id_clubs_home, id_clubs_away, home_score, away_score, " +
                        "away_score_half, home_score_half, round, note, id_leagues) VALUES (%d, %d, %d, %d, %d, %d, %d,'%s', %d);",
                idClubsHome, idClubsAway, homeScore, awayScore, awayScoreHalf, homeScoreHalf, round, note, idLeague);
    }

    @Override
    public String toString() {
        return "Result{" +
                "home=" + home +
                ", away=" + away +
                ", homeScore=" + homeScore +
                ", awayScore=" + awayScore +
                ", homeScoreHalf=" + homeScoreHalf +
                ", awayScoreHalf=" + awayScoreHalf +
                ", viewers=" + viewers +
                ", note='" + note + '\'' +
                ", round=" + round +
                "}\n";
    }

    public String getResultHalf() {
        if ((homeScore == 0 && awayScore == 0) || homeScoreHalf == -1 || awayScoreHalf == -1)
            return "";
        return String.format("%d:%d", homeScoreHalf, awayScoreHalf);
    }

    public String getResult() {
        return String.format("%d:%d", homeScore, awayScore);
    }

    public void setScore(int homeScore, int awayScore, int homeScoreHalf, int awayScoreHalf) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeScoreHalf = homeScoreHalf;
        this.awayScoreHalf = awayScoreHalf;
    }
}
