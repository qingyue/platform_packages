/**
 * 
 */
package com.onyx.android.sdk.data.util;

import android.content.Intent;

/**
 * @author qingyue
 *
 */
public class IntentFactory
{
    private static Intent mIntent = new Intent();
    
    public static Intent getIntentFrontPreferredApplications()
    {
        mIntent.setAction(ActionNameFactory.FRONT_PREFERRED_APPLICATIONS);
        return mIntent;
    }
    
    public static Intent getIntentCopy()
    {
        mIntent.setAction(ActionNameFactory.COPY);
        return mIntent;
    }
    
    public static Intent getIntentCut()
    {
        mIntent.setAction(ActionNameFactory.CUT);
        return mIntent;
    }
    
    public static Intent getIntentMulti()
    {
        mIntent.setAction(ActionNameFactory.MULTI);
        return mIntent;
    }
    
    public static Intent getIntentDelete()
    {
        mIntent.setAction(ActionNameFactory.DELETE);
        return mIntent;
    }
}
