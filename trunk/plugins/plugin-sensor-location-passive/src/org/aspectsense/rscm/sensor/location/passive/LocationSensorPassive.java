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

package org.aspectsense.rscm.sensor.location.passive;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * Date: 4/12/12
 * Time: 8:19 PM
 */
public class LocationSensorPassive extends SensorService implements LocationListener
{
    public static final String TAG = "org.aspectsense.rscm.sensor.location.fine.LocationSensorPassive";

    public static final String SCOPE_LOCATION_PASSIVE = "location.passive";

    private LocationManager locationManager;

    @Override public void onCreate()
    {
        super.onCreate();
        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    public static final long MIN_TIME = 600000L;  // 10 minutes
    public static final float MIN_DISTANCE = 100; // 100 meters

    @Override public IBinder onBind(Intent intent)
    {
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
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
        final ContextValue lastContextValue = ContextValue.createContextValue(SCOPE_LOCATION_PASSIVE, coordinatesAsJSONString);
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