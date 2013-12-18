package ly.mens.layoutinderpolator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class InderpolatorView extends FrameLayout {
    private static final float BUFFER_FRACTION = 0.1f;
    private float currentPosition = 0;
    private List<Page> pages;
    private boolean isLaidOut;

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
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (isLaidOut) {
            setupAnimation();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        isLaidOut = true;
        setupAnimation();
    }

    private void setupAnimation() {
        int pageCount = getChildCount();
        pages = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            // Register new page
            pages.add(new Page(getChildAt(i)));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO: Adjust position of subviews based on touch movement
        return super.onTouchEvent(event);
    }
}