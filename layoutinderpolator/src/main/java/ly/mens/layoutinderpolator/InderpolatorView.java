package ly.mens.layoutinderpolator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InderpolatorView extends FrameLayout {
    public static final String LOGTAG = "LIS";

    private static final float BUFFER_FRACTION = 0.1f;
    private static final float ANIMATE_SNAP = 0.3f;
    private static final float MOVE_THRESHOLD = 0.02f;
    private static final float SWITCH_THRESHOLD = 0.00001f;
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
    private Interpolator interpolator = new LinearInterpolator();
    private float initialX;
    private float initialTouchPosition;
    private PageFactory<?> factory;

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

    public void setCurrentPosition(float currentPosition, boolean animated) {
        // TODO: Support animating
        setCurrentPosition(currentPosition);
    }

    public void setCurrentPosition(float currentPosition) {
        this.currentPosition = Math.min(Math.max(0, currentPosition), getChildCount() - 1);
        updatePositions();
    }

    public boolean next(boolean animated) {
        int current = getPage();
        if (current < getChildCount() - 1) {
            setCurrentPosition(current + 1, animated);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean previous(boolean animated) {
        int current = getPage();
        if (current > 0) {
            // TODO: Animate to this position
            setCurrentPosition(current - 1, animated);
            return true;
        }
        else {
            return false;
        }
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
        if (changed) {
            setupAnimation();
            updatePositions();
        }
    }

    private void setupAnimation() {
        if (pages != null) {
            for (Page p : pages) {
                p.reset();
            }
        }
        int pageCount = getChildCount();
        pages = new ArrayList<>(pageCount);
        if (factory == null) {
            for (int i = 0; i < pageCount; i++) {
                // Register new page
                pages.add(new Page(getChildAt(i)));
            }
        }
        else {
            for (int i = 0; i < pageCount; i++) {
                // Register new page
                pages.add(factory.createPage(getChildAt(i)));
            }
        }
    }
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
                if (Math.abs(diff) < ANIMATE_SNAP) {
                    // Return to original position
                    setCurrentPosition(initialTouchPosition, true);
                }
                else {
                    // Move to next/previous page
                    setCurrentPosition(Math.round(initialTouchPosition - diff), true);
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
                    p.container.setVisibility(View.INVISIBLE);
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
                    List<Integer> intersection = new ArrayList<>(current.getIntersection(next));
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
        final float phase = interpolator.getInterpolation(currentPosition - thisPage);
        final float phaseInv = 1 - phase;
        // Panning views simply move linearly off the page
        for (View v : this.panLeft) {
            v.setTranslationX(-width * phase);
            v.setAlpha(phaseInv * current.getInfo(v.getId()).alpha);
        }
        for (View v : this.panRight) {
            v.setTranslationX(width * phaseInv);
            v.setAlpha(phase * next.getInfo(v.getId()).alpha);
        }
        final Iterator<View> leftIter = this.interpolatedLeft.iterator();
        final Iterator<View> rightIter = this.interpolatedRight.iterator();
        if (phase > SWITCH_THRESHOLD) {
            while (leftIter.hasNext() && rightIter.hasNext()) {
                final View leftView = leftIter.next();
                final View rightView = rightIter.next();
                final ViewInfo leftInfo = current.getInfo(leftView.getId());
                final ViewInfo rightInfo = next.getInfo(rightView.getId());
                if (leftInfo.isColocated(rightInfo)) {
                    // Show both views
                    leftView.setVisibility(View.VISIBLE);
                    rightView.setVisibility(View.VISIBLE);
                    leftInfo.reset(leftView);
                    rightInfo.reset(rightView);
                    // Fade out left view
                    leftView.setAlpha(leftInfo.alpha * phaseInv);
                    // Fade in right view
                    rightView.setAlpha(rightInfo.alpha * phase);
                }
                else {
                    leftView.setVisibility(View.INVISIBLE); // Hide view, as new view will take its place
                    // Display this view as it comes in
                    rightView.setVisibility(View.VISIBLE);
                    rightInfo.applyInterpolation(rightView, leftInfo, phaseInv);
                }
            }
        }
        else {
            while (leftIter.hasNext() && rightIter.hasNext()) {
                // Display the page as normal
                View leftView = leftIter.next();
                current.getInfo(leftView.getId()).reset(leftView);
                leftView.setVisibility(View.VISIBLE);
                rightIter.next().setVisibility(View.INVISIBLE); // Hide views from next page
            }
        }
    }
}