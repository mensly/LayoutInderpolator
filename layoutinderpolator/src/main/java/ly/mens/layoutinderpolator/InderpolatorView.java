package ly.mens.layoutinderpolator;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InderpolatorView extends FrameLayout {
    public static interface OnPageChangeListener {
        public void onPageChange(InderpolatorView view, int page);
    }

    private static final float BUFFER_FRACTION = 0.1f;
    private static final float ANIMATE_SNAP = 0.2f;
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
    private float initialX;
    private float initialTouchPosition;
    private PageFactory<?> factory;
    private CountDownTimer timer;
    private long transitionMillis = 500;
    private OnPageChangeListener listener;
    private boolean appendOnly = true;
    private float overscroll = 0;

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
     * Get the time it takes for a full transition animation
     * default: 500ms
     */
    public long getTransitionMillis() {
        return transitionMillis;
    }

    /**
     * Set the time it takes for a full transition animation
     */
    public void setTransitionMillis(long transitionMillis) {
        this.transitionMillis = transitionMillis;
    }

    public boolean isAppendOnly() {
        return appendOnly;
    }

    public void setAppendOnly(boolean appendOnly) {
        this.appendOnly = appendOnly;
    }

    public PageFactory<?> getFactory() {
        return factory;
    }

    public void setFactory(PageFactory<?> factory) {
        this.factory = factory;
    }

    public OnPageChangeListener getOnPageChangeListener() {
        return listener;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.listener = listener;
    }

    public int getPageCount() {
        return getChildCount();
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

    public void setCurrentPosition(float targetPosition, boolean animated) {
        if (animated) {
            synchronized (this) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new AnimateTimer(transitionMillis, targetPosition).start();
            }
        }
        else {
            setCurrentPosition(targetPosition);
        }
    }

    public void setCurrentPosition(float currentPosition) {
        this.currentPosition = Math.min(Math.max(0, currentPosition), getChildCount() - 1);
        this.overscroll = currentPosition - this.currentPosition;
        updatePositions();
    }

    /**
     * Go to the next page
     * @return True if not already on the last page
     */
    public boolean next(boolean animated) {
        endAnimation();
        int current = getPage();
        if (current < getChildCount() - 1) {
            setCurrentPosition(current + 1, animated);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Go to the previous page
     * @return True if not already on the first page
     */
    public boolean previous(boolean animated) {
        endAnimation();
        int current = getPage();
        if (current > 0) {
            setCurrentPosition(current - 1, animated);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void addView(final View child, final int index, ViewGroup.LayoutParams params) {
        // Hide subviews by default when added
        if (child instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup)child;
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                group.getChildAt(i).setAlpha(0);
            }
        }
        isLaidOut = false; // Signify the animations should be reset on next layout
        super.addView(child, index, params);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed || !isLaidOut) {
            isLaidOut = true;
            setupAnimation();
            updatePositions();
        }
    }

    synchronized private void setupAnimation() {
        if (pages != null) {
            for (Page p : pages) {
                p.reset();
            }
        }
        int pageCount = getChildCount();
        if (pages == null || !appendOnly) {
            pages = new ArrayList<>(pageCount);
        }
        if (factory == null) {
            for (int i = pages.size(); i < pageCount; i++) {
                // Register new page
                pages.add(new Page(getChildAt(i)));
            }
        }
        else {
            for (int i = pages.size(); i < pageCount; i++) {
                // Register new page
                pages.add(factory.createPage(getChildAt(i)));
            }
        }
        lastUpdatePage = -1;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (this) {
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                }
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
                float targetPosition;
                if (Math.abs(diff) < ANIMATE_SNAP) {
                    // Return to original position
                    targetPosition = Math.round(initialTouchPosition);
                }
                else {
                    // Move to next/previous page
                    targetPosition = Math.round(initialTouchPosition - diff);
                }
                // Remove overscroll
                targetPosition = Math.min(Math.max(0, targetPosition), getChildCount() - 1);
                setCurrentPosition(targetPosition, true);
                break;
            }
        }
        return handled;
    }

    private void updatePositions() {
        if (pages == null) {
            // Not yet set up
            return;
        }
        int thisPage = (int)currentPosition;
        if (thisPage >= getPageCount()) {
            // Edge case, do nothing for now
            return;
        }
        if (thisPage != lastUpdatePage && thisPage < pages.size()) {
            if (listener != null) {
                listener.onPageChange(this, thisPage);
            }
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
        final float phase = currentPosition - thisPage;
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
                    leftView.setAlpha(leftInfo.alpha * (float)Math.sqrt(phaseInv));
                    // Fade in right view
                    rightView.setAlpha(rightInfo.alpha * (float)Math.sqrt(phase));
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
        getChildAt(thisPage).setAlpha(1 - Math.abs(overscroll * 0.75f));
    }

    synchronized private void endAnimation() {
        if (timer != null) {
            timer.cancel();
            timer.onFinish();
            timer = null;
        }
    }

    private class AnimateTimer extends CountDownTimer {
        private final float targetPosition;
        private final float valuePerMilli;
        private AnimateTimer(long transitionMillis, float targetPosition) {
            super((long)(transitionMillis * Math.abs(targetPosition - currentPosition)), 1);
            this.targetPosition = targetPosition;
            this.valuePerMilli = Math.signum(currentPosition - targetPosition) / transitionMillis;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setCurrentPosition(targetPosition + valuePerMilli * millisUntilFinished);
        }

        @Override
        public void onFinish() {
            setCurrentPosition(targetPosition);
            synchronized (InderpolatorView.this) {
                if (timer == this) {
                    timer = null;
                }
            }
        }
    }
}