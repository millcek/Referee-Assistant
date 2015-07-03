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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegionItem that = (RegionItem) o;

        if (id != that.id) return false;
        if (icon != that.icon) return false;
        return !(text != null ? !text.equals(that.text) : that.text != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + icon;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return text;
    }
}
