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
    public final float alpha;

    public ViewInfo(View view) {
        this.id = view.getId();
        this.width = view.getWidth();
        this.height = view.getHeight();
        this.x = view.getX();
        this.y = view.getY();
        this.alpha = (view.getVisibility() == View.VISIBLE) ? 1 : 0;
    }

    private static float interpolate(float x0, float x1, float phase) {
        return x0 + (x1 - x0) * phase;
    }

    public void applyInterpolation(View view, ViewInfo next, float phase) {
        if (this.id == next.id && this.id == view.getId()) {
            view.setAlpha(interpolate(alpha, next.alpha, phase));
            if (width == next.width) {
                view.setScaleX(1);
            }
            else {
                view.setScaleX(interpolate(1, (float)next.width / width, phase));
            }
            if (height == next.height) {
                view.setScaleY(1);
            }
            else {
                view.setScaleY(interpolate(1, (float)next.height / height, phase));
            }

            view.setX(interpolate(x, next.x, phase));
            view.setY(interpolate(y, next.y, phase));
        }
        else {
            throw new RuntimeException("Mismatched ids used for interpolation");
        }
    } 
    public void reset(View view) {
        if (id == view.getId()) {
            view.setX(x);
            view.setY(y);
            view.setAlpha(alpha);
            view.setScaleX(1);
            view.setScaleY(1);
        }
        else {
            throw new RuntimeException("Mismatched ids used to reset");
        }
    }
}
