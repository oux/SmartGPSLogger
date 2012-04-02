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

/* This class implements the smart wake-up policy.  */
public class Policy
{
    private static final String TAG = "GPSPolicy";
    private Context mContext;
    private Resources mRes;

    private int currentFreq;
    private AlarmManager am;
    private Debug debug;

    public Policy (Context context, Debug debug)
    {
        this.debug = debug;
        mContext = context;
        currentFreq = Settings.getInstance().minFreq();
        am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public int getCurrentFreq()
    {
        return currentFreq;
    }

    public void setCurrentFreqToMin()
    {
        currentFreq = Settings.getInstance().minFreq();
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
            currentFreq = Math.min(currentFreq * 2, Settings.getInstance().maxFreq());
        } else if (prev.distanceTo(cur) <= Settings.getInstance().minDist()) {
            debug.log("prev and cur are very close");
            currentFreq = Math.min(currentFreq * 2, Settings.getInstance().maxFreq());
        } else {
            debug.log("last case");
            setCurrentFreqToMin();
        }
        Intent intent = new Intent();
        intent.setAction(IntentReceiver.REQUEST_NEW_LOCATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                                                                 intent,
                                                                 PendingIntent.FLAG_ONE_SHOT);
        am.set(AlarmManager.RTC_WAKEUP,
               System.currentTimeMillis() + (currentFreq * 60 * 1000), pendingIntent);
        debug.log("will wake-up in " + currentFreq + " minutes");

        return currentFreq;
    }
}
// vi:et
