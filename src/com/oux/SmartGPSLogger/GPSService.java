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
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;
import android.os.IBinder;

public class GPSService extends Service
{
    private static final String TAG = "GPSService";
    private PowerManager.WakeLock wakelock;
    private Object lock;
    private LocationManager mLm;
    private LocationListener mLl;
    private Location curLoc;
    private DataWriter writer;
    private Policy policy;

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SmartGPSLogger");
        mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            writer = new DataWriter();
        } catch (java.io.IOException e) {
            Log.e(TAG, "Failed to open file: " + e.toString());
        }
        startService();
    }

    private void startService ()
    {
        Thread thr = new Thread(null, new Runnable() {
                public void run()
                {
                    wakelock.acquire();
                    mLm.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLl, null);
                    try {
                        lock.wait(60000);
                        policy.setNextWakeUp(curLoc); // TODO: need some work
                    } catch (java.lang.InterruptedException e) {
                        policy.setNextWakeUp(null); // TODO: need some work
                        mLm.removeUpdates(mLl);
                    }
                    wakelock.release();
                    GPSService.this.stopSelf();
                }});
        thr.start();
    }

    public class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc)
        {
            String Text = "Current GPS data: " +
                "Time = " + loc.getTime() +
                ", Latitude = " + loc.getLatitude() +
                ", Longitude = " + loc.getLongitude() +
                ", Speed = " + loc.getSpeed() +
                ", Altitude = " + loc.getAltitude();

            Toast.makeText(getApplicationContext(),
                           Text,
                           Toast.LENGTH_SHORT).show();
            try {
                writer.write(loc);
            } catch (java.io.IOException e) {
                Log.e(TAG, "Failed to write location data : " +
                      e.toString());
            }
            curLoc = loc;
            lock.notify();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
