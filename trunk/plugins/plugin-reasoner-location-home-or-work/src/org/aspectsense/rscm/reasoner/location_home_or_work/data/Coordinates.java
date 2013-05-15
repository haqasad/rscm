/*
 * Really Simple Context Middleware (RSCM)
 *
 * Copyright (c) 2013 The RSCM Team
 *
 * This file is part of the RSCM: the Really Simple Context Middleware for ANDROID. More information about the project
 * is available at: http://code.google.com/p/rscm
 *
 * The RSCM is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.aspectsense.rscm.reasoner.location_home_or_work.data;

import java.util.Date;

/**
 * User: Nearchos Paspallis
 * Date: 5/9/13
 * Time: 6:41 PM
 */
public class Coordinates
{
    public static final int TIME_ELSEWHERE = 0x00;
    public static final int TIME_AT_WORK   = 0x01;
    public static final int TIME_AT_HOME   = 0x02;

    private final long timestamp;
    private final int where; // indicates if at home, work or elsewhere
    private final float lat;
    private final float lng;

    public Coordinates(final long timestamp, final int where, final float lat, final float lng)
    {
        this.timestamp = timestamp;
        this.where = where;
        this.lat = lat;
        this.lng = lng;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getWhere()
    {
        return where;
    }

    public String getWhereAsString()
    {
        if(where == TIME_ELSEWHERE)
        {
            return "elsewhere";
        }
        else if(where == TIME_AT_WORK)
        {
            return "work";
        }
        else // where == TIME_AT_HOME
        {
            return "home";
        }
    }

    public float getLat()
    {
        return lat;
    }

    public float getLng()
    {
        return lng;
    }

    @Override public String toString()
    {
        return "(" + lat + ", " + lng + ") @ " + new Date(timestamp) + " (" + getWhereAsString() + ")";
    }
}