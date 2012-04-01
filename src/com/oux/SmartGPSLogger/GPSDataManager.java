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
import android.location.Location;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import android.text.format.DateFormat;
import android.util.Log;
import java.util.LinkedList;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.res.Resources;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class GPSDataManager
{
    private final String DIR = "/sdcard/SmartGPSLogger/";
    private final String SUFFIX = ".log";
    private final String CURRENT = DIR + "current" + SUFFIX;
    private final String FMT = "yyyy-MM-dd";

    private Debug debug;
    private String lastLocDate = null;
    private File file;
    private BufferedWriter writer;
    private LinkedList<Location> locations;
    private Context mContext;
    private Resources mRes;
    private SharedPreferences pref;

    private void openCurrent() throws java.io.IOException
    {
        file = new File(CURRENT);
        writer = new BufferedWriter(new FileWriter(file, true));
    }

    private void loadLocations() throws java.io.IOException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        File file = new File(CURRENT);
        RandomAccessFile reader = new RandomAccessFile(file, "r");
        String line;
        if (file.length() > 80 * 100) {
            reader.seek(file.length() - (80 * 100));
            line = reader.readLine(); // Drop first probably uncomplete line
        }
        while ((line = reader.readLine()) != null)
        {
            String[] tokens = line.split(",");
            Location loc = new Location("MyGPSProvider");
            loc.setLatitude(new Float(tokens[2]).doubleValue());
            loc.setLongitude(new Float(tokens[3]).doubleValue());
            locations.add(loc);
        }
        reader.close();
    }

    public GPSDataManager(Context context, Debug debug) throws java.io.IOException
    {
        this.debug = debug;
        mContext = context;
        mRes = mContext.getResources();
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        locations = new LinkedList<Location>();
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite())
        {
            File dir = new File(DIR);
            if (!dir.exists() && !dir.mkdir())
                throw new java.io.IOException("Failed to create " + DIR +
                                              " directory");
            else
            {
                loadLocations();
                openCurrent();
            }
        }
    }

    protected void finalize() throws Throwable
    {
        writer.close();
        super.finalize();
    }

    public Location getLastLocation()
    {
        return locations.size() > 0 ? locations.getLast() : null;
    }

    public LinkedList<Location> getLocations()
    {
        return locations;
    }

    public void addNewLocation(Location loc)
    {
        Location prev = getLastLocation();

        boolean isClose = false;
        if (prev.distanceTo(loc) <= Float.valueOf(pref.getString("min_dist",
                                                                 mRes.getString(R.string.MinDist))))
            isClose = true;

        locations.add(loc);
        if (locations.size() > 100) // TODO: use a preference
            locations.removeFirst();

        String newLocDate = DateFormat.format(FMT, loc.getTime()).toString();

        if (lastLocDate == null)
            lastLocDate = DateFormat.format(FMT, loc.getTime()).toString();

        if (!lastLocDate.equals(newLocDate))
        {
            try {
                debug.log("Process file rotation");
                writer.close();
                file.renameTo(new File(DIR + lastLocDate + ".txt"));
                lastLocDate = DateFormat.format(FMT, loc.getTime()).toString();
                openCurrent();
            } catch (java.io.IOException e) {
                debug.log("File rotation failed : " + e.toString());
            }
        }

        String newLine = DateFormat.format("yyyy:MM:dd", loc.getTime()) +
            "," + DateFormat.format("kk:mm:ss", loc.getTime()) +
            "," + loc.getLatitude() +
            "," + loc.getLongitude() +
            "," + loc.getSpeed() +
            "," + loc.getAltitude() +
            "," + isClose;

        try {
            writer.write(newLine  + "\n");
            writer.flush();
        } catch (java.io.IOException e) {
            debug.log("Failed to write location data : " +
                      e.toString());
        }

        debug.log(newLine);
    }
}
