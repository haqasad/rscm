/*
 * Really Simple Context Middleware (RCSM)
 *
 * Copyright (c) 2012 The RCSM Team
 *
 * This file is part of the RCSM: the Really Simple Context Middleware for ANDROID. More information about the project
 * is available at: http://code.google.com/p/rscm
 *
 * The RCSM is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
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

package org.aspectsense.rscm.sensor.location.coarse;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * Date: 4/12/12
 * Time: 8:19 PM
 */
public class LocationSensorCoarse extends SensorService implements LocationListener
{
    public static final String TAG = "org.aspectsense.rscm.sensor.location.coarse.LocationSensorCoarse";

    public static final String SCOPE_LOCATION_COARSE    = "location.coarse";

    private LocationManager locationManager;

    @Override public void onCreate()
    {
        super.onCreate();
        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    public static final long MIN_TIME = 10000L;  // 10 seconds
    public static final float MIN_DISTANCE = 10; // 10 meters

    @Override public IBinder onBind(Intent intent)
    {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        locationManager.removeUpdates(this);
        return super.onUnbind(intent);
    }

    @Override public void onLocationChanged(Location location)
    {
        final String coordinatesAsJSONString = "{ lat: " + location.getLatitude() + ", lng: " + location.getLongitude() + " }";
        final ContextValue lastContextValue = new ContextValue(SCOPE_LOCATION_COARSE, coordinatesAsJSONString);
        notifyListener(lastContextValue);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // ignore
    }

    @Override public void onProviderEnabled(String provider)
    {
        // ignore
    }

    @Override public void onProviderDisabled(String provider)
    {
        // ignore
    }
}