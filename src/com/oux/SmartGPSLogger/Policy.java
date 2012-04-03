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

import android.content.Context;
import android.content.res.Resources;
import com.oux.SmartGPSLogger.R;
import android.location.Location;
import android.app.AlarmManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/* This class implements the smart wake-up policy.  */
public class Policy extends BroadcastReceiver
{
    private static final String TAG = "GPSPolicy";
    private Context mContext;
    private Resources mRes;

    private int currentFreq;
    private AlarmManager am;
    private Debug debug;
    private long nextWakeUpTime = 0;

    private double coef = 1.0;

    public Policy (Context context, Debug debug)
    {
        this.debug = debug;
        mContext = context;
        currentFreq = Settings.getInstance().minFreq();
        am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        context.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public long getNextWakeUpTime()
    {
        return nextWakeUpTime;
    }

    public int getCurrentFreq()
    {
        return currentFreq;
    }

    public void setCurrentFreqToMin()
    {
        currentFreq = (int)((double)Settings.getInstance().minFreq() * coef);
    }

    private int maxFreq()
    {
        return (int)((double)Settings.getInstance().maxFreq() * coef);
    }

    @Override
    public void onReceive(Context arg0, Intent intent)
    {
        int level = intent.getIntExtra("level", 0);
        if (level > 25) {
            if (coef != 1.0)
                debug.log("battery normal state, set coef to 1.0");
            coef = 1.0;
        } else if (level > 15 && coef != 2.0) {
            if (coef != 2.0)
                debug.log("battery warning state, set coef to 2.0");
            coef = 2.0;
        } else if (coef != 4.0) {
            if (coef != 1.5)
                debug.log("battery critical state, set coef to 4.0");
            coef = 4.0;
        }
    }

    /* Set the next wake-up taking into account the current location
     * LOC (could be null), the previous position and the current
     * frequency.  */
    public int setNextWakeUp (Location prev, Location cur)
    {
        if (prev == null && cur != null)
            debug.log("prev is null and cur is NOT null");
            /* Keep currentFreq unchanged */
        else if (cur == null) {
            debug.log("cur is null");
            currentFreq = Math.min(currentFreq * 2, maxFreq());
        } else if (prev.distanceTo(cur) <= Settings.getInstance().minDist()) {
            debug.log("prev and cur are very close");
            currentFreq = Math.min(currentFreq * 2, maxFreq());
        } else {
            debug.log("last case");
            setCurrentFreqToMin();
        }
        Intent intent = new Intent();
        intent.setAction(IntentReceiver.REQUEST_NEW_LOCATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                                                                 intent,
                                                                 PendingIntent.FLAG_ONE_SHOT);

        nextWakeUpTime = System.currentTimeMillis() + (currentFreq * 60 * 1000);
        am.set(AlarmManager.RTC_WAKEUP, nextWakeUpTime, pendingIntent);
        debug.log("will wake-up in " + currentFreq + " minutes");

        return currentFreq;
    }
}
// vi:et
