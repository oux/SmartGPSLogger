/*
 * Copyright (C) 2012 Michel Sébastien & Jérémy Compostella
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import android.util.Log;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;

public class Preferences extends PreferenceActivity
    implements OnPreferenceChangeListener
{
    private EditTextPreference minPeriod;
    private EditTextPreference maxPeriod;
    private EditTextPreference minDist;
    private EditTextPreference gpsTimeout;
    private EditTextPreference maxLoc;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.default_values);

        res = getResources();
        minPeriod = (EditTextPreference) findPreference("min_period");
        minPeriod.setOnPreferenceChangeListener(this);
        maxPeriod = (EditTextPreference) findPreference("max_period");
        maxPeriod.setOnPreferenceChangeListener(this);
        minDist = (EditTextPreference) findPreference("min_dist");
        minDist.setOnPreferenceChangeListener(this);
        gpsTimeout = (EditTextPreference) findPreference("gps_timeout");
        gpsTimeout.setOnPreferenceChangeListener(this);
        maxLoc = (EditTextPreference) findPreference("max_loc");
        maxLoc.setOnPreferenceChangeListener(this);

        onPreferenceChange(null, null);
    }

    public boolean onPreferenceChange(Preference preference, Object newVal)
    {
        EditTextPreference textPref = (EditTextPreference)preference;
        minPeriod.setSummary("Default: " + res.getString(R.string.MinPeriod) +
                             " minutes, current: " +
                             (textPref == minPeriod ? (String)newVal : Settings.getInstance().minPeriod()) +
                             " minutes");
        maxPeriod.setSummary("Default: " + res.getString(R.string.MaxPeriod) +
                             " minutes, current: " +
                             (textPref == maxPeriod ? (String)newVal : Settings.getInstance().maxPeriod()) +
                              " minutes");
        minDist.setSummary("Default: " + res.getString(R.string.MinDist) +
                           " meters, current: " +
                           (textPref == minDist ? (String)newVal : Settings.getInstance().minDist()) +
                            " meters");
        gpsTimeout.setSummary("Default: " + res.getString(R.string.GpsTimeout) +
                              " seconds, current: " +
                              (textPref == gpsTimeout ? (String)newVal : Settings.getInstance().gpsTimeout()) +
                               " seconds");
        maxLoc.setSummary("Default: " + res.getString(R.string.LocCacheSize) +
                          " locations, current: " +
                          (textPref == maxLoc ? (String)newVal : Settings.getInstance().locCacheSize()) +
                          " locations");

        return true;
    }
}
// vi:et
