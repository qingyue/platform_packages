/**
 * 
 */
package com.onyx.android.launcher.util;

import android.content.Context;
import android.widget.Toast;

/**
 * @author joy
 *
 */
public class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler
{
    private final Context mContext;

    public UncaughtExceptionHandler(Context context) {
        mContext = context;
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
