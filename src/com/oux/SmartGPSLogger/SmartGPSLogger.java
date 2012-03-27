package com.oux.SmartGPSLogger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.Intent;

public class SmartGPSLogger extends Activity
{
    private static final String TAG = "SmartGPSLogger";
    private DataWriter writer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent service = new Intent(this, GPSService.class);
		this.startService(service);
        Log.d(TAG, "started");
    }
}
// vi:et
