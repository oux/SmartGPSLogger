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
import android.app.AlarmManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.util.Log;

/* This class implements the smart wake-up policy.  */
public class Policy
{
    private static final String TAG = "GPSPolicy";
    private Context mContext;
    private Resources mRes;
    private SharedPreferences pref;

    private int currentFreq;
    private Location prevLocation = null;
    private AlarmManager am;

    public Policy (Context context)
    {
        mContext = context;
        mRes = mContext.getResources();
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        currentFreq = Integer.valueOf(pref.getString("min_freq",
                        mRes.getString(R.string.MinFreq)));
        am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    }

    /* Set the next wake-up taking into account the current location
     * LOC (could be null), the previous position and the current
     * frequency.  */
    public void setNextWakeUp (Location loc)
    {
        if (prevLocation == null && loc != null)
            Log.d(TAG, "setNextWakeUp: prevLocation is null and loc is NOT null");
            /* Keep currentFreq unchanged */
        else if (loc == null) {
            Log.d(TAG, "setNextWakeUp: loc is null");
            currentFreq = Math.min(currentFreq * 2,
                                   Integer.valueOf(
                                       pref.getString("max_freq", mRes.getString(R.string.MaxFreq))));
        } else if (prevLocation.distanceTo(loc) <= Float.valueOf(pref.getString("min_dist",
                                                               mRes.getString(R.string.MinDist)))) {
            Log.d(TAG, "setNextWakeUp: short distance");
            currentFreq = Math.min(currentFreq * 2,
                                   Integer.valueOf(
                                       pref.getString("max_freq", mRes.getString(R.string.MaxFreq))));
        } else {
            Log.d(TAG, "setNextWakeUp: last case");
            currentFreq = Math.max(currentFreq / 4,
                                   Integer.valueOf(
                                       pref.getString("min_freq", mRes.getString(R.string.MinFreq))));
        }
        prevLocation = loc;
        Intent intent = new Intent();
        intent.setAction(IntentReceiver.REQUEST_NEW_LOCATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                                                                 intent,
                                                                 PendingIntent.FLAG_ONE_SHOT);
        am.set(AlarmManager.RTC_WAKEUP,
               System.currentTimeMillis() + (currentFreq * 60 * 1000), pendingIntent);
        Log.d(TAG, "will wake-up " + currentFreq + " minutes");
    }
}
// vi:et
