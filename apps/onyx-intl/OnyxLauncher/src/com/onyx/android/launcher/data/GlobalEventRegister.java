/**
 * 
 */
package com.onyx.android.launcher.data;

import android.util.Log;

import com.onyx.android.sdk.data.OnyxItemURI;

/**
 * @author joy
 *
 */
public class GlobalEventRegister
{
    private static final String TAG = "GlobalEventRegister";
    
    public static interface OnSearchFinishedListener
    {
        public void onSearchFinished(OnyxItemURI uri);
    }
    
    // initialize to avoid null checking
    private static OnSearchFinishedListener ourOnSearchFinishedListener = new OnSearchFinishedListener()
    {
        
        @Override
        public void onSearchFinished(OnyxItemURI uri)
        {
            // TODO Auto-generated method stub
            
        }
    };
    
    public static void notifySearchFinishedEvent(OnyxItemURI uri)
    {
        Log.d(TAG, "notify Search Finished Event");
        
        ourOnSearchFinishedListener.onSearchFinished(uri);
    }
    public static void setOnSearchFinishedListener(OnSearchFinishedListener l) 
    {
        Log.d(TAG, "register Search Finished Event");
        
        ourOnSearchFinishedListener = l;
    }
}
