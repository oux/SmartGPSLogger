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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.IBinder;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {
    private static final String TAG = "IntentReceiver";

    public static final String GET_NEW_LOCATION = "com.oux.GET_NEW_LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) ||
            GET_NEW_LOCATION.equals(intent.getAction())) {
            context.startService(new Intent(context, GPSService.class));
        }
    }
}
