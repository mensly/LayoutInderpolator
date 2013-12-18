package ly.mens.layoutinderpolator;

import android.view.View;

/**
 * Allows creating view info objects that handle views in different ways such as interpolating
 * other parameters
 * Created by mensly on 19/12/2013.
 */
public interface ViewInfoFactory<T extends ViewInfo> {
    public T createInfo(View view);
}
