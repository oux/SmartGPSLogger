/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oux.SmartGPSLogger;

import com.oux.SmartGPSLogger.SmartGPSLogger;
import com.oux.SmartGPSLogger.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.EditTextPreference;
import android.content.res.Resources;

/**
 * This activity is an example of a simple settings screen that has default
 * values.
 * <p>
 * In order for the default values to be populated into the
 * {@link SharedPreferences} (from the preferences XML file), the client must
 * call
 * {@link PreferenceManager#setDefaultValues(android.content.Context, int, boolean)}.
 * <p>
 * This should be called early, typically when the application is first created.
 * This ensures any of the application's activities, services, etc. will have
 * the default values present, even if the user has not wandered into the
 * application's settings. For ApiDemos, this is {@link ApiDemosApplication},
 * and you can find the call to
 * {@link PreferenceManager#setDefaultValues(android.content.Context, int, boolean)}
 * in its {@link ApiDemosApplication#onCreate() onCreate}.
 */
public class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.default_values);

        SharedPreferences pref;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();
        EditTextPreference etp = (EditTextPreference) findPreference("min_period");
        etp.setSummary("Default: " + res.getString(R.string.MinPeriod) +
                       " minutes, current: " + pref.getString("min_period",
                           res.getString(R.string.MinPeriod)) + " minutes");
        etp = (EditTextPreference) findPreference("max_period");
        etp.setSummary("Default: " + res.getString(R.string.MaxPeriod) +
                       " minutes, current: " + pref.getString("max_period",
                           res.getString(R.string.MaxPeriod)) + " minutes");
        etp = (EditTextPreference) findPreference("min_dist");
        etp.setSummary("Default: " + res.getString(R.string.MinDist) +
                       " meters, current: " + pref.getString("min_dist",
                           res.getString(R.string.MinDist)) + " meters");
        etp = (EditTextPreference) findPreference("gps_timeout");
        etp.setSummary("Default: " + res.getString(R.string.GpsTimeout) +
                       " seconds, current: " + pref.getString("gps_timeout",
                           res.getString(R.string.GpsTimeout)) + " seconds");
        etp = (EditTextPreference) findPreference("max_loc");
        etp.setSummary("Default: " + res.getString(R.string.LocCacheSize) +
                       " locations, current: " + pref.getString("max_loc",
                           res.getString(R.string.LocCacheSize)) + " locations");
    }

}

// vi:et
