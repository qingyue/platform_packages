package com.onyx.android.sdk.ui.data;

import android.graphics.Bitmap;

import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.util.NotImplementedException;

public class BookItemData extends FileItemData
{
    private OnyxMetadata mMetadata = null;
    private Bitmap mThumbnail = null;

    public BookItemData(OnyxItemURI uri, FileType fileType, String text, int imageResourceId)
    {
        super(uri, fileType, text, imageResourceId);
    }

    public BookItemData(OnyxItemURI uri, FileType fileType, String text, Bitmap bitmap)
    {
        super(uri, fileType, text, bitmap);
    }
    
    public static BookItemData create(OnyxMetadata data)
    {
	    throw new NotImplementedException();
    }
    
    public OnyxMetadata getMetadata()
    {
        return mMetadata;
    }
    public void setMetadata(OnyxMetadata data)
    {
        mMetadata = data;
    }
    
    public Bitmap getThumbnail()
    {
        return mThumbnail;
    }
    public void setThumbnail(Bitmap thumbnail)
    {
        mThumbnail = thumbnail;
    }

}
