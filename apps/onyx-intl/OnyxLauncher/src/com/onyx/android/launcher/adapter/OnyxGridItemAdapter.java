package com.onyx.android.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

public class OnyxGridItemAdapter extends GridItemBaseAdapter
{
    private LayoutInflater mInflater = null;
    
    private static final int sItemMinWidth = 145;
    private static final int sItemMinHeight = 140;
    private static final int sHorizontalSpacing = 0;
    private static final int sVerticalSpacing = 0;
    private static final int sItemDetailMinHeight = 70;

    public OnyxGridItemAdapter(Context context, OnyxGridView gridView)
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
        return null;
    }

	@Override
    public long getItemId(int position)
    {
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
            ThumbnailHolder thumbnailHolder = new ThumbnailHolder();

	        ret_view = mInflater.inflate(R.layout.gridview_item_thumbnailview, null);

	        thumbnailHolder.textview_thumbnail_launcher_name = (TextView)ret_view.findViewById(R.id.textview_thumbnail_gridview_item_name);
	        thumbnailHolder.imageview_thumbnail_launcher = (ImageView)ret_view.findViewById(R.id.imageview_thumbnail_gridview_item_cover);

	        ret_view.setTag(item_data);

	        if (item_data.getBitmap() != null) {
	            thumbnailHolder.imageview_thumbnail_launcher.setImageBitmap(item_data.getBitmap());
	        }
	        else {
	            thumbnailHolder.imageview_thumbnail_launcher.setImageResource(item_data.getImageResourceId());
	        }

	        assert(thumbnailHolder.textview_thumbnail_launcher_name != null);
	        thumbnailHolder.textview_thumbnail_launcher_name.setText(item_data.getText());
        }
        else {
            assert(this.getPageLayout().getViewMode() == GridViewMode.Detail);

            DetailHolder detailHolder = new DetailHolder();

            ret_view = mInflater.inflate(R.layout.gridview_item_detailview, null);

            detailHolder.textview_detail_launcher_name = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_name);
            detailHolder.textview_detail_launcher_author = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_author);
            detailHolder.textview_detail_launcher_time = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_time);
            detailHolder.imageview_detail_launcher = (ImageView)ret_view.findViewById(R.id.imageview_detail_gridview_item_cover);

            RelativeLayout.LayoutParams imageview_params = new RelativeLayout.LayoutParams(this.getPageLayout().getItemCurrentHeight(),
                    this.getPageLayout().getItemCurrentHeight());
            detailHolder.imageview_detail_launcher.setLayoutParams(imageview_params);

            ret_view.setTag(item_data);

            if (item_data.getBitmap() != null) {
                detailHolder.imageview_detail_launcher.setImageBitmap(item_data.getBitmap());
            }
            else {
                detailHolder.imageview_detail_launcher.setImageResource(item_data.getImageResourceId());
            }

            if (detailHolder.textview_detail_launcher_name != null) {
                detailHolder.textview_detail_launcher_name.setText(item_data.getText());
            }
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

	public final class ThumbnailHolder{
	    public TextView textview_thumbnail_launcher_name = null;
	    public ImageView imageview_thumbnail_launcher = null;
	}

	public final class DetailHolder{
	    public TextView textview_detail_launcher_name = null;
	    public TextView textview_detail_launcher_author = null;
	    public TextView textview_detail_launcher_time = null;
	    public ImageView imageview_detail_launcher = null;
    }
}