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

    /**
     * Compare locations of views
     * @return True if the position and size of the two views is equal
     */
    public boolean isColocated(ViewInfo other) {
        return width == other.width && height == other.height &&
                Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewInfo viewInfo = (ViewInfo) o;

        if (!isColocated(viewInfo)) return false;
        if (Float.compare(viewInfo.alpha, alpha) != 0) return false;
        if (id != viewInfo.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (alpha != +0.0f ? Float.floatToIntBits(alpha) : 0);
        return result;
    }
}
