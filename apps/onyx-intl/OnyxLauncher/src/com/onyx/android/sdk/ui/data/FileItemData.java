/**
 * 
 */
package com.onyx.android.sdk.ui.data;

import android.graphics.Bitmap;

import com.onyx.android.sdk.data.OnyxItemURI;

/**
 * @author joy
 *
 */
public class FileItemData extends GridItemData
{
    public enum FileType { Directory, NormalFile, }
    
    private FileType mFileType = FileType.NormalFile;

    public FileItemData(OnyxItemURI uri, FileType fileType, String text, Bitmap bitmap)
    {
        super(uri, text, bitmap);
        
        mFileType = fileType;
    }

    public FileItemData(OnyxItemURI uri, FileType fileType, String text, int imageResourceId)
    {
        super(uri, text, imageResourceId);
        
        mFileType = fileType;
    }
    
    public FileType getFileType()
    {
        return mFileType;
    }
    
    public boolean isDirectory()
    {
        return mFileType == FileType.Directory;
    }

}
