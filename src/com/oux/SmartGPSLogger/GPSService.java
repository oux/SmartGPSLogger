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

import android.content.res.Resources;
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
import java.util.ListIterator;

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
    private Debug debug;
    private boolean ready = false;
    private final IBinder binder = new MyBinder();
    private long lastGPSFixTime = 0;
    private LinkedList<LocationUpdate> updaters = new LinkedList<LocationUpdate>();

    @Override
    public void onCreate()
    {
        super.onCreate();
        Settings.getInstance(this);

        try {
            debug = new Debug();
            data = new GPSDataManager(this, debug);
        } catch (java.io.IOException e) {
            stopSelf();
            ready = false;
            Log.e(TAG, "Writers creation failed. Service start canceled : " +
                  e.toString());
        }
        updaters.add(data);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                  "SmartGPSLogger");

        mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        policy = new Policy(this, debug);
        timer = new Timer("timeout", true);

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
            timer.schedule(timeout, Settings.getInstance().gpsTimeout() * 1000);
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
            updaters.remove(data);
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
        lastGPSFixTime = System.currentTimeMillis();
        Location prev = data.getLastLocation();
        notifyNewLocation(loc);
        mLm.removeUpdates(GPSService.this);
        policy.setNextWakeUp(prev, loc);
        wakelock.release();
    }

    private void notifyNewLocation(Location loc)
    {
        ListIterator<LocationUpdate> it = updaters.listIterator(0);
        while (it.hasNext())
            it.next().newLocation(loc);
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public class MyBinder extends Binder {
        public LinkedList<Location> getLocations()
        {
            return GPSService.this.data.getLocations();
        }

        public void renew()
        {
            GPSService.this.policy.setCurrentFreqToMin();
            GPSService.this.getNewLocation();
        }

        public long getLastGPSFixTime()
        {
            return GPSService.this.lastGPSFixTime;
        }

        public void registerLocationUpdate(LocationUpdate updater)
        {
            GPSService.this.updaters.add(updater);
        }

        public void unregisterLocationUpdate(LocationUpdate updater)
        {
            GPSService.this.updaters.remove(updater);
        }
    }
}
