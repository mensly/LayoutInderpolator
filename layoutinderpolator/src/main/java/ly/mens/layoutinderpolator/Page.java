package ly.mens.layoutinderpolator;

import android.graphics.Color;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mensly on 18/12/2013.
 */
public class Page {
    public final View container;
    private SparseArray<ViewInfo> viewInfo = new SparseArray<>();
    private ViewInfoFactory<?> factory;

    public Page(View container) {
        this.container = container;

        if (container instanceof ViewGroup) {
            // Register information about page's children
            final ViewGroup viewGroup = (ViewGroup)container;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                addView(viewGroup.getChildAt(i));
            }
            // Containers must have clear background
            container.setBackgroundColor(Color.TRANSPARENT);
        }
        else {
            // Page is a single animated view
            addView(container);
        }
    }

    public Page setViewInfoFactory(ViewInfoFactory<?> factory) {
        this.factory = factory;
        return this;
    }

    public void addView(View subview) {
        final ViewInfo info;
        if (factory == null) {
            info = new ViewInfo(subview);
        }
        else {
            info = factory.createInfo(subview);
        }
        if (info.id == 0) {
            throw new IllegalArgumentException("Child views to be animated must have an id");
        }
        else {
            viewInfo.put(info.id, info);
        }
        subview.setPivotX(0);
        subview.setPivotY(0);
        subview.setVisibility(View.VISIBLE);
    }

    /**
     * Gets all the ids of the views to be animated
     */
    public Set<Integer> getIds() {
        final int size = viewInfo.size();
        Set<Integer> ids = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(viewInfo.keyAt(i));
        }
        return ids;
    }

    /**
     * Gets the intersection of another page's set of ids with this one
     * ie every id that is contained on this page, and on the other
     */
    public Set<Integer> getIntersection(Page otherPage) {
        if (otherPage == null) {
            return Collections.emptySet();
        }
        final int size = viewInfo.size();
        Set<Integer> ids = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int id = viewInfo.keyAt(i);
            if (otherPage.viewInfo.get(id) != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * Gets the compliment of another page's set of ids relative to this one
     * ie every id that is contained on this page, but not the other
     */
    public Set<Integer> getCompliment(Page otherPage) {
        if (otherPage == null) {
            return getIds();
        }
        final int size = viewInfo.size();
        Set<Integer> ids = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int id = viewInfo.keyAt(i);
            if (otherPage.viewInfo.get(id) == null) {
                ids.add(id);
            }
        }
        return ids;
    }

    public List<View> getViews(Collection<Integer> ids) {
        List<View> views = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            views.add(container.findViewById(id));
        }
        return views;
    }

    public ViewInfo getInfo(int id) {
        return viewInfo.get(id);
    }

    public void reset() {
        final int size = viewInfo.size();
        for (int i = 0; i < size; i++) {
            int key = viewInfo.keyAt(i);
            viewInfo.get(key).reset(container.findViewById(key));
        }
    }
}
