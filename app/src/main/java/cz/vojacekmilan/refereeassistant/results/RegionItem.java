package cz.vojacekmilan.refereeassistant.results;

/**
 * Created by milan on 24.6.15.
 */
public class RegionItem {
    private int id;
    private int icon;
    private String text;

    public RegionItem(int id, int icon, String text) {
        this.id = id;
        this.icon = icon;
        this.text = text;
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

    @Override
    public String toString() {
        return text;
    }
}
