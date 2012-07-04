package com.onyx.android.launcher.adapter;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShortCutsAdapter extends GridItemBaseAdapter {
	 private LayoutInflater mInflater = null;
	    
	 private static final int sItemMinWidth = 125;
	 private static final int sItemMinHeight = 130;
	 private static final int sItemDetailMinHeight = 70;
	 private static final int sHorizontalSpacing = 0;
	 private static final int sVerticalSpacing = 10;

	public ShortCutsAdapter(Context context, OnyxGridView gridView) {
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

	        thumbnailHolder.mTextViewThumbnailLauncherName = (TextView)ret_view.findViewById(R.id.textview_thumbnail_gridview_item_name);
	        thumbnailHolder.mImageViewThumbnailLauncher = (ImageView)ret_view.findViewById(R.id.imageview_thumbnail_gridview_item_cover);

	        ret_view.setTag(item_data);

	        if (item_data.getBitmap() != null) {
	            thumbnailHolder.mImageViewThumbnailLauncher.setImageBitmap(item_data.getBitmap());
	        }
	        else {
	            thumbnailHolder.mImageViewThumbnailLauncher.setImageResource(item_data.getImageResourceId());
	        }

	        assert(thumbnailHolder.mTextViewThumbnailLauncherName != null);
	        thumbnailHolder.mTextViewThumbnailLauncherName.setText(item_data.getTextId());
        }
        else {
            assert(this.getPageLayout().getViewMode() == GridViewMode.Detail);

            DetailHolder detailHolder = new DetailHolder();

            ret_view = mInflater.inflate(R.layout.gridview_item_detailview, null);

            detailHolder.mTextViewDetailLauncherName = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_name);
            detailHolder.mTextViewDetailLauncherAuthor = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_author);
            detailHolder.mTextViewDetailLauncherTime = (TextView)ret_view.findViewById(R.id.textview_detail_gridview_item_time);
            detailHolder.mImageViewDetailLauncher = (ImageView)ret_view.findViewById(R.id.imageview_detail_gridview_item_cover);

            RelativeLayout.LayoutParams imageview_params = new RelativeLayout.LayoutParams(this.getPageLayout().getItemCurrentHeight(),
                    this.getPageLayout().getItemCurrentHeight());
            detailHolder.mImageViewDetailLauncher.setLayoutParams(imageview_params);

            ret_view.setTag(item_data);

            if (item_data.getBitmap() != null) {
                detailHolder.mImageViewDetailLauncher.setImageBitmap(item_data.getBitmap());
            }
            else {
                detailHolder.mImageViewDetailLauncher.setImageResource(item_data.getImageResourceId());
            }

            if (detailHolder.mTextViewDetailLauncherName != null) {
                detailHolder.mTextViewDetailLauncherName.setText(item_data.getText());
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
	    public TextView mTextViewThumbnailLauncherName = null;
	    public ImageView mImageViewThumbnailLauncher = null;
	}

	public final class DetailHolder{
	    public TextView mTextViewDetailLauncherName = null;
	    public TextView mTextViewDetailLauncherAuthor = null;
	    public TextView mTextViewDetailLauncherTime = null;
	    public ImageView mImageViewDetailLauncher = null;
    }
}
