/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.sys.OnyxAppPreference;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;

/**
 * @author joy
 *
 */
public class DialogFileProperty extends OnyxDialogBase
{
	private OnyxBaseActivity mActivity = null;
	private File mFile = null;
	private TextView mTextViewOpenWith = null;
	private TextView mTextViewLabelOpenWith = null;

    public DialogFileProperty(Context context, FileItemData fileItem)
    {
        super(context);

        mActivity = (OnyxBaseActivity)context;
        mFile = GridItemManager.getFileFromURI(fileItem.getURI());

        this.setContentView(R.layout.dialog_file_property);

        TextView tv_name = (TextView)this.findViewById(R.id.textview_name);
        TextView tv_title = (TextView)this.findViewById(R.id.textview_title);
        TextView tv_authors = (TextView)this.findViewById(R.id.textview_authors);
        TextView tv_size = (TextView)this.findViewById(R.id.textview_size);
        TextView tv_path = (TextView)this.findViewById(R.id.textview_path);
        TextView tv_description = (TextView)this.findViewById(R.id.textview_description);
        mTextViewLabelOpenWith = (TextView)this.findViewById(R.id.textview_label_open_with);
        mTextViewOpenWith= (TextView)this.findViewById(R.id.textview_open_with);

        OnyxAppPreference p = OnyxAppPreferenceCenter.getApplicationPreference(mFile);
        if (p != null) {
            mTextViewOpenWith.setText(p.getAppName());
        }
        else {
        	mTextViewOpenWith.setText(R.string.default_application);
        }

        Button button_change_open_with = (Button)this.findViewById(R.id.button_change_open_with);
        button_change_open_with.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogFileProperty.this.dismiss();

				final Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);

                String ext = FileUtil.getFileExtension(mFile.getName());

                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                File file = new File("mnt/sdcard/dummy."+ ext);
                if (type != null) {
                    intent.setDataAndType(Uri.fromFile(file), type);
                }
                else {
                    intent.setData(Uri.fromFile(file));
                }

                List<ResolveInfo> info_list = mActivity.getPackageManager().queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);

                if (info_list.size() <= 0) {
                    Toast.makeText(mActivity, R.string.unable_open_file, Toast.LENGTH_SHORT).show();
                }
                else {
                    DialogPreferredApplications dlg = new DialogPreferredApplications(mActivity, info_list, ext, mTextViewOpenWith.getText().toString());
                    dlg.show();
                }
			}
		});

        Button button_cancel = (Button)this.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogFileProperty.this.dismiss();
			}
		});

        if (fileItem instanceof BookItemData) {
            BookItemData book = (BookItemData)fileItem;
            
            if (book.getMetadata() != null) {
            	tv_name.setText(book.getMetadata().getName());
                tv_title.setText(book.getMetadata().getTitle());
                tv_authors.setText(book.getMetadata().getAuthors() == null ? "" : 
                    OnyxMetadata.convertAuthorsToString(book.getMetadata().getAuthors()));
                tv_size.setText(String.valueOf(book.getMetadata().getSize()));
                tv_path.setText(GridItemManager.getFileFromURI(book.getURI()).getPath());
                tv_description.setText(book.getMetadata().getDescription());
            }
        }
        else {
        	if (GridItemManager.getFileFromURI(fileItem.getURI()).isDirectory()) {
				mTextViewOpenWith.setVisibility(View.GONE);
				mTextViewLabelOpenWith.setVisibility(View.GONE);
				button_change_open_with.setVisibility(View.GONE);
			}
            tv_name.setText(fileItem.getURI().getName());
            tv_size.setText(SDFileFactory.getDirectorySize(mFile) + "");
            tv_path.setText(GridItemManager.getFileFromURI(fileItem.getURI()).getPath());
		}
    }
}
