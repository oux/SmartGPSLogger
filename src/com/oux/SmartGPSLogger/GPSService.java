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

import android.preference.PreferenceManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
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
import java.util.LinkedList;

public class GPSService extends Service implements LocationListener
{
    private static final String TAG = "GPSService";
    public static PowerManager.WakeLock wakelock;
    private LocationManager mLm;
    private GPSDataManager data;
    private Policy policy;
    private Timer timer;
    private TimerTask timeout;
    private Resources mRes;
    private SharedPreferences pref;
    private Debug debug;
    private boolean ready = false;
    private final IBinder binder = new MyBinder();

    @Override
    public void onCreate()
    {
        super.onCreate();

        try {
            debug = new Debug();
            data = new GPSDataManager(this, debug);
        } catch (java.io.IOException e) {
            stopSelf();
            ready = false;
            Log.e(TAG, "Writers creation failed. Service start canceled : " +
                  e.toString());
        }

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                  "SmartGPSLogger");

        mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        policy = new Policy(this, debug);
        timer = new Timer("timeout", true);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mRes = this.getResources();

        ready = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (ready && !wakelock.isHeld())
            getNewLocation();
        return Service.START_STICKY;
    }

    public void getNewLocation()
    {
        debug.log("ask for location update");
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
            int gpstimeout = Integer.valueOf
                (pref.getString("gps_timeout",
                                mRes.getString(R.string.GpsTimeout)));
            timer.schedule(timeout, gpstimeout * 1000);
        }
    }

    private void timeout ()
    {
        debug.log("timeout fired");
        mLm.removeUpdates(GPSService.this);
        policy.setNextWakeUp(data.getLastLocation(), null);
        wakelock.release();
    }

    @Override
    public void onDestroy()
    {
        if (wakelock.isHeld()) {
            timeout.cancel();
            timeout();
        }
        debug.log("service destroyed");
    }

    @Override
    public void onLocationChanged(Location loc)
    {
        debug.log("new location found");
        timeout.cancel();
        Location prev = data.getLastLocation();
        data.addNewLocation(loc);
        mLm.removeUpdates(GPSService.this);
        policy.setNextWakeUp(prev, loc);
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
        return binder;
    }

    public class MyBinder extends Binder {
        public GPSService getService()
        {
            return GPSService.this;
        }

        public LinkedList<Location> getLocations()
        {
            return GPSService.this.data.getLocations();
        }
    }
}
