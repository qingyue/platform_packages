/**
 * 
 */
package com.onyx.android.sdk.ui.menu;


/**
 * @author joy
 *
 */
public class OnyxMenuItem
{
    public interface OnMenuItemClickListener
    {
        void onClick();
    }
    
    private OnMenuItemClickListener mOnMenuItemClickListener = new OnMenuItemClickListener()
    {
        
        @Override
        public void onClick()
        {
            // do nothing
        }
    };
    public void notifyClick()
    {
        mOnMenuItemClickListener.onClick();
    }
    public void setOnMenuItemClickListener(OnMenuItemClickListener l)
    {
        mOnMenuItemClickListener = l;
    }
    
    private int mTextResourceId = -1;
    private int mImageResourceId = -1;
    private boolean mEnabled = false;
    
    public OnyxMenuItem(int textResourceId, int imageResourceId, boolean enabled)
    {
        mTextResourceId = textResourceId;
        mImageResourceId = imageResourceId;
        mEnabled = enabled;
    }
    
    public int getTextResourceId()
    {
        return mTextResourceId;
    }
    public int getImageResourceId()
    {
        return mImageResourceId;
    }
    public boolean getEnabled()
    {
        return mEnabled;
    }
}
