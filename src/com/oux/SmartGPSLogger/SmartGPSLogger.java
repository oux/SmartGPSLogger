/*
 * Copyright (C) 2012 Michel Sébastien & Jérémy Compostella
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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.app.SearchManager;
import android.content.ServiceConnection;
import java.util.LinkedList;
import android.content.ComponentName;
import android.os.IBinder;
import android.location.Location;
import com.google.android.maps.MyLocationOverlay;
import android.graphics.Color;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import android.text.format.DateFormat;

public class SmartGPSLogger extends MapActivity implements LocationUpdate
{
    private static final String TAG = "SmartGPSLogger";

    // Identifiers for option menu items
    private static final int MENU_RECLOG = Menu.FIRST;
    private static final int MENU_STOPLOG = MENU_RECLOG + 1;
    private static final int MENU_PLAYLOG = MENU_STOPLOG + 1;
    private static final int MENU_SETTINGS = MENU_PLAYLOG + 1;

    private DataWriter writer;
    private Intent mService;
    private MyLocationOverlay me;
    private MapView map;
    private TextView text;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Settings.getInstance(this);
        setContentView(R.layout.main);

        map = (MapView)findViewById(R.id.map);
        me = new MyLocationOverlay(this, map);
        map.getOverlays().add(me);
        map.setBuiltInZoomControls(true);

        text = (TextView)findViewById(R.id.text_area);

        mService = new Intent(this, GPSService.class);
        bindService(mService, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        me.disableCompass();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        me.enableCompass();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_RECLOG, 0, R.string.menu_reclog)
            .setIcon(R.drawable.ic_reclog)
            .setAlphabeticShortcut(SearchManager.MENU_KEY);
        menu.add(0, MENU_STOPLOG, 0, R.string.menu_stoplog)
            .setIcon(R.drawable.ic_stoplog)
            .setAlphabeticShortcut(SearchManager.MENU_KEY);
        menu.add(0, MENU_PLAYLOG, 0, R.string.menu_playlog)
            .setIcon(R.drawable.ic_playlog)
            .setAlphabeticShortcut(SearchManager.MENU_KEY);
        menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
            .setIcon(android.R.drawable.ic_menu_preferences)
            .setIntent(new Intent(this,Preferences.class))
            .setAlphabeticShortcut(SearchManager.MENU_KEY);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_RECLOG:
            this.startService(mService);
            Log.d(TAG, "started");
            return true;
        case MENU_STOPLOG:
            this.stopService(mService);
            Log.d(TAG, "stopped");
            return true;
        case MENU_PLAYLOG:
            // START MAP ACTIVITY
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newLocation(Location loc)
    {
        updateText(System.currentTimeMillis());
    }

    private void updateText(long lastGPSFixTime)
    {
        if (lastGPSFixTime == 0)
            text.setText("Last GPS fix information is unavailable"); 
        else
            text.setText("Last GPS fix at + " + DateFormat.format("yyyy:MM:dd kk:mm:ss", lastGPSFixTime));

        if (lastGPSFixTime >= System.currentTimeMillis() - Settings.getInstance().maxFreq() * 60 * 1000)
            text.setTextColor(Color.GREEN);
        else
            text.setTextColor(Color.RED);
    }

    private ServiceConnection connection = new ServiceConnection()
        {
            private GPSService.MyBinder binder;
            private PathOverlay path;

            public void onServiceConnected(ComponentName className, IBinder binder)
            {
                this.binder = (GPSService.MyBinder)binder;
                LinkedList<Location> locations = this.binder.getLocations();
                path = new PathOverlay(locations);
                map.getOverlays().add(path);
                SmartGPSLogger.this.updateText(this.binder.getLastGPSFixTime());
                this.binder.registerLocationUpdate(SmartGPSLogger.this);
                this.binder.registerLocationUpdate(path);
            }

            public void onServiceDisconnected(ComponentName className)
            {
                map.getOverlays().remove(path);
                this.binder.unregisterLocationUpdate(SmartGPSLogger.this);
                this.binder.unregisterLocationUpdate(path);
            }
        };
}
// vi:et
