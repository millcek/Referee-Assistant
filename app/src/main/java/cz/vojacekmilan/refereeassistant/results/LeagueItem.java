package cz.vojacekmilan.refereeassistant.results;

import cz.vojacekmilan.refereeassistant.R;

/**
 * Created by milan on 24.6.15.
 */
public class LeagueItem {
    private int id;
    private int icon;
    private String text;
    private boolean favourite;
    private int background;

    public LeagueItem(int id, int icon, String text, boolean favourite) {
        this.id = id;
        this.icon = icon;
        this.text = text;
        this.setFavourite(favourite);
    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        background = favourite ? R.color.favourite : R.color.transparent;
        this.favourite = favourite;
    }

    public int getBackground() {
        return this.background;
    }

    @Override
    public String toString() {
        return text;
    }
}
