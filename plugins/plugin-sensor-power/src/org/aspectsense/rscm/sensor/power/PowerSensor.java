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

package org.aspectsense.rscm.sensor.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * Date: 4/30/12
 * Time: 3:36 PM
 */
public class PowerSensor extends SensorService
{
    public static final String TAG = "org.aspectsense.rscm.sensor.power.PowerSensor";

    public static final IntentFilter filter = new IntentFilter();
    static
    {
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
    }

    @Override public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "PowerSensor: registerReceiver");
        registerReceiver(powerReceiver, filter);

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "PowerSensor: unregisterReceiver");
        unregisterReceiver(powerReceiver);

        return super.onUnbind(intent);
    }

    public static final String SCOPE_POWER_CONNECTED = "power.connected";

    final BroadcastReceiver powerReceiver = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "intent is " + intent);

            if(Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()))
            {
                notifyListener(ContextValue.createContextValue(SCOPE_POWER_CONNECTED, true));
            }
            else if(Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction()))
            {
                notifyListener(ContextValue.createContextValue(SCOPE_POWER_CONNECTED, false));
            }
        }
    };
}