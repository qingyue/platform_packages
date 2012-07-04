package com.onyx.android.launcher.dialog;

import android.app.Activity;
import android.view.View;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.launcher.view.DialogBaseSettings;

public class DialogStartupSettings extends DialogBaseSettings
{
    
    public DialogStartupSettings(Activity hostActivity)
    {
        super(hostActivity);
        
        String[] array = this.getGridView().getResources().getStringArray(R.array.startup_settings);
        SelectionAdapter adapter = new SelectionAdapter(hostActivity, this.getGridView(), array);
        this.getGridView().setAdapter(adapter);
        
        this.getButtonSet().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                DialogStartupSettings.this.dismiss();
            }
        });
        
        this.getButtonCancel().setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogStartupSettings.this.cancel();
            }
        });
        
        this.getTextViewTitle().setText(R.string.Startup_Settings);
        
        adapter.getPaginator().setPageSize(array.length);
    }
}
