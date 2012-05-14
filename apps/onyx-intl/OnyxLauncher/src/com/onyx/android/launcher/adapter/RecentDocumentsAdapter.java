/**
 * 
 */
package com.onyx.android.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

/**
 * @author Administrator
 *
 */
public class RecentDocumentsAdapter extends GridItemBaseAdapter
{
    private LayoutInflater mInflater = null;
    
    private static final int sItemMinWidth = 145;
    private static final int sItemMinHeight = 140;
    private static final int sHorizontalSpacing = 0;
    private static final int sVerticalSpacing = 0;
    private static final int sItemDetailMinHeight = 70;
    
    public RecentDocumentsAdapter(Context context, OnyxGridView gridView)
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

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;

        int idx = this.getPaginator().getItemIndex(position, this.getPaginator().getPageIndex());
        GridItemData item_data = this.getItems().get(idx);

        if (this.getPageLayout().getViewMode() == GridViewMode.Thumbnail) {
            ret_view = mInflater.inflate(R.layout.gridview_item_thumbnailview, null);
            TextView textview_thumbnail_item_name = (TextView)ret_view.findViewById(R.id.textview_thumbnail_gridview_item_name);
            ImageView imageview_thumbnail_item = (ImageView)ret_view.findViewById(R.id.imageview_thumbnail_gridview_item_cover);

            ret_view.setTag(item_data);

            if ((item_data instanceof BookItemData) &&
                    (((BookItemData)item_data).getThumbnail() != null)) {
                BookItemData book = (BookItemData)item_data;
                imageview_thumbnail_item.setImageBitmap(book.getThumbnail());
            }
            else if (item_data.getBitmap() != null) {
                imageview_thumbnail_item.setImageBitmap(item_data.getBitmap());
            }
            else {
                imageview_thumbnail_item.setImageResource(item_data.getImageResourceId());
            }

            assert(textview_thumbnail_item_name != null);
            textview_thumbnail_item_name.setText(item_data.getText());
        }
        else {
            assert(this.getPageLayout().getViewMode() == GridViewMode.Detail);
            ret_view = mInflater.inflate(R.layout.gridview_item_detailview, null);
            TextView textview_detail_item_name = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_name);
            
//            do nothing 
//            TextView textview_detail_item_author = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_author);
//            TextView textview_detail_item_time = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_time);
            ImageView imageview_detail_item = (ImageView)ret_view.findViewById(R.id.imageview_detail_gridview_item_cover);

            RelativeLayout.LayoutParams imageview_params = new RelativeLayout.LayoutParams(this.getPageLayout().getItemCurrentHeight(),
                    this.getPageLayout().getItemCurrentHeight());
            imageview_detail_item.setLayoutParams(imageview_params);

            ret_view.setTag(item_data);

            if ((item_data instanceof BookItemData) &&
                    (((BookItemData)item_data).getThumbnail() != null)) {
                BookItemData book = (BookItemData)item_data;
                imageview_detail_item.setImageBitmap(book.getThumbnail());
            }
            else if (item_data.getBitmap() != null) {
                imageview_detail_item.setImageBitmap(item_data.getBitmap());
            }
            else {
                imageview_detail_item.setImageResource(item_data.getImageResourceId());
            }

            if (textview_detail_item_name != null) {
                textview_detail_item_name.setText(item_data.getText());
            }
        }

        CheckBox cb = (CheckBox) ret_view.findViewById(R.id.checkbox_multi);
        if (item_data instanceof FileItemData) {
            if (this.getMultipleSelectionMode()) {
                cb.setVisibility(View.VISIBLE);
                if (this.getSelectedItems().contains(item_data)) {
                    cb.setChecked(true);
                }
            }
        }
        else if (this.getMultipleSelectionMode()) {
            cb.setVisibility(View.VISIBLE);
            cb.setEnabled(false);
            cb.setButtonDrawable(R.drawable.check_box_shield);
        }

        // warning!
        // repeatedly calling setLayoutParams() will cause a strange bug that makes TextView's content disappearing, 
        // having no clue about it
        if (ret_view.getLayoutParams() == null) {
            OnyxGridView.LayoutParams params = new OnyxGridView.LayoutParams(this.getPageLayout().getItemCurrentWidth(),
                    this.getPageLayout().getItemCurrentHeight());
            ret_view.setLayoutParams(params);  
        }
        else {
            ret_view.getLayoutParams().width = this.getPageLayout().getItemCurrentWidth();
            ret_view.getLayoutParams().height = this.getPageLayout().getItemCurrentHeight();
        }

        return ret_view;
    }
}
