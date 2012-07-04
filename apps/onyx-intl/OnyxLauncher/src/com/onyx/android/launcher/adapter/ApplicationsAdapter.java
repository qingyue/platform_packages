/**
 * 
 */
package com.onyx.android.launcher.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;

/**
 * @author joy
 *
 */
public class ApplicationsAdapter extends OnyxPagedAdapter
{
    public static class AppItem {
        private PackageManager mPackageManager = null;
        private ResolveInfo mResolveInfo = null;
        private String mTitle = null;
        private Drawable mIcon = null;
        private Intent mIntent = null;
        
        public AppItem(PackageManager packageManager, ResolveInfo info)
        {
            mPackageManager = packageManager;
            mResolveInfo = info;
            
            mTitle = info.activityInfo.applicationInfo.loadLabel(packageManager).toString();
        }
        
        public String getTitle()
        {
            return mTitle;
        }
        
        public Drawable getIcon()
        {
            if (mIcon == null) {
                mIcon = mResolveInfo.loadIcon(mPackageManager);
            }
            
            return mIcon;
        }
        
        public Intent getIntent()
        {
            if (mIntent == null) {
                ComponentName component_name = new ComponentName(mResolveInfo.activityInfo.applicationInfo.packageName,
                        mResolveInfo.activityInfo.name);
                mIntent = new Intent(Intent.ACTION_MAIN);
                mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mIntent.setComponent(component_name);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            }
            
            return mIntent;
        }
        
    }
    
    private final static Comparator<AppItem> sComparator = new Comparator<AppItem>() {

        @Override
        public int compare(AppItem object1, AppItem object2)
        {
            return object1.getTitle().compareToIgnoreCase(object2.getTitle());
        }
        
    };
            
    private static final int sItemMinWidth = 128;
    private static final int sItemMinHeight = 128;
    private static final int sHorizontalSpacing = 0;
    private static final int sVerticalSpacing = 0;
    private static final int sItemDetailMinHeight = 70;
    
    private LayoutInflater mInflater = null;    
    ArrayList<AppItem> mApps = new ArrayList<AppItem>();

    public ApplicationsAdapter(Context context, OnyxGridView gridView)
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
    
    public void setAppItems(ArrayList<AppItem> apps)
    {
        mApps = apps;
        Collections.sort(mApps, sComparator);
        
        this.getPaginator().setItemCount(mApps.size());
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
        
        AppItem app = mApps.get(this.getPaginator().getAbsoluteIndex(position));
        
        View ret_view = convertView;
        if (ret_view == null) {
            ret_view = mInflater.inflate(R.layout.gridview_item_thumbnailview, null);
        } 
        TextView textview = (TextView)ret_view.findViewById(R.id.textview_thumbnail_gridview_item_name);
        ImageView imageview = (ImageView)ret_view.findViewById(R.id.imageview_thumbnail_gridview_item_cover);
        
        textview.setText(app.getTitle());
        imageview.setImageDrawable(app.getIcon());
        
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
        
        ret_view.setTag(app);
        return ret_view;
    }

}
