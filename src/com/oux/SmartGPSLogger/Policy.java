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

package com.oux;

import android.content.SharedPreferences;
import android.content.Context;

/* This class implements the smart wake-up policy.  */
public class Policy
{
    private final int DEFAULT_MIN_FREQ = 5;
    private final int DEFAULT_MAX_FREQ = 60;
    private final float DEFAULT_MIN_DIST = (float)0.05;

    private Context context;
    private SharedPreferences pref;

    private int currentFreq;
    private Position prevPosition;

    public Policy (Context context)
    {
        this.context = context;
        pref = context.getSharedPreferences(Constants.NAME,
                                            Context.MODE_PRIVATE);
        currentFreq = pref.getInt("min_freq", DEFAULT_MIN_FREQ);
        /* Set an improbable first previous position.  */
        prevPosition = new Position(0, 0);
    }

    /* Set the next wake-up taking into account the current position
     * POS (could be null), the previous position and the current
     * frequency.  */
    public void setNextWakeUp (Position pos)
    {
        if (pos == null)
            currentFreq = Math.min(currentFreq * 2,
                                   pref.getInt("max_freq", DEFAULT_MAX_FREQ));
        else if (prevPosition.distance(pos) <= pref.getFloat("min_dist",
                                                             DEFAULT_MIN_DIST))
            currentFreq = Math.min(currentFreq * 2,
                                   pref.getInt("max_freq", DEFAULT_MAX_FREQ));
        else
            currentFreq = Math.max(currentFreq / 4,
                                   pref.getInt("min_freq", DEFAULT_MAX_FREQ));

        prevPosition = pos;

        // TODO: set Service next wakeup to currentFreq
    }
}
