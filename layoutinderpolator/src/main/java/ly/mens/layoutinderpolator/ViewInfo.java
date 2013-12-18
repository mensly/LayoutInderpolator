package ly.mens.layoutinderpolator;

import android.view.View;

/**
 * Position information for a view
 * Created by mensly on 18/12/2013.
 */
public class ViewInfo {
    public final int id;
    public final int width;
    public final int height;
    public final float x;
    public final float y;

    public ViewInfo(View view) {
        this.id = view.getId();
        this.width = view.getWidth();
        this.height = view.getHeight();
        this.x = view.getX();
        this.y = view.getY();
    }
}
