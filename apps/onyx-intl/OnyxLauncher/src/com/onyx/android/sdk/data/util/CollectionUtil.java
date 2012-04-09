/**
 * 
 */
package com.onyx.android.sdk.data.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author joy
 *
 */
public class CollectionUtil
{
    public static <T> ArrayList<T> listToArrayList(List<T> list)
    {
        ArrayList<T> a = new ArrayList<T>();
        a.addAll(list);
        return a;
    }
}
