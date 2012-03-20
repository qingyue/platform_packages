package com.onyx.android.launcher.dialog;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.util.RefValue;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData.FileType;
import com.onyx.android.sdk.ui.data.GridItemData;

public class DialogFileNewFolder extends OnyxDialogBase
{
    private EditText mEditTextFileName = null;
    private Button mButtonSet = null;
    private Button mButtonCancel = null;
    
    public DialogFileNewFolder(Context context, final FileOperationHandler fileHandler)
    {
        super(context);
        
        this.setContentView(R.layout.dialog_newfolder);
        
        mEditTextFileName = (EditText)this.findViewById(R.id.edittext_newfolder);
        mButtonSet = (Button)this.findViewById(R.id.button_set_newfolder);
        mButtonCancel = (Button)this.findViewById(R.id.button_cancel_newfolder);
        
        mButtonSet.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                GridItemBaseAdapter adapter = fileHandler.getAdapter();
                
                String file_name = mEditTextFileName.getText().toString();
                if (file_name.length() != 0) {
                    String dir = GridItemManager.getFileFromURI(adapter.getHostURI()).getPath();
                    File file = new File(dir, file_name);
                    
                    RefValue<String> err_msg = new RefValue<String>(); 
                    if (SDFileFactory.createFolder(file, err_msg)) {
                        GridItemData item = new BookItemData(GridItemManager.getURIFromFilePath(file.getAbsolutePath()),
                                FileType.Directory, mEditTextFileName.getText().toString(), R.drawable.directory);
                        adapter.appendItem(item);
                    }
                    else {
                        Toast.makeText(fileHandler.getContext(), err_msg.getValue(), Toast.LENGTH_SHORT).show();
                    }

                    DialogFileNewFolder.this.dismiss();
                }
            }
        });
        
        mButtonCancel.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogFileNewFolder.this.cancel();
            }
        });
    }

}
