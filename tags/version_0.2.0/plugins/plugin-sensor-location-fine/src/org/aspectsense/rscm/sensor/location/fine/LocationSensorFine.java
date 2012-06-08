package org.aspectsense.rscm.sensor.location.fine;

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
public class LocationSensorFine extends SensorService implements LocationListener
{
    public static final String TAG = "org.aspectsense.rscm.sensor.location.fine.LocationSensorFine";

    public static final String SCOPE_LOCATION_FINE      = "location.fine";

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
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
        final ContextValue lastContextValue = ContextValue.createContextValue(SCOPE_LOCATION_FINE, coordinatesAsJSONString);
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