package com.onyx.android.launcher.adapter;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.sys.OnyxAppPreference;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

public class PreferredApplicationsAdapter extends GridItemBaseAdapter
{
    private String[] mFileFormats = null;
    private Context mContext = null;

    private static final int sItemMinHeight = 60;

    public PreferredApplicationsAdapter(Context context, OnyxGridView gridView, String[] arrars)
    {
        super(gridView);

        mFileFormats = arrars;
        mContext = context;
        
        this.getPageLayout().setItemMinHeight(sItemMinHeight);
        this.getPageLayout().setItemThumbnailMinHeight(sItemMinHeight);
        this.getPageLayout().setItemDetailMinHeight(sItemMinHeight);
        this.getPageLayout().setViewMode(GridViewMode.Detail);

        this.getPaginator().initializePageData(mFileFormats.length,
                this.getPaginator().getPageSize());
        this.notifyDataSetChanged();
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;

        int idx = this.getPaginator().getAbsoluteIndex(position);

        if (convertView != null) {
            ret_view = convertView;
        }
        else {
            ret_view = View.inflate(mContext, R.layout.activity_preferred_applications_item, null);
        }
        
        String ext = mFileFormats[idx];

        TextView text_view = (TextView)ret_view.findViewById(R.id.textview_file_extension);
        text_view.setText(ext);
        
        TextView text_view_app = (TextView)ret_view.findViewById(R.id.textivew_default_application);
        File fake_file = new File("mnt/sdcard/dummy." + ext);
        OnyxAppPreference p = OnyxAppPreferenceCenter.getApplicationPreference(fake_file);
        if (p != null) {
            text_view_app.setText(p.getAppName());
        }
        else {
            text_view_app.setText("");
        }

        final int height = Math.max(this.getPageLayout().getItemCurrentHeight(),
                this.getPageLayout().getItemMinHeight());
        text_view.getLayoutParams().height = height;
        
        ret_view.setTag(text_view.getText().toString());
        
        return ret_view;
    }
}
