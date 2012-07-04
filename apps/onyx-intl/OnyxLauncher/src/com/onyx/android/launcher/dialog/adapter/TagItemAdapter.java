/**
 * 
 */
package com.onyx.android.launcher.dialog.adapter;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * @author dxwts
 * 
 */
public class TagItemAdapter extends GridItemBaseAdapter
{

    private LayoutInflater mInflater = null;
    private String mLetter = null;

    private static final int sItemMinWidth = 80;
    private static final int sItemMinHeight = 40;
    private static final int sItemDetailMinHeight = 0;
    private static final int sHorizontalSpacing = 6;
    private static final int sVerticalSpacing = 0;

    public TagItemAdapter(Context context, OnyxGridView gridView)
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

        ret_view = mInflater.inflate(R.layout.tag_text_item, null);

        Button tag_button = (Button) ret_view.findViewById(R.id.tag_item);

        GridItemData item_data = this.getItems().get(position);
        tag_button.setText(item_data.getText());

        return ret_view;
    }

}
