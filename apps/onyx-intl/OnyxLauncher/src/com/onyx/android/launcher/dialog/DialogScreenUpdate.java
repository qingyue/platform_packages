package com.onyx.android.launcher.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Pair;
import android.view.View;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.launcher.view.DialogBaseSettings;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdatePolicy;

public class DialogScreenUpdate extends DialogBaseSettings
{
    
    public DialogScreenUpdate(Activity hostActivity)
    {
        super(hostActivity);
        
        final ArrayList<Pair<String, Object>> items = new ArrayList<Pair<String, Object>>();
        items.add(new Pair<String, Object>("Automatic", new Integer(-1)));
        items.add(new Pair<String, Object>("every 3 pages", new Integer(3)));
        items.add(new Pair<String, Object>("every 5 pages", new Integer(5)));
        items.add(new Pair<String, Object>("every 7 pages", new Integer(7)));
        items.add(new Pair<String, Object>("every 9 pages", new Integer(9)));
        items.add(new Pair<String, Object>("always", new Integer(0)));

        String[] array = this.getGridView().getResources().getStringArray(R.array.screen_update);
        final SelectionAdapter adapter = new SelectionAdapter(hostActivity, this.getGridView(), items, -1);
        this.getGridView().setAdapter(adapter);

        this.getButtonSet().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int pages = ((Integer)items.get(adapter.getSelection()).second).intValue();
                if (pages == -1) {
                    ScreenUpdateManager.setUpdatePolicy(DialogScreenUpdate.this.getGridView(),
                            UpdatePolicy.Automatic, 0);
                }
                else {
                    ScreenUpdateManager.setUpdatePolicy(DialogScreenUpdate.this.getGridView(),
                            UpdatePolicy.GUIntervally, pages);
                }
            }
        });
        
        this.getButtonCancel().setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogScreenUpdate.this.cancel();
            }
        });
        
        this.getTextViewTitle().setText("Screen Update");
        
        adapter.getPaginator().setPageSize(array.length);
    }
}
