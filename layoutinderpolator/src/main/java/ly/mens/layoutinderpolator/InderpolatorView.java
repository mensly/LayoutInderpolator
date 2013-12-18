package ly.mens.layoutinderpolator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class InderpolatorView extends FrameLayout {
    private static final float BUFFER_FRACTION = 0.1f;
    private float currentPosition = 0;

    public InderpolatorView(Context context) {
        super(context);
    }

    public InderpolatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InderpolatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Which child view index is currently focused
     */
    public int getPage() {
        return Math.min(Math.max((int)(currentPosition + BUFFER_FRACTION), 0), getChildCount() - 1);
    }

    public float getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(float currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO: Adjust position of subviews based on touch movement
        return super.onTouchEvent(event);
    }
}