package cz.vojacekmilan.refereeassistant;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

/**
 * Created by milan on 15.4.15.
 */
public class ObjectDrawerItem {
    private Drawable icon;
    private String name;

    // Constructor.
    public ObjectDrawerItem(Drawable icon, String name) {
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
        icon.setColorFilter(new PorterDuffColorFilter(R.color.active, PorterDuff.Mode.MULTIPLY));
    }

    public void deactivate() {
        icon.clearColorFilter();
    }
}
