/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.aurora.device.DeviceSettings;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;

import com.aurora.device.DeviceSettings.ModeSwitch.DCModeSwitch;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_DC_SWITCH = "dc";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_SETTINGS_PREFIX = "device_setting_";
    public static final String KEY_BUTTON_SWAP = "button_swap";

    private static final boolean sIsOnePlus5t = android.os.Build.DEVICE.equals("OnePlus5T");
    private TwoStatePreference mButtonSwap;

    private SwitchPreference mFpsInfo;
    private VibratorStrengthPreference mVibratorStrength;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main);

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null)
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());

        TwoStatePreference mDCModeSwitch = findPreference(KEY_DC_SWITCH);

        mDCModeSwitch.setEnabled(DCModeSwitch.isSupported());
        mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled());
        mDCModeSwitch.setOnPreferenceChangeListener(new DCModeSwitch());

        mFpsInfo = findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(isFPSOverlayRunning());
        mFpsInfo.setOnPreferenceChangeListener(this);

        mButtonSwap = (TwoStatePreference) findPreference(KEY_BUTTON_SWAP);
        if (!sIsOnePlus5t) {
            mButtonSwap.setEnabled(ButtonSwap.isSupported());
            mButtonSwap.setChecked(ButtonSwap.isCurrentlyEnabled(this.getContext()));
            mButtonSwap.setOnPreferenceChangeListener(new ButtonSwap());
        } else {
            mButtonSwap.setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mFpsInfo.setChecked(isFPSOverlayRunning());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            Intent fpsinfo = new Intent(getContext(), FPSInfoService.class);
            if (enabled) {
                getContext().startService(fpsinfo);
            } else {
                getContext().stopService(fpsinfo);
            }
        } 
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFPSOverlayRunning() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                am.getRunningServices(Integer.MAX_VALUE))
            if (FPSInfoService.class.getName().equals(service.service.getClassName()))
                return true;
        return false;
   }
}
