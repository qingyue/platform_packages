/**
 * 
 */
package com.onyx.android.launcher.adapter;

import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onyx.android.launcher.LibraryActivity;
import com.onyx.android.launcher.R;

/**
 * @author dxwts
 *
 */
public class LibraryFirstLetterAdapter extends GridItemBaseAdapter
{
    
    private LayoutInflater mInflater = null;
    private String mLetter = null;
    
    private static final int sItemMinWidth = 40;
    private static final int sItemMinHeight = 20;
    private static final int sItemDetailMinHeight = 0;
    private static final int sHorizontalSpacing = 6;
    private static final int sVerticalSpacing = 6;

    public LibraryFirstLetterAdapter(Context context, OnyxGridView gridView)
    {
        super(gridView);
        mInflater = LayoutInflater.from(context);
        
        this.getPageLayout().setItemMinWidth(sItemMinWidth);
        this.getPageLayout().setItemMinHeight(sItemMinHeight);
        this.getPageLayout().setItemThumbnailMinHeight(sItemMinHeight);
        this.getPageLayout().setItemDetailMinHeight(sItemDetailMinHeight);
        this.getPageLayout().setHorizontalSpacing(sHorizontalSpacing);
        this.getPageLayout().setVerticalSpacing(sVerticalSpacing);
        
    }

    @Override
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public String getLetter()
    {
    return mLetter;    
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;

        ret_view = mInflater.inflate(R.layout.gridview_item_letter, null);

        TextView textview_letter = (TextView) ret_view.findViewById(R.id.item_letter);

        GridItemData item_data = this.getItems().get(position);
        textview_letter.setText(item_data.getText());
        
        if(LibraryActivity.SortPolicy != SortOrder.Name) {
            textview_letter.setTextColor(Color.GRAY);
        }

        return ret_view;
    }

}
