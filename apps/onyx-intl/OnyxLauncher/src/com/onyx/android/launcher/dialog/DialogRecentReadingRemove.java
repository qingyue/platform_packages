/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.task.DeleteFileTask;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;

/**
 * @author joy
 *
 */
public class DialogRecentReadingRemove extends OnyxDialogBase
{

    public DialogRecentReadingRemove(final Activity activity, final FileOperationHandler fileHandler)
    {
        super(activity);
        
        this.setContentView(R.layout.dialog_recent_reading_remove);
        
        TextView textview_fileName = (TextView)this.findViewById(R.id.textview_filename);
        final CheckBox checkbox = (CheckBox)this.findViewById(R.id.checkbox_remove_file_on_disk);
        Button button_set = (Button)this.findViewById(R.id.button_set);
        Button button_cancel = (Button)this.findViewById(R.id.button_cancel);
        
        if (fileHandler.getSourceItems().size() == 1) {
            textview_fileName.setText("Remove record " + fileHandler.getSourceItems().get(0).getText() + "?");
        }
        else {
            assert(fileHandler.getSourceItems().size() > 1);
            textview_fileName.setText("Remove " + fileHandler.getSourceItems().size() + " records?");
        }
        
        button_set.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                for (FileItemData f : fileHandler.getSourceItems()) {
                    if (f instanceof BookItemData) {
                        BookItemData book = (BookItemData)f;
                        if (book.getMetadata() != null) {
                            if (CmsCenterHelper.removeRecentReading(activity, book)) {
                                fileHandler.getAdapter().removeItem(book);
                            }
                        }
                    }
                }
                
                if (checkbox.isChecked()) {
                    DeleteFileTask deleteFileTask = new DeleteFileTask(fileHandler);
                    deleteFileTask.execute();
                }
                
                DialogRecentReadingRemove.this.dismiss();
            }
        });
        
        button_cancel.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogRecentReadingRemove.this.cancel();
            }
        });
    }

}
