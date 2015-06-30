package cz.vojacekmilan.refereeassistant;

/**
 * Created by milan on 15.4.15.
 */
public class DrawerItem {
    private int icon;
    private String name;
    private boolean active;

    // Constructor.
    public DrawerItem(int icon, String name) {
        this.icon = icon;
        this.name = name;
        active = false;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void activate() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    @Override
    public String toString() {
        return name;
    }

}
