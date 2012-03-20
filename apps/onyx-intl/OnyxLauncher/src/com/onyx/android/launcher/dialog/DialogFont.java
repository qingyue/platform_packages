package com.onyx.android.launcher.dialog;

import android.app.Activity;
import android.view.View;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.launcher.view.DialogBaseSettings;

public class DialogFont extends DialogBaseSettings
{
    public DialogFont(Activity hostActivity)
    {
        super(hostActivity);

        String[] array = this.getGridView().getResources().getStringArray(R.array.font_styles);
        SelectionAdapter adapter = new SelectionAdapter(hostActivity, this.getGridView(), array);
        this.getGridView().setAdapter(adapter);
        
        this.getButtonSet().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                DialogFont.this.dismiss();
            }
        });
        
        this.getButtonCancel().setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogFont.this.cancel();
            }
        });
        
        this.getTextViewTitle().setText("Default Font");
        
        adapter.getPaginator().setPageSize(array.length);
    }
}
