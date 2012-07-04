package com.onyx.android.launcher.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onyx.android.launcher.OnyxApplication;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.task.DeleteFileTask;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.util.IntentFactory;

// TODO wait
public class DialogFileRemove extends OnyxDialogBase
{    
    public DialogFileRemove(Context context, final FileOperationHandler fileHandler)
    {
        super(context);
        
        this.setContentView(R.layout.dialog_deletefile);
    
        TextView textview_fileName = (TextView)this.findViewById(R.id.textview_filename_deletefile);
        Button button_set = (Button)this.findViewById(R.id.button_set_deletefile);
        Button button_cancel = (Button)this.findViewById(R.id.button_cancel_deletefile);
        
        if (fileHandler.getSourceItems().size() == 1) {
            textview_fileName.setText(OnyxApplication.getInstance().getResources().getString(R.string.Delete) + ": " + fileHandler.getSourceItems().get(0).getText() + "?");
        }
        else {
        	textview_fileName.setText(context.getText(R.string.Delete) + ": " + String.valueOf(fileHandler.getSourceItems().size()) + context.getText(R.string.files) + "?");
        }
        
        button_set.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogFileRemove.this.getContext().sendBroadcast(IntentFactory.getIntentDelete());
                
                DeleteFileTask deleteFileTask = new DeleteFileTask(fileHandler);
                deleteFileTask.execute();

                DialogFileRemove.this.dismiss();
            }
        });
        
        button_cancel.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogFileRemove.this.cancel();
            }
        });
        
    }
}
