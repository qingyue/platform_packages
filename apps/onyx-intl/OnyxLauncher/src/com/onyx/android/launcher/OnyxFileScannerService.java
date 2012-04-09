/**
 * 
 */
package com.onyx.android.launcher;

import com.onyx.android.launcher.data.OnyxFileScanner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

/**
 * @author joy
 *
 */
public class OnyxFileScannerService extends Service implements Runnable
{
    private static final String TAG = "OnyxFileScannerService";
    
    private final class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            try {
                mWakeLock.acquire();
                
                Bundle args = (Bundle)msg.obj;
                String[] dir_list = args.getStringArray("dirlist"); 
            
                if (dir_list == null) {
                    return;
                }
                
                OnyxFileScanner scanner = new OnyxFileScanner(OnyxFileScannerService.this);
                scanner.scanDirectories(dir_list);
            }
            catch (Exception e) {
                Log.e(TAG, "exception in OnyxFileScanner.scanDirectories: " + e.getMessage());
            }
            finally {
                mWakeLock.release();
                stopSelf();
            }
        }
    }
    
    private PowerManager.WakeLock mWakeLock; 
    private boolean mInitialized = false;
    private Looper mLooper = null; 
    private ServiceHandler mHandler = null;
    
    @Override
    public void onCreate()
    {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        
        Thread th = new Thread(null, this, "OnyxFileScannerService");
        th.start();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        while (!mInitialized) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        if (intent == null) {
            return Service.START_NOT_STICKY;
        }

        Message msg = mHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent.getExtras();
        mHandler.sendMessage(msg);

        // Try again later if we are killed before we can finish scanning.
        return Service.START_REDELIVER_INTENT;
    }
    
    @Override
    public void onDestroy()
    {
        while (!mInitialized) {
            try {
                Thread.sleep(100);
            } 
            catch (InterruptedException e) {
            }
        }
        
        mLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void run()
    {
        // reduce priority below other background threads to avoid interfering
        // with other services at boot time.
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND +
                android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
        Looper.prepare();

        mLooper = Looper.myLooper();
        mHandler = new ServiceHandler();

        mInitialized = true;
        
        Looper.loop();
    }

}
