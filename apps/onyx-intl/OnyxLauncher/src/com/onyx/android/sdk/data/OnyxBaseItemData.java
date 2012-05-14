/**
 * 
 */
package com.onyx.android.sdk.data;

/**
 * @author joy
 *
 */
public class OnyxBaseItemData
{
    private OnyxItemURI mURI = null;
    private Object mTag = null;
    
    public OnyxBaseItemData(OnyxItemURI uri)
    {
        assert(uri != null);
        mURI = uri;
    }
    
    public OnyxItemURI getURI()
    {
        return mURI;
    }
    
    public Object getTag()
    {
        return mTag;
    }
    public void setTag(Object tag)
    {
        mTag = tag;
    }
    
    public boolean match(String pattern)
    {
        return mURI.getName().toUpperCase().contains(pattern.toUpperCase());
    }
    
    public void setURI(OnyxItemURI uri)
    {
        this.mURI = uri;
    }
}
