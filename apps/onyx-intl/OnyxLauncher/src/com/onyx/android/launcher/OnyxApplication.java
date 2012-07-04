/**
 * 
 */
package com.onyx.android.launcher;

import android.app.Application;
import android.util.Log;

import com.onyx.android.launcher.data.GridItemManager;

/**
 * @author joy
 *
 */
public class OnyxApplication extends Application
{
    private final static String TAG = "OnyxApplication";
    
    private static OnyxApplication sInstance = null;
    
    public static boolean UpdatePolicyInitialized = false;
    
    /**
     * called from android, only once when startup
     */
    public OnyxApplication()
    {
        Log.d(TAG, "creating OnyxApplication");

        GridItemManager.initialize();
        
        sInstance = this;
    }
    
    public static OnyxApplication getInstance()
    {
        return sInstance;
    }

}
