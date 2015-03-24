package cz.vojacekmilan.refereeassistant.results;

import java.util.List;

/**
 * Created by milan on 27.2.15.
 */
public class Region {
    private int id;
    private String name;
    private boolean favourite;
    private List<Region> subRegions;
    private List<League> leagues;

    public Region(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Region(String name, List<Region> subRegions, List<League> leagues) {
        this.name = name;
        this.subRegions = subRegions;
        this.leagues = leagues;
    }

    public Region(int id, String name, List<Region> subRegions, List<League> leagues) {
        this.id = id;
        this.name = name;
        this.subRegions = subRegions;
        this.leagues = leagues;
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

    public List<Region> getSubRegions() {
        return subRegions;
    }

    public void setSubRegions(List<Region> subRegions) {
        this.subRegions = subRegions;
    }

    public List<League> getLeagues() {
        return leagues;
    }

    public void setLeagues(List<League> leagues) {
        this.leagues = leagues;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public void negFavourite() {
        this.favourite = !favourite;
    }

    @Override
    public String toString() {
        return name + (favourite ? " *" : "");
    }
}
