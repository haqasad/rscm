/*
 * Really Simple Context Middleware (RSCM)
 *
 * Copyright (c) 2012-2013 The RSCM Team
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

package org.aspectsense.rscm.reasoner.location_home_or_work;

import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.ReasonerService;
import org.aspectsense.rscm.reasoner.location_home_or_work.data.Coordinates;
import org.aspectsense.rscm.reasoner.location_home_or_work.db.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import static org.aspectsense.rscm.reasoner.location_home_or_work.data.Coordinates.*;

import java.util.*;

/**
 * User: Nearchos Paspallis
 * Date: 4/29/13
 * Time: 10:26 AM
 */
public class LocationHomeOrWorkReasoner extends ReasonerService
{
    public static final String TAG = "org.aspectsense.rscm.reasoner.location_home_or_work.LocationHomeOrWorkReasoner";

    public static final long TWO_WEEKS = 2L * 7 * 24 * 60 * 60 * 1000L;

    public static final String SCOPE_LOCATION_FINE = "location.fine";
    public static final String SCOPE_LOCATION_COARSE = "location.coarse";
    public static final String SCOPE_LOCATION_PASSIVE = "location.passive";
    public static final String SCOPE_LOCATION_HOME_OR_WORK = "location.home_or_work";

    private double lastLat = Double.NaN;
    private double lastLng = Double.NaN;

    @Override protected void onContextValueChanged(ContextValue contextValue)
    {
        if(contextValue == null) return;
Log.d(TAG, "onContextValueChanged: " + contextValue.toString());// todo

        final long now = System.currentTimeMillis();
        final int where = timeWhere(now);

//        if(TIME_ELSEWHERE == where) todo enable in production
//        {
//            // just ignore the data
//            return;
//        }

        try
        {
            final String scope = contextValue.getScope();

            if(SCOPE_LOCATION_FINE.equals(scope) || SCOPE_LOCATION_COARSE.equals(scope) || SCOPE_LOCATION_PASSIVE.equals(scope))
            {
                final String valueAsJSON = contextValue.getValueAsString();
                final JSONObject jsonObject = new JSONObject(valueAsJSON);
                final double newLat = jsonObject.getDouble("lat");
                final double newLng = jsonObject.getDouble("lng");
                if(newLat != lastLat || newLng != lastLng)
                {
                    lastLat = newLat;
                    lastLng = newLng;
                }

                if(!Double.isNaN(newLat) && !Double.isNaN(newLng))
                {
                    final DatabaseHelper databaseHelper = DatabaseHelper.getDatabaseHelper(this);
                    databaseHelper.insert(now, where, newLat, newLng);

                    // delete old (obsolete) data (i.e. older than 2 weeks)
                    databaseHelper.cleanup(now - TWO_WEEKS);

//                    ContextValue locationContextValue = ContextValue.createContextValue(SCOPE_LOCATION_HOME_OR_WORK, "home");
//                    notifyListener(locationContextValue);
                }

                // todo consider algorithm at:
                // http://stackoverflow.com/questions/8145590/clustering-2d-integer-coordinates-into-sets-of-at-most-n-points
                // http://link.springer.com/content/pdf/10.1007%2Fs11063-004-2793-y.pdf
                // http://www.youtube.com/watch?v=TYxwjO4TsvQ
            }
        }
        catch (JSONException jsone)
        {
            Log.e(TAG, jsone.getMessage(), jsone);
        }
    }

    private Set<Set<Coordinates>> groupIntoClusters(final Coordinates [] coordinates)
    {
        final Set<Set<Coordinates>> spots = new HashSet<Set<Coordinates>>();


        return spots;
    }

    public static final int WORK_HOURS_START = 10;
    public static final int WORK_HOURS_STOP  = 16;

    public static final int HOME_HOURS_START = 20;
    public static final int HOME_HOURS_STOP  = 7;

    private int timeWhere(final long timestamp)
    {

        // find the day of week
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        // check if weekend
        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        final boolean weekend = Calendar.SATURDAY == dayOfWeek || Calendar.SUNDAY == dayOfWeek;

        final int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        final boolean homeHour = hourOfDay >= HOME_HOURS_START || hourOfDay <= HOME_HOURS_STOP;
        if(weekend)
        {
            // check if home hour
            if(homeHour)
            {
                return TIME_AT_HOME;
            }
            else
            {
                return TIME_ELSEWHERE;
            }
        }
        else
        {
            // check if work hour
            final boolean workHour = hourOfDay >= WORK_HOURS_START && hourOfDay <= WORK_HOURS_STOP;
            if(workHour)
            {
                return TIME_AT_WORK;
            }
            else if(homeHour)
            {
                return TIME_AT_HOME;
            }
            else
            {
                return TIME_ELSEWHERE;
            }
        }
    }
}