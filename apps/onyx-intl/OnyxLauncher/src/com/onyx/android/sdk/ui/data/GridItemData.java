/**
 * 
 */
package com.onyx.android.sdk.ui.data;

import android.graphics.Bitmap;
import com.onyx.android.sdk.data.OnyxBaseItemData;
import com.onyx.android.sdk.data.OnyxItemURI;

/**
 * Image storing either in ImageResourceId or Bitmap, 
 * can be known by checking whether getBitmap() == null
 * 
 * @author joy
 *
 */
public class GridItemData extends OnyxBaseItemData {
	private String mText = null;
	private int mTextId = -1;
	private int mImageResourceId = -1;
	private Bitmap mBitmap = null;
	
	public GridItemData(OnyxItemURI uri, String text, int imageResourceId)
	{
	    super(uri);
	    
		mText = text;
		mImageResourceId = imageResourceId;
	}
	
	public GridItemData(OnyxItemURI uri, String text, Bitmap bitmap)
	{
	    super(uri);
	    
	    mText = text;
	    
	    mBitmap = bitmap;
	}
	
	public GridItemData(OnyxItemURI uri, int textId, Bitmap bitmap)
	{
	    super(uri);
	    
	    mTextId = textId;
	    
	    mBitmap = bitmap;
	}
	
	public GridItemData(OnyxItemURI uri, int textId, int imageResourceId)
	{
	    super(uri);

	    mTextId = textId;
	    
       mImageResourceId = imageResourceId;
	}
	
	public String getText()
	{
		return mText;
	}
	public int getTextId(){
		return mTextId;
	}
    public void setText(String text)
    {
        this.mText = text;
    }
    public int getImageResourceId()
	{
	    return mImageResourceId;
	}
	public Bitmap getBitmap()
	{
	    return mBitmap;
	}
	public void setBitmap(Bitmap bitmap)
	{
	    this.mBitmap = bitmap;
	}
}
