package com.onyx.android.launcher.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.view.OnyxDialogBase;

public class DialogFormatFlast extends OnyxDialogBase
{
    private Button mSetButton = null;
    private Button mCancelButton = null;
    private TextView mTextViewTitle = null;
    
    public DialogFormatFlast(Activity hostActivity)
    {
        super(hostActivity);

        this.setContentView(R.layout.dialog_settings_formatflash_view);
        
        mSetButton = (Button)this.findViewById(R.id.button_yes_formatflash);
        mCancelButton = (Button)this.findViewById(R.id.button_no_formatflash);
        mTextViewTitle = (TextView)this.findViewById(R.id.textview_formatflash_title);
        mTextViewTitle.setText(R.string.Warning);
        
        mSetButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFormatFlast.this.dismiss();
            }
        });
        
        mCancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFormatFlast.this.cancel();
            }
        });
    }
}
