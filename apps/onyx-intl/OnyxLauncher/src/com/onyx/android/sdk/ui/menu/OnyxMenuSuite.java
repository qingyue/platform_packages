/**
 * 
 */
package com.onyx.android.sdk.ui.menu;

import java.util.ArrayList;

/**
 * @author joy
 *
 */
public class OnyxMenuSuite
{
    private int mTextResourceId = -1;
    private int mImageResourceId = -1;
    ArrayList<OnyxMenuRow> mRows = new ArrayList<OnyxMenuRow>();
    
    public OnyxMenuSuite(int textResourceId, int imageResourceId)
    {
        mTextResourceId = textResourceId;
        mImageResourceId = imageResourceId;
    }
    
    public int getTextResourceId()
    {
        return mTextResourceId;
    }
    public int getImageResourceId()
    {
        return mImageResourceId;
    }
    public ArrayList<OnyxMenuRow> getMenuRows()
    {
        return mRows;
    }
}
