/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;
import android.content.Intent;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.SettingsActivity;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class SettingsActor extends ItemContainerActor
{
    private SettingsLanguageActor mLanguage = null;
    private SettingsDateActor mDate = null;
    private SettingsFontActor mFont = null;
    private SettingsAppSettingsActor mAppSettings = null;
    private SettingsAppPreferenceActor mAppPreference = null;
    private SettingsStartupActor mStartup = null;
    private SettingsScreenActor mScreen = null;
    private SettingsPowerActor mPower = null;
    private SettingsFormatFlashActor mFormatFlash = null;
    private SettingsTimezoneActor mTimezone = null;
    private SettingsWirelessNetworksActor mWirelessNetworks = null;
    private SettingsPrivacyActor mPrivacy = null;
    private SettingsStylusActor mStylus = null;
    private SettingsAboutActor mAbout = null;
    
    public SettingsActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Settings"), 
                "Settings", 
                R.drawable.settings));
        
        mLanguage = new SettingsLanguageActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mLanguage);
        
        mDate = new SettingsDateActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mDate);
        
        mFont = new SettingsFontActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mFont);
        
        mAppSettings = new SettingsAppSettingsActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mAppSettings);
        
        mAppPreference = new SettingsAppPreferenceActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mAppPreference);
        
        mStartup = new SettingsStartupActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mStartup);
        
        mScreen = new SettingsScreenActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mScreen);
        
        mPower = new SettingsPowerActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mPower);
        
        mFormatFlash = new SettingsFormatFlashActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mFormatFlash);
        
        mTimezone = new SettingsTimezoneActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mTimezone);

        mWirelessNetworks = new SettingsWirelessNetworksActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mWirelessNetworks);

        mPrivacy = new SettingsPrivacyActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mPrivacy);

        mStylus = new SettingsStylusActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mStylus);
        
        mAbout = new SettingsAboutActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mAbout);
    }

    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity)
    {
        Intent intent = new Intent(hostActivity, SettingsActivity.class);
        return ActivityUtil.startActivitySafely(hostActivity, intent);
    }
}
