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

import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import android.text.format.DateFormat;
import android.util.Log;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.Context;

public class Debug
{
    private final String TAG = "SmartGPSLogger";
    private final String DIR = "/sdcard/SmartGPSLogger/";

    private File file;
    private BufferedWriter writer;
    private boolean isDebugMode;

    public Debug (Context context) throws java.io.IOException
    {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int flags = packageInfo.applicationInfo.flags;
            isDebugMode = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            isDebugMode = true;
        }

        if (isDebugMode)
        {
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite())
                {
                    File dir = new File(DIR);
                    if (!dir.exists() && !dir.mkdir())
                        throw new java.io.IOException("Failed to access/create " + DIR +
                                                      " directory");
                    else
                        {
                            file = new File(DIR + "debug.log");
                            writer = new BufferedWriter(new FileWriter(file, true));
                        }
                }
        }
        Log.d("SmartGPSLogger Debug", isDebugMode ? "is in debug mode" : "is NOT in debug mode");
    }

    protected void finalize() throws Throwable
    {
        if (isDebugMode)
            writer.close();
        super.finalize();
    }

    public void log(String message)
    {
        Log.d(TAG, message);
        if (isDebugMode)
        {
            try {
                writer.write(DateFormat.format("yyyy:MM:dd kk:mm:ss: ", System.currentTimeMillis())
                             + message + "\n");
                writer.flush();
            } catch (java.io.IOException e) {
                Log.e(TAG, "Debug.log failed + " + e.toString());
            }
        }
    }
}
