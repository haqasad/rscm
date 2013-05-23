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

package org.aspectsense.rscm.battery_predictor;

import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.IContextListener;
import org.aspectsense.rscm.battery_predictor.db.DatabaseHelper;
import org.aspectsense.rscm.battery_predictor.db.DatabaseMetadata;
import org.json.JSONException;
import org.json.JSONObject;

import static org.aspectsense.rscm.battery_predictor.db.DatabaseMetadata.ContextValuesTableMetadata;

/**
 * Date: 8/7/12
 * Time: 8:45 AM
 */
public class BackgroundService extends Service
{
    public static final String TAG = "org.aspectsense.rscm.battery_predictor.BackgroundService";

    public static final String BATTERY_LEVEL = "battery.level";
    public static final String POWER_CONNECTED = "power.connected";
    public static final String LOCATION_BACKGROUND = "location.background";

    public static final String [] REQUESTED_SCOPES = new String[] {
            BATTERY_LEVEL,
            POWER_CONNECTED,
            LOCATION_BACKGROUND
    };

    private String [] getRequestedScopes()
    {
        return REQUESTED_SCOPES;
    }

    private DatabaseHelper databaseHelper;

    private int lastBatteryLevel = -1;
    private int lastPowerConnected = -1;
    private double lastLat = Double.NaN;
    private double lastLng = Double.NaN;

    public void onContextValueChanged(ContextValue contextValue)
    {
        // insert in DB
//        Toast.makeText(this, "New context value!\n" + contextValue, Toast.LENGTH_SHORT).show();

        final long timestamp = System.currentTimeMillis();

        try
        {
            if(BATTERY_LEVEL.equals(contextValue.getScope()))
            {
                final int newBatteryLevel = contextValue.getValueAsInteger();
                if(newBatteryLevel != lastBatteryLevel)
                {
                    lastBatteryLevel = newBatteryLevel;
                    databaseHelper.insert(timestamp, lastBatteryLevel, lastPowerConnected, lastLat, lastLng);
                }
            }
            else if(POWER_CONNECTED.equals(contextValue.getScope()))
            {
                final int newPowerConnected = contextValue.getValueAsBoolean() ? DatabaseMetadata.ContextValuesTableMetadata.POWER_CONNECTED_TRUE : DatabaseMetadata.ContextValuesTableMetadata.POWER_CONNECTED_FALSE;
                if(newPowerConnected != lastPowerConnected)
                {
                    lastPowerConnected = newPowerConnected;
                    databaseHelper.insert(timestamp, lastBatteryLevel, lastPowerConnected, lastLat, lastLng);
                }
            }
            else if(LOCATION_BACKGROUND.equals(contextValue.getScope()))
            {
                final String valueAsJSON = contextValue.getValueAsString();
                final JSONObject jsonObject = new JSONObject(valueAsJSON);
                final double newLat = jsonObject.getDouble("lat");
                final double newLng = jsonObject.getDouble("lng");
                if(newLat != lastLat || newLng != lastLng)
                {
                    lastLat = newLat;
                    lastLng = newLng;
                    databaseHelper.insert(timestamp, lastBatteryLevel, lastPowerConnected, lastLat, lastLng);
                }
            }
        }
        catch (JSONException jsone)
        {
            Log.e(TAG, jsone.getMessage(), jsone);
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "Starting service: " + intent);
//        Toast.makeText(this, "Starting service: " + intent, Toast.LENGTH_SHORT).show();

        databaseHelper = DatabaseHelper.getDatabaseHelper(this);

        // initialize the context
        lastBatteryLevel = getBatteryLevel();
        lastPowerConnected = isConnected(this) ? ContextValuesTableMetadata.POWER_CONNECTED_TRUE : ContextValuesTableMetadata.POWER_CONNECTED_FALSE;
//        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        final Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//        lastLat = location != null ? location.getLatitude() : -1d;
//        lastLng = location != null ? location.getLongitude() : -1d;

        databaseHelper.insert(System.currentTimeMillis(), lastBatteryLevel, lastPowerConnected, lastLat, lastLng);

        bindService(new Intent(IContextAccess.class.getName()), mConnection, Context.BIND_AUTO_CREATE);

        // return START_STICKY to make sure the service stays active even after exiting the activity
        return START_STICKY;
    }

    private int getBatteryLevel()
    {
        final Intent batteryIntent = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return  batteryIntent.getIntExtra("level", -1);
    }

    private boolean isConnected(Context context)
    {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    @Override public void onDestroy()
    {
        Log.d(TAG, "Stopping service");
        Toast.makeText(this, "Stopping service", Toast.LENGTH_SHORT).show();

        unregisterContextTypes();
        unbindService(mConnection);
        super.onDestroy();
    }

    private void registerContextTypes()
    {
        final String [] requestedScopes = getRequestedScopes();

        if(requestedScopes != null)
        {
            for(final String scope : requestedScopes)
            {
                onRequestContextUpdates(scope);
            }
        }
    }

    private void unregisterContextTypes()
    {
        final String [] requestedScopes = getRequestedScopes();

        if(requestedScopes != null)
        {
            for(final String scope : requestedScopes)
            {
                onRemoveContextUpdates(scope);
            }
        }
    }

    private IContextAccess contextAccess = null;

    protected boolean onRequestContextUpdates(final String scope)
    {
        if(contextAccess != null)
        {
            try
            {
                Log.d(TAG, "$*** requestContextUpdates(" + scope + ", " + asynchronousContextListener + ")");
                contextAccess.requestContextUpdates(scope, asynchronousContextListener);
                return true;
            }
            catch (RemoteException re)
            {
                Log.e(TAG, "onRequestContextUpdates: " + re);
            }
        }
        return false;
    }

    protected boolean onRemoveContextUpdates(final String scope)
    {
        if(contextAccess != null)
        {
            try
            {
                Log.d(TAG, "$*** removeContextUpdates(" + scope + ", " + asynchronousContextListener + ")");
                contextAccess.removeContextUpdates(scope, asynchronousContextListener);
                return true;
            }
            catch (RemoteException re)
            {
                Log.e(TAG, "onRemoveContextUpdates: " + re);
            }
        }
        return false;
    }

    /**
     * Class for interacting with the activity_home interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            Log.d(TAG, "$*** onServiceConnected(" + componentName + ")");
            contextAccess = IContextAccess.Stub.asInterface(iBinder);
            registerContextTypes();
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            Log.d(TAG, "$*** onServiceDisconnected(" + componentName + ")");
            contextAccess = null;
        }
    };

    public static final int CONTEXT_VALUE_TYPE = 0x9000;

    private final IContextListener asynchronousContextListener = new IContextListener.Stub()
    {
        @Override public void onContextValueChanged(ContextValue contextValue) throws RemoteException
        {
            Log.d(TAG, "IContextListener.onContextValueChanged: " + contextValue);
            handler.sendMessage(handler.obtainMessage(CONTEXT_VALUE_TYPE, contextValue));
        }
    };

    private Handler handler = new Handler()
    {
        @Override public void handleMessage(Message msg)
        {
            Log.d(TAG, "handler.handleMessage: " + msg);
            switch (msg.what)
            {
                case CONTEXT_VALUE_TYPE:
                    final ContextValue contextValue = (ContextValue) msg.obj;
                    BackgroundService.this.onContextValueChanged(contextValue);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };

    public IBinder onBind(Intent intent)
    {
        return null;
    }
}