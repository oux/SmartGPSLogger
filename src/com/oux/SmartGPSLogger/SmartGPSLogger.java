package com.oux.SmartGPSLogger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import android.app.SearchManager;
import com.google.android.maps.MyLocationOverlay;

public class SmartGPSLogger extends MapActivity
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MapView map = (MapView)findViewById(R.id.map);
        me = new MyLocationOverlay(this, map);
        map.getOverlays().add(me);

        mService = new Intent(this, GPSService.class);
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
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
    public boolean onCreateOptionsMenu(Menu menu) {
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

}
// vi:et
