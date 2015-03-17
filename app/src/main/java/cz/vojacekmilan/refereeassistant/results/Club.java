package cz.vojacekmilan.refereeassistant.results;

/**
 * Created by milan on 27.2.15.
 */
public class Club {
    private int id;
    private int rank;
    private String name;
    private int winnings;
    private int draws;
    private int losses;
    private int scoredGoals;
    private int receivedGoals;
    private int pointsTruth;

    public Club() {
    }

    public Club(int id, int rank, String name, int winnings, int draws, int losses, int scoredGoals, int receivedGoals, int pointsTruth) {
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.winnings = winnings;
        this.draws = draws;
        this.losses = losses;
        this.scoredGoals = scoredGoals;
        this.receivedGoals = receivedGoals;
        this.pointsTruth = pointsTruth;
    }

    public Club(String name) {
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int order) {
        this.rank = order;
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

    public int getWinnings() {
        return winnings;
    }

    public void setWinnings(int winnings) {
        this.winnings = winnings;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getScoredGoals() {
        return scoredGoals;
    }

    public void setScoredGoals(int scoredGoals) {
        this.scoredGoals = scoredGoals;
    }

    public int getReceivedGoals() {
        return receivedGoals;
    }

    public void setReceivedGoals(int receivedGoals) {
        this.receivedGoals = receivedGoals;
    }

    public int getPointsTruth() {
        return pointsTruth;
    }

    public void setPointsTruth(int pointsTruth) {
        this.pointsTruth = pointsTruth;
    }

    public int getPoints() {
        return winnings * 3 + draws;
    }

    public String getScore() {
        return scoredGoals + ":" + receivedGoals;
    }

    public int[] getColumnValues() {
        return new int[]{winnings + draws + losses, winnings, draws, losses};
    }

    public String getSqlInsert(int idLeagues) {
        return String.format("INSERT INTO clubs (rank, name, winnings, draws, losses, scored_goals, received_goals, points_truth, id_leagues) VALUES (%d, '%s', %d, %d, %d, %d, %d, %d, %d);",
                rank, name, winnings, draws, losses, scoredGoals, receivedGoals, pointsTruth, idLeagues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Club club = (Club) o;
        return draws == club.draws && losses == club.losses && pointsTruth == club.pointsTruth && receivedGoals == club.receivedGoals && scoredGoals == club.scoredGoals && winnings == club.winnings && name.equals(club.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + winnings;
        result = 31 * result + draws;
        result = 31 * result + losses;
        result = 31 * result + scoredGoals;
        result = 31 * result + receivedGoals;
        result = 31 * result + pointsTruth;
        return result;
    }

    @Override
    public String toString() {
        return "Club{" +
                "name='" + name + '\'' +
                ", winnings=" + winnings +
                ", draws=" + draws +
                ", losses=" + losses +
                ", scoredGoals=" + scoredGoals +
                ", receivedGoals=" + receivedGoals +
                ", pointsTruth=" + pointsTruth +
                '}';
    }
}
