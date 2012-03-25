/*
 * Copyright (C) 2012 Jérémy Compostella
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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.res.Resources;
import com.oux.SmartGPSLogger.R;
import android.location.Location;

/* This class implements the smart wake-up policy.  */
public class Policy
{
    private Context mContext;
    private Resources mRes;
    private SharedPreferences pref;

    private int currentFreq;
    private Location prevLocation = null;

    public Policy (Context context)
    {
        mContext = context;
        mRes = mContext.getResources();
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        currentFreq = pref.getInt("min_freq", mRes.getInteger(R.integer.MinFreq));
    }

    /* Set the next wake-up taking into account the current location
     * LOC (could be null), the previous position and the current
     * frequency.  */
    public void setNextWakeUp (Location loc)
    {
        if (prevLocation == null)
            /* Keep currentFreq unchanged */;
        else if (loc == null)
            currentFreq = Math.min(currentFreq * 2,
                                   pref.getInt("max_freq", mRes.getInteger(R.integer.MaxFreq)));
        else if (prevLocation.distanceTo(loc) <= pref.getFloat("min_dist",
                                                               mRes.getInteger(R.integer.MinFreq)))
            currentFreq = Math.min(currentFreq * 2,
                                   pref.getInt("max_freq", mRes.getInteger(R.integer.MaxFreq)));
        else
            currentFreq = Math.max(currentFreq / 4,
                                   pref.getInt("min_freq", mRes.getInteger(R.integer.MinFreq)));

        prevLocation = loc;

        // TODO: set Service next wakeup to currentFreq
    }
}
// vi:et
