package cz.vojacekmilan.refereeassistant.results;

import java.util.List;

/**
 * Created by milan on 27.2.15.
 */
public class League {
    private int id;
    private String name;
    private String urlString;
    private List<Result> results;
    private List<Club> clubs;

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

    public League(int id, String name) {
        this.id = id;
        this.name = name;
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
        return name;
    }
}
