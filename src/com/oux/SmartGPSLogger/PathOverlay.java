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

import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import android.graphics.Point;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Canvas;
import com.google.android.maps.MapView;
import android.location.Location;
import java.util.LinkedList;
import java.util.ListIterator;

public class PathOverlay extends Overlay implements LocationUpdate
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

    public void newLocation(Location loc)
    {
        points.add(new GeoPoint((int)(loc.getLatitude() * 1E6),
                                (int)(loc.getLongitude() * 1E6)));
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
