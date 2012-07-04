package com.onyx.android.launcher.dialog;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.FileIconFactory;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.ui.data.FileItemData;

public class DialogFileRename extends OnyxDialogBase
{
    private TextView mTextViewCurrentName = null;
    private EditText mEditTextRename = null;
    private Button mButtonSet = null;
    private Button mButtonCancel = null;
    
    public DialogFileRename(Context context, final FileOperationHandler fileHandler, final FileItemData dstItem)
    {
        super(context);
        
        this.setContentView(R.layout.dialog_rename);
        
        mTextViewCurrentName = (TextView)this.findViewById(R.id.textview_currentname);
        mEditTextRename = (EditText)this.findViewById(R.id.edittext_rename);
        mButtonSet = (Button)this.findViewById(R.id.button_set_rename);
        mButtonCancel = (Button)this.findViewById(R.id.button_cancel_rename);
        
        mTextViewCurrentName.setText(dstItem.getText());
        mEditTextRename.setText(dstItem.getText());
        
        mButtonSet.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                File currentFile = GridItemManager.getFileFromURI(dstItem.getURI());
                
                String new_path = currentFile.getParent() + "/" + mEditTextRename.getText().toString().trim();
                File newFile = new File(new_path); 
                if (SDFileFactory.reName(currentFile, newFile)) {
                    if (newFile.isFile()) {
                        Bitmap icon = FileIconFactory.getIconByFileName(newFile.getName());
                        dstItem.setBitmap(icon);
                    }
                    dstItem.setText(mEditTextRename.getText().toString());
                    dstItem.setURI(GridItemManager.getURIFromFilePath(newFile.getPath()));
                    
                    fileHandler.getAdapter().notifyDataSetChanged();
                }
                else {
                    Toast.makeText(fileHandler.getContext(), R.string.rename_failed, Toast.LENGTH_SHORT).show();
                }
                DialogFileRename.this.dismiss();
            }
        });
        
        mButtonCancel.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogFileRename.this.cancel();
            }
        });
    }
    
}
