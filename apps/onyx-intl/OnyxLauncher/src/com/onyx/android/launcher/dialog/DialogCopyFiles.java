package com.onyx.android.launcher.dialog;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.view.OnyxDialogBase;

public class DialogCopyFiles extends OnyxDialogBase
{
    private TextView mTextViewCopyName = null;
    private ProgressBar mProgressBar = null;
    public DialogCopyFiles(Context context)
    {
        super(context);

        this.setContentView(R.layout.dialog_progressbar_horizontal);
        
        mTextViewCopyName = (TextView)this.findViewById(R.id.textview_copyfilename);
        mProgressBar = (ProgressBar)this.findViewById(R.id.progressbar_horizontal);
    }
    
    public ProgressBar getProgressBar()
    {
        return mProgressBar;
    }
    
    public TextView getCopyName()
    {
        return mTextViewCopyName;
    }
}
