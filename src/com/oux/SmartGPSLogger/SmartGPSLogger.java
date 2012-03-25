package com.oux.SmartGPSLogger;

import android.app.Activity;
import android.os.Bundle;
import android.location.GpsStatus.NmeaListener;
import android.location.LocationManager;
import android.util.Log;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.widget.Toast;

public class SmartGPSLogger extends Activity implements NmeaListener
{
    private LocationManager mLm;
    private DataWriter writer;
    // private Context mContext;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new MyLocationListener();
        mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        mLm.addNmeaListener((NmeaListener)this);
        try {
            writer = new DataWriter();
        } catch (java.io.IOException e) {
            Log.e(Constants.NAME, "Failed to open file: " + e.toString());
        }
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea)
    {
        //Log.d(Constants.NAME, timestamp+"::"+nmea);
        Toast.makeText(getApplicationContext(),
                timestamp+"::"+nmea,
                Toast.LENGTH_SHORT).show();
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
                Log.e(Constants.NAME, "Failed to write location data : " +
                      e.toString());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
// vi:et
