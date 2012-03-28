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

import android.app.Service;
import android.os.PowerManager;
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.os.Looper;
import java.util.Timer;
import java.util.TimerTask;
import android.os.IBinder;
import android.os.Binder;
import android.app.AlarmManager;
import android.app.PendingIntent;

public class GPSService extends Service implements LocationListener
{
    private static final String TAG = "GPSService";
    public static PowerManager.WakeLock wakelock;
    private Object lock = new Object();
    private LocationManager mLm;
    private LocationListener mLl;
    private Location curLoc;
    private DataWriter writer;
    private Policy policy;
    private Timer timer;
    private TimerTask timeout;

    @Override
    public void onCreate()
    {
        super.onCreate();
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                  "SmartGPSLogger");

        mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            writer = new DataWriter();
        } catch (java.io.IOException e) {
            Log.e(TAG, "Failed to open file: " + e.toString());
        }

        policy = new Policy(this);
        timer = new Timer("timeout", true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (!wakelock.isHeld())
            getNewLocation();
        return Service.START_STICKY;
    }

    public void getNewLocation()
    {
        Log.d(TAG, "ask for location update");
        if (!wakelock.isHeld()) {
            wakelock.acquire();

            Intent intent = new Intent();
            intent.setAction(IntentReceiver.NEW_LOCATION_REQUESTED);
            sendBroadcast(intent);
            mLm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            timeout = new TimerTask() {
                    public void run () {
                        GPSService.this.timeout();
                    }};
            timer.schedule(timeout, 60000);
        }
    }

    private void timeout ()
    {
        policy.setNextWakeUp(null);
        mLm.removeUpdates(GPSService.this);
        wakelock.release();
        Log.d(TAG, "timeout fired");
    }

    @Override
	public void onDestroy()
    {
        if (wakelock.isHeld()) {
            timeout.cancel();
            timeout();
        }
        Log.d(TAG, "destroyed");
    }

    @Override
    public void onLocationChanged(Location loc)
    {
        Log.d(TAG, "new location found");
        timeout.cancel();
        try {
            writer.write(loc);
        } catch (java.io.IOException e) {
            Log.e(TAG, "Failed to write location data : " +
                  e.toString());
        }
        policy.setNextWakeUp(loc);
        wakelock.release();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
