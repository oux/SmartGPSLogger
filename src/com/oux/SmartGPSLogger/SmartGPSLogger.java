package com.oux.SmartGPSLogger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;

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
    }
}
// vi:et
