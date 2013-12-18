package ly.mens.layoutinderpolator;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mensly on 18/12/2013.
 */
public class Page {
    public final View container;
    private SparseArray<ViewInfo> viewInfo = new SparseArray<>();

    public Page(View container) {
        this.container = container;

        if (container instanceof ViewGroup) {
            // Register information about page's children
            final ViewGroup viewGroup = (ViewGroup)container;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                addView(viewGroup.getChildAt(i));
            }
        }
        else {
            // Page is a single animated view
            addView(container);
        }
    }

    public void addView(View subview) {
        ViewInfo info = new ViewInfo(subview);
        if (info.id == 0) {
            throw new IllegalArgumentException("Child views to be animated must have an id");
        }
        else {
            viewInfo.put(info.id, info);
        }
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
}
