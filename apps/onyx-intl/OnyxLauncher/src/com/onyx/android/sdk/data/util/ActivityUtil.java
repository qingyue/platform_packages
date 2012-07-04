/**
 * 
 */
package com.onyx.android.sdk.data.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;

/**
 * @author joy
 * 
 */
public class ActivityUtil
{
    public static boolean startActivitySafely(Activity from, Intent intent)
    {
        try {
            from.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
        } catch (SecurityException e) {
        }
        
        return false;
    }

    public static boolean startActivitySafely(Activity from, Intent intent,
            ActivityInfo appInfo)
    {
        @SuppressWarnings("unused")
        CharSequence app_name = appInfo.applicationInfo.loadLabel(from
                .getPackageManager());

        try {
            intent.setPackage(appInfo.packageName);
            intent.setClassName(appInfo.packageName, appInfo.name);

            from.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
        } catch (SecurityException e) {
        }

        return false;
    }
}
