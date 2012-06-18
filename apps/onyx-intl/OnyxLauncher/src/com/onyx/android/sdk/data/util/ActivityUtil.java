/**
 * 
 */
package com.onyx.android.sdk.data.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

import com.onyx.android.launcher.R;

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

        Toast.makeText(from, R.string.open_item_failed, Toast.LENGTH_SHORT)
                .show();
        return false;
    }

    public static boolean startActivitySafely(Activity from, Intent intent,
            ActivityInfo appInfo)
    {
        CharSequence app_name = appInfo.applicationInfo.loadLabel(from
                .getPackageManager());

        try {
            intent.setPackage(appInfo.packageName);
            intent.setClassName(appInfo.packageName, appInfo.name);

            from.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    from,
                    app_name
                            + OnyxApplication.getInstance().getResources()
                                    .getString(R.string.not_found),
                    Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(
                    from,
                    app_name
                            + OnyxApplication.getInstance().getResources()
                                    .getString(R.string.not_allowed),
                    Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}
