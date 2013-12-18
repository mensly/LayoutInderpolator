package ly.mens.layoutinderpolator;

import android.view.View;

/**
 * Allows creating pages in different ways such as assigning a ViewInfoFactory
 * Created by mensly on 19/12/2013.
 */
public interface PageFactory<T extends Page> {
    public T createPage(View container);
}
