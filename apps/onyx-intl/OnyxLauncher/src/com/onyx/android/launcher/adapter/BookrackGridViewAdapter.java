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
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

public class BookrackGridViewAdapter extends GridItemBaseAdapter
{
    private LayoutInflater mInflater = null;

    private static final int sItemMinWidth = 165;
    private static final int sItemMinHeight = 170;
    private static final int sItemDetailMinHeight = 70;
    private static final int sHorizontalSpacing = 0;
    private static final int sVerticalSpacing = 10;

    public BookrackGridViewAdapter(Context context, OnyxGridView gridView)
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
    public int getCount()
    {
        if (this.getPageLayout().getViewMode() == GridViewMode.Thumbnail) {
            // in order to keep book shelf style, we always show full shelf items
            return this.getPaginator().getPageSize();
        }
        else {
            return this.getPaginator().getItemCountInCurrentPage();
        }
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;

        if (this.getPageLayout().getViewMode() == GridViewMode.Thumbnail) {
            ret_view = mInflater.inflate(R.layout.gridview_bookrack_item, null);

            @SuppressWarnings("unused")
            TextView textview_author = (TextView)ret_view.findViewById(R.id.textview_author);

            RelativeLayout layout = (RelativeLayout) ret_view.findViewById(R.id.book_cover_layout);
            ImageView imageview_cover = (ImageView)layout.findViewById(R.id.imageview_book_cover);
            TextView textview_bookrack = (TextView)layout.findViewById(R.id.textview_bookrack_background);
            TextView textview_book_name = (TextView)ret_view.findViewById(R.id.textview_book_name);

            int background_img_id = this.getBookrackBackground(position);
            textview_bookrack.setBackgroundResource(background_img_id);
            textview_bookrack.getBackground().setAlpha(130);

            int idx = this.getPaginator().getItemIndex(position, this.getPaginator().getPageIndex());
            if (idx <= (this.getPaginator().getItemCount() - 1)) {
                GridItemData item_data = this.getItems().get(idx);
                textview_book_name.setText(item_data.getText());
                if ((item_data instanceof BookItemData) &&
                        (((BookItemData)item_data).getThumbnail() != null)) {
                    BookItemData book = (BookItemData)item_data;
                    imageview_cover.setImageBitmap(book.getThumbnail());
                }
                else {
                    imageview_cover.setImageResource(R.drawable.cover);
                }

                BookrackGridViewAdapter.this.setShowMultipleCheckbox(ret_view, item_data);

                ret_view.setTag(item_data);
            }
            else {
                // the position may have nothing, so put an item standing for empty
                textview_book_name.setText("");
                imageview_cover.setImageResource(R.drawable.nothingness);

                ret_view.setTag(null);
            }
        }
        else {
            ret_view = mInflater.inflate(R.layout.gridview_bookrack_item_detailview, null);

            @SuppressWarnings("unused")
            TextView textview_author = (TextView)ret_view.findViewById(R.id.textview_library_detail_gridview_item_author);

            TextView textview_book_name = (TextView)ret_view.findViewById(R.id.textview_library_detail_gridview_item_name);
            ImageView imageview_cover = (ImageView)ret_view.findViewById(R.id.imageview_library_detail_gridview_item_cover);

            int idx = this.getPaginator().getItemIndex(position, this.getPaginator().getPageIndex());
            GridItemData item_data = this.getItems().get(idx);
            textview_book_name.setText(item_data.getText());
            
            if ((item_data instanceof BookItemData) &&
                    (((BookItemData)item_data).getThumbnail() != null)) {
                BookItemData book = (BookItemData)item_data;
                imageview_cover.setImageBitmap(book.getThumbnail());
            }
            else {
                imageview_cover.setImageResource(R.drawable.cover);
            }

            BookrackGridViewAdapter.this.setShowMultipleCheckbox(ret_view, item_data);

            ret_view.setTag(item_data);

            RelativeLayout.LayoutParams imageview_params = new RelativeLayout.LayoutParams(this.getPageLayout().getItemCurrentHeight(),
                    this.getPageLayout().getItemCurrentHeight());
            imageview_cover.setLayoutParams(imageview_params);
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

    private int getBookrackBackground(int position)
    {
        int mod = position % this.getPageLayout().getLayoutColumnCount();
        if (mod == 0) {
            return R.drawable.bookrack_left;
        }
        else if (mod == (this.getPageLayout().getLayoutColumnCount() - 1)) {
            return R.drawable.bookrack_right;
        }
        else {
            return R.drawable.bookrack_middle;
        }
    }

    private void setShowMultipleCheckbox(View view, GridItemData item_data)
    {
        CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_multi);
        if (this.getMultipleSelectionMode()) {
            cb.setVisibility(View.VISIBLE);
            if (this.getSelectedItems().contains(item_data)) {
                cb.setChecked(true);
            }
        }
    }
}
