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

public class Position
{
    private double x;
    private double y;

    public Position (double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /* Return the distance between the position and position P.  */
    public double distance (Position p)
    {
        return Math.sqrt(((p.x - x) * (p.x - x)) + (p.y - y) * (p.y - y));
    }
}
// vi:et
