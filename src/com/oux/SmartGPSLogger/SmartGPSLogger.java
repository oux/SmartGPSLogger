package com.oux.SmartGPSLogger;

import android.app.Activity;
import android.os.Bundle;
import android.location.GpsStatus.NmeaListener;
import android.location.LocationManager;
import android.util.Log;
import android.content.Context;
// import android.view.View;

public class SmartGPSLogger extends Activity implements NmeaListener
{
	private static final String TAG = "SmartGPSLogger";
	private LocationManager mLm;
	// private Context mContext;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLm.addNmeaListener((NmeaListener)this);
    }

	@Override
	public void onNmeaReceived(long timestamp, String nmea)
	{
		Log.d(TAG, timestamp+"::"+nmea);
	}
}
