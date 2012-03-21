/**
 * 
 */
package com.onyx.android.sdk.ui.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;
import android.view.Surface;
import android.view.View;

/**
 * singleton class
 * 
 * @author joy
 *
 */
public final class ScreenUpdateManager
{
    private static String TAG = "ScreenUpdateManager";
    
    public enum UpdatePolicy { Automatic, GUIntervally }
    
    public enum UpdateMode 
    {
        None, DW, GU, GC,
    }
    
    private static boolean sInitialized = false;
    private static boolean sEinkMode = false;
    
    private static int sPolicyAutomatic = 0;
    private static int sPolicyGUIntervally = 0;
    
    private static int sModeDW = 0;
    private static int sModeGU = 0;
    private static int sModeGC = 0;
    
    /**
     * static int View.getWindowRotation()
     */
    private static Method sMethodGetWindowRotation = null;
    /**
     * static void View.setWindowRotation(int rotation, boolean alwaysSendConfiguration, int animFlags)
     */
    private static Method sMethodSetWindowRotation = null;
    /**
     * View.setUpdatePolicy(int updatePolicy, int guInterval)
     */
    private static Method sMethodSetUpdatePolicy = null;
    /**
     * View.postInvalidate(int updateMode)
     */
    private static Method sMethodPostInvalidate = null;
    /**
     * View.invalidate(int updateMode)
     */
    private static Method sMethodInvalidate = null;
    
    private ScreenUpdateManager()
    {
    }
    
    public static int getWindowRotation()
    {
        Log.d(TAG, "getWindowRotation");
        if (!sInitialized) {
            init();
        }
        
        Log.d(TAG, "sMethodGetWindowRotation not null: " + (sMethodGetWindowRotation != null));
        if (sMethodGetWindowRotation != null) {
            try {
                return (Integer)sMethodGetWindowRotation.invoke(null);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e);
            } catch (IllegalAccessException e) {
                Log.w(TAG, e);
            } catch (InvocationTargetException e) {
                Log.w(TAG, e);
            }
	    finally {
		Log.d(TAG, "sMethodGetWindowRotation finished");
	    }
        }
        
