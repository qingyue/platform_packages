package com.onyx.android.launcher.dialog;

import android.app.Activity;
import android.view.View;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.launcher.view.DialogBaseSettings;

public class DialogTimeZone extends DialogBaseSettings
{
    
    public DialogTimeZone(Activity hostActivity)
    {
        super(hostActivity);
        
        String[] array = this.getGridView().getResources().getStringArray(R.array.time_zones);
        SelectionAdapter adapter = new SelectionAdapter(hostActivity, this.getGridView(), array);
        this.getGridView().setAdapter(adapter);

        this.getButtonSet().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                DialogTimeZone.this.dismiss();
            }
        });
        
        this.getButtonCancel().setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogTimeZone.this.cancel();
            }
        });
        
        this.getTextViewTitle().setText("Time Zone");
        
        adapter.getPaginator().setPageSize(array.length);
    }
}