package ly.mens.layoutinderpolator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InderpolatorView extends FrameLayout {
    private static final String LOGTAG = "LIS";

    private static final float BUFFER_FRACTION = 0.1f;
    private static final float ANIMATE_SNAP = 0.3f;
    private static final float MOVE_THRESHOLD = 0.02f;
    private float currentPosition = 0;
    private int lastUpdatePage = -1;
    private List<Page> pages;
    private boolean isLaidOut;
    private Collection<View> interpolatedLeft;
    private Collection<View> interpolatedRight;
    private Collection<View> panLeft;
    private Collection<View> panRight;
    private Page current;
    private Page next;

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
        this.currentPosition = Math.min(Math.max(0, currentPosition), pages.size() - 1);
        updatePositions();
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
        updatePositions();
    }

    private void setupAnimation() {
        int pageCount = getChildCount();
        pages = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            // Register new page
            pages.add(new Page(getChildAt(i)));
        }
    }

    float initialX;
    float initialTouchPosition;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // TODO: Cancel animation
                initialX = event.getX();
                initialTouchPosition = currentPosition;
                handled = true;
                break;
            case MotionEvent.ACTION_MOVE: {
                float diff = (event.getX() - initialX) / getWidth();
                if (Math.abs(diff) > MOVE_THRESHOLD) {
                    setCurrentPosition(initialTouchPosition - diff);
                    handled = true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                float diff = (event.getX() - initialX) / getWidth();
                // TODO: Animate to this position
                if (Math.abs(diff) < ANIMATE_SNAP) {
                    // Return to original position
                    setCurrentPosition(initialTouchPosition);
                }
                else {
                    // Move to next/previous page
                    setCurrentPosition(Math.round(initialTouchPosition - diff));
                }
                break;
            }
        }
        return handled;
    }

    private void updatePositions() {
        int thisPage = (int)currentPosition;
        if (thisPage != lastUpdatePage) {
            if (pages != null) {
                for (Page p : pages) {
                    p.container.setVisibility(View.GONE);
                }
                // Get the pages of the transition
                current = pages.get(thisPage);
                if (thisPage < pages.size() - 1) {
                    next = pages.get(thisPage + 1);
                    // Show relevant pages
                    current.container.setVisibility(View.VISIBLE);
                    next.container.setVisibility(View.VISIBLE);
                    // Build up lists of relevant views between the two pages
                    this.panLeft = current.getViews(current.getCompliment(next));
                    this.panRight = next.getViews(next.getCompliment(current));
                    Collection<Integer> intersection = current.getIntersection(next);
                    this.interpolatedLeft = current.getViews(intersection);
                    this.interpolatedRight = next.getViews(intersection);
                }
                else {
                    next = null;
                    // Final pages does not move any further
                    current.container.setVisibility(View.VISIBLE);
                    current.reset();
                    // Clear all lists
                    this.panLeft = this.panRight =
                            this.interpolatedRight = this.interpolatedLeft = Collections.emptyList();
                }
            }
            lastUpdatePage = thisPage;
        }
        int width = getWidth();
        float phase = currentPosition - thisPage;
        // Panning views simply move linearly off the page
        for (View v : this.panLeft) {
            v.setTranslationX(-width * phase);
        }
        for (View v : this.panRight) {
            v.setTranslationX(width * (1 - phase));
        }
        // TODO: Handle panning views
        // TODO: Handle interpolated views
    }
}