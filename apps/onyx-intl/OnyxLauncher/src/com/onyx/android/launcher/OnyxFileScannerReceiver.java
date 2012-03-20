/**
 * 
 */
package com.onyx.android.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.IntentFilterFactory;

/**
 * @author joy
 *
 */
public class OnyxFileScannerReceiver extends BroadcastReceiver
{
    private static final String TAG = "OnyxFileScannerReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive: " + intent.getAction() + ", " + intent.getDataString());
        
        String action = intent.getAction();
        
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            this.updateDirectory(context, FileUtil.getFilePathFromUri(intent.getDataString()));
        }
        else { 
            boolean sdcard_unmounted = false;
            IntentFilter filter = IntentFilterFactory.getSDCardUnmountedFilter();             
            int count_actions = filter.countActions();
            for (int i = 0; i < count_actions; i++) {
                if (intent.getAction().equals(filter.getAction(i))) {
                    sdcard_unmounted = true;
                    break;
                }
            }
            
            if (sdcard_unmounted) {
                // TODO: implicitly depending on scanner will delete obsolete files belonging to unmounted SD card
                this.updateDirectory(context, FileUtil.getFilePathFromUri(intent.getDataString()));
            }
        }
    }
    
    private void updateDirectory(Context context, String dir)
    {
        Bundle args = new Bundle();
        args.putStringArray("dirlist", new String[] { dir });

        Intent dst_intent = new Intent(context, OnyxFileScannerService.class);
        dst_intent.putExtras(args);

        context.startService(dst_intent);
    }
    
}
