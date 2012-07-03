/**
 * 
 */
package com.onyx.android.sdk.data.util;

import android.app.Application;

/**
 * @author joy
 *
 */
public class OnyxApplication extends Application
{
    private static OnyxApplication sInstance = null;
    
    public static boolean UpdatePolicyInitialized = false;
    
    /**
     * called from android, only once when startup
     */
    public OnyxApplication()
    {
        sInstance = this;
    }
    
    public static OnyxApplication getInstance()
    {
        return sInstance;
    }

}