        return Surface.ROTATION_0;
    }
    
    public static void setWindowRotation(int rotation)
    {
        Log.d(TAG, "setWindowRotation);
        if (!sInitialized) {
            init();
        }
        
        Log.d(TAG, "sMethodSetWindowRotation not null: " + (sMethodSetWindowRotation != null));
        if (sMethodSetWindowRotation != null) {
            try {
                final int Surface_FLAGS_ORIENTATION_ANIMATION_DISABLE = 0x000000001;
                sMethodSetWindowRotation.invoke(null, rotation, true, Surface_FLAGS_ORIENTATION_ANIMATION_DISABLE);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e);
            } catch (IllegalAccessException e) {
                Log.w(TAG, e);
            } catch (InvocationTargetException e) {
                Log.w(TAG, e);
            }
	    finally {
		Log.d(TAG, "sMethodSetWindowRotation");
	    }
        }
    }
    
    public static void setUpdatePolicy(View view, UpdatePolicy policy, int guInterval)
    {
        if (!sInitialized) {
            init();
        }
        
        Log.d(TAG, "setUpdatePolicy in eink mode: " + (sEinkMode ? "true" : "false"));

        if (sEinkMode) {
            int dst_mode_value = getPolicyValue(policy);

            try {
                assert(sMethodPostInvalidate != null);
                Log.d(TAG, "dst mode: " + dst_mode_value);
                sMethodSetUpdatePolicy.invoke(view, dst_mode_value, guInterval);
                return;
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
    }
    
    public static void postInvalidate(View view, UpdateMode mode)
    {
        if (!sInitialized) {
            init();
        }
        
        Log.d(TAG, "postInvalidate in eink mode: " + (sEinkMode ? "true" : "false"));

        if (sEinkMode) {
            int dst_mode_value = getUpdateMode(mode);

            try {
                assert(sMethodPostInvalidate != null);
                Log.d(TAG, "dst mode: " + dst_mode_value);
                sMethodPostInvalidate.invoke(view, dst_mode_value);
                return;
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }

        view.postInvalidate();
    }
    
    public static void invalidate(View view, UpdateMode mode)
    {
        if (!sInitialized) {
            init();
        }
        
        Log.d(TAG, "invalidate in eink mode: " + (sEinkMode ? "true" : "false"));

        if (sEinkMode) {
            int dst_mode_value = getUpdateMode(mode);

            try {
                assert(sMethodInvalidate != null);
                Log.d(TAG, "dst mode: " + dst_mode_value);
                sMethodInvalidate.invoke(view, dst_mode_value);
                return;
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }

        view.invalidate();
    }
    
    private static void init()
    {
        if (sInitialized) {
            // only need initialize once
            assert(false);
            return;
        }
        
        sInitialized = true;
        
        Class<View> cls = View.class;
        
        try {
            // signature of "public static int getWindowRotation()"
            sMethodGetWindowRotation = cls.getMethod("getWindowRotation");
            // signature of "public static void setWindowRotation(int rotation, boolean alwaysSendConfiguration, int animFlags)"
            sMethodSetWindowRotation = cls.getMethod("setWindowRotation", int.class, boolean.class, int.class);
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	Log.d(TAG, "reflecting window ratation api success.");
        
        try {
            Field fld_policy_automic = cls.getField("EINK_ONYX_AUTO_MASK");
            int value_policy_automic = fld_policy_automic.getInt(null);
            Field fld_policy_gu_intervally = cls.getField("EINK_ONYX_GC_MASK");
            int value_policy_gu_intervally = fld_policy_gu_intervally.getInt(null);
            
            Field fld_mode_regional = cls.getField("EINK_AUTO_MODE_REGIONAL");
            int value_mode_regional = fld_mode_regional.getInt(null);
            Field fld_mode_nowait = cls.getField("EINK_WAIT_MODE_NOWAIT");
            int value_mode_nowait = fld_mode_nowait.getInt(null);
            Field fld_mode_wait = cls.getField("EINK_WAIT_MODE_WAIT");
            int value_mode_wait = fld_mode_wait.getInt(null);
            Field fld_mode_waveform_du = cls.getField("EINK_WAVEFORM_MODE_DU");
            int value_mode_waveform_du = fld_mode_waveform_du.getInt(null);
            Field fld_mode_waveform_gc16 = cls.getField("EINK_WAVEFORM_MODE_GC16");
            int value_mode_waveform_gc16 = fld_mode_waveform_gc16.getInt(null);
            Field fld_mode_update_partial = cls.getField("EINK_UPDATE_MODE_PARTIAL");
            int value_mode_update_partial = fld_mode_update_partial.getInt(null);
            Field fld_mode_update_full = cls.getField("EINK_UPDATE_MODE_FULL");
            int value_mode_update_full = fld_mode_update_full.getInt(null);

            sPolicyAutomatic = value_policy_automic;
            sPolicyGUIntervally = value_policy_gu_intervally;
            
            sModeDW = value_mode_regional | value_mode_nowait | value_mode_waveform_du | value_mode_update_partial;
            sModeGU = value_mode_regional | value_mode_nowait | value_mode_waveform_gc16 | value_mode_update_partial;
            sModeGC = value_mode_regional | value_mode_wait | value_mode_waveform_gc16 | value_mode_update_full;
            
            // signature of "public void setUpdatePolicy(int updatePolicy, int guInterval)"
            sMethodSetUpdatePolicy = cls.getMethod("setUpdatePolicy", int.class, int.class);
            // signature of "public void postInvalidate(int updateMode)"
            sMethodPostInvalidate = cls.getMethod("postInvalidate", int.class);
            // signature of "public void invalidate(int updateMode)"
            sMethodInvalidate = cls.getMethod("invalidate", int.class);
            
            Log.d(TAG, "eink mode is true");
            sEinkMode = true;
            
            return;
        } catch (SecurityException e) {
            Log.w(TAG, e);
        } catch (NoSuchFieldException e) {
            Log.w(TAG, e);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e);
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, e);
        }
        
        Log.d(TAG, "eink mode is false");
        sEinkMode = false;
    }

    private static int getUpdateMode(UpdateMode mode)
    {
        // default use GC update mode
        int dst_mode = sModeGC;
        
        switch (mode) {
        case DW:
            dst_mode = sModeDW;
            break;
        case GU:
            dst_mode = sModeGU;
            break;
        case GC:
            dst_mode = sModeGC;
            break;
        default:
            assert(false);
            break;
        }
        
        return dst_mode;
    }
    
    private static int getPolicyValue(UpdatePolicy policy)
    {
        int dst_value = sModeGU;
        switch (policy) {
        case Automatic:
            dst_value |= sPolicyAutomatic; 
            break;
        case GUIntervally:
            dst_value |= sPolicyGUIntervally;
            break;
        default:
            assert(false);
            break;
        }
        
        return dst_value;
    }
}
