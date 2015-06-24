package cz.vojacekmilan.refereeassistant;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

/**
 * Created by milan on 15.4.15.
 */
public class DrawerItem {
    private Drawable icon;
    private String name;

    // Constructor.
    public DrawerItem(Drawable icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void activate() {
        if (icon != null)
            icon.setColorFilter(new PorterDuffColorFilter(R.color.primary_color, PorterDuff.Mode.MULTIPLY));
    }

    public void deactivate() {
        if (icon != null)
            icon.clearColorFilter();
    }

    @Override
    public String toString() {
        return name;
    }

}
