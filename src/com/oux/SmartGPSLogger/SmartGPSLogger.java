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
import java.util.ListIterator;
import android.content.ComponentName;
import android.os.IBinder;
import android.location.Location;
import android.graphics.Canvas;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import android.widget.Toast;
import android.graphics.Point;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Color;

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
    private MapView map;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        map = (MapView)findViewById(R.id.map);
        me = new MyLocationOverlay(this, map);
        map.getOverlays().add(me);

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

    private ServiceConnection connection = new ServiceConnection()
        {
            public void onServiceConnected(ComponentName className, IBinder binder)
            {
                LinkedList<Location> locations = ((GPSService.MyBinder) binder).getLocations();
                Toast.makeText(SmartGPSLogger.this, "Connected, locations list length : " + locations.size(),
                               Toast.LENGTH_SHORT).show();
                map.getOverlays().add(new PathOverlay(locations));
            }

            public void onServiceDisconnected(ComponentName className) {}
        };

    private class PathOverlay extends Overlay
    {
        private LinkedList<GeoPoint> points = new LinkedList<GeoPoint>();

        public PathOverlay(LinkedList<Location> locations)
        {
            ListIterator<Location> it = locations.listIterator(0);
            while (it.hasNext()) {
                Location cur = it.next();
                points.add(new GeoPoint((int)(cur.getLatitude() * 1E6),
                                        (int)(cur.getLongitude() * 1E6)));
            }
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow)
        {
            super.draw(canvas, mapView, shadow);

            if (points.size() < 2)
                return;

            Paint mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(2);

            Path path = new Path();
            Projection projection = mapView.getProjection();
            GeoPoint gP1 = points.getFirst();
            Point p1 = new Point();
            Point p2 = new Point();
            ListIterator<GeoPoint> it = points.listIterator(1);
            while (it.hasNext()) {
                GeoPoint gP2 = it.next();

                projection.toPixels(gP1, p1);
                projection.toPixels(gP2, p2);

                path.moveTo(p2.x, p2.y);
                path.lineTo(p1.x, p1.y);

                gP1 = gP2;
            }

            canvas.drawPath(path, mPaint);
        }
    }
}
// vi:et
