/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.content.Context;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;

/**
 * @author joy
 *
 */
public class DialogFileProperty extends OnyxDialogBase
{

    public DialogFileProperty(Context context, FileItemData fileItem)
    {
        super(context);
        
        this.setContentView(R.layout.dialog_file_property);
        
        TextView tv_name = (TextView)this.findViewById(R.id.textview_name);
        tv_name.setText(fileItem.getURI().getName());
        
        if (fileItem instanceof BookItemData) {
            BookItemData book = (BookItemData)fileItem;
            
            if (book.getMetadata() != null) {
                TextView tv_title = (TextView)this.findViewById(R.id.textview_title);
                tv_title.setText(book.getMetadata().getTitle());
                TextView tv_authors = (TextView)this.findViewById(R.id.textview_authors);
                tv_authors.setText(book.getMetadata().getAuthors() == null ? "" : 
                    OnyxMetadata.convertAuthorsToString(book.getMetadata().getAuthors()));
                TextView tv_size = (TextView)this.findViewById(R.id.textview_size);
                tv_size.setText(String.valueOf(book.getMetadata().getSize()));
                TextView tv_description = (TextView)this.findViewById(R.id.textview_description);
                tv_description.setText(book.getMetadata().getDescription());
            }
        }
    }
    
}
