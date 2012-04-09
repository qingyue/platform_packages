/**
 * 
 */
package com.onyx.android.sdk.data.util;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author joy
 *
 */
public class IntentFilterFactory
{
    private static final IntentFilter SDCARD_UNMOUNTED_FILTER;
    
    private static IntentFilter mIntentFilter = null;
    
    static {
        SDCARD_UNMOUNTED_FILTER = new IntentFilter(); 
        SDCARD_UNMOUNTED_FILTER.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        SDCARD_UNMOUNTED_FILTER.addAction(Intent.ACTION_MEDIA_REMOVED);
        SDCARD_UNMOUNTED_FILTER.addAction(Intent.ACTION_MEDIA_SHARED);
        SDCARD_UNMOUNTED_FILTER.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        SDCARD_UNMOUNTED_FILTER.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        SDCARD_UNMOUNTED_FILTER.addDataScheme("file"); 
    }
    
    public static IntentFilter getSDCardUnmountedFilter()
    {
        return SDCARD_UNMOUNTED_FILTER;
    }
    
    public static IntentFilter getIntentFilterFrontPreferredApplications()
    {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ActionNameFactory.FRONT_PREFERRED_APPLICATIONS);
        
        return mIntentFilter;
    }
    
    public static IntentFilter getIntentFilterCopy()
    {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ActionNameFactory.COPY);
        
        return mIntentFilter;
    }
    
    public static IntentFilter getIntentFilterCut()
    {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ActionNameFactory.CUT);
        
        return mIntentFilter;
    }
    
    public static IntentFilter getIntentFilterMulti()
    {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ActionNameFactory.MULTI);
        
        return mIntentFilter;
    }
    
    public static IntentFilter getIntentFilterDelete()
    {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ActionNameFactory.DELETE);
        
        return mIntentFilter;
    }
}
