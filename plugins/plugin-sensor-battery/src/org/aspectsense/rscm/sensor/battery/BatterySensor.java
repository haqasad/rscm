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

package org.aspectsense.rscm.sensor.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 *         Date: 3/8/12
 *         Time: 9:24 PM
 */
public class BatterySensor extends SensorService
{
    public static final String TAG = "org.aspectsense.rscm.sensor.battery.BatterySensor";

    public static final String SCOPE_BATTERY_LEVEL      = "battery.level";
    public static final String SCOPE_BATTERY_VOLTAGE    = "battery.voltage";
    public static final String SCOPE_BATTERY_TEMP       = "battery.temp";

    public static final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    @Override public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "BatterySensor: registerReceiver");
        registerReceiver(batteryReceiver, filter);

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "BatterySensor: unregisterReceiver");
        unregisterReceiver(batteryReceiver);

        return super.onUnbind(intent);
    }

    final BroadcastReceiver batteryReceiver = new BroadcastReceiver()
    {
        int scale = -1;
        int level = -1;
        int voltage = -1;
        int temp = -1;

        @Override public void onReceive(Context context, Intent intent)
        {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            Log.d(TAG, "level is " + level + " / " + scale + ", temperature is " + temp + ", voltage is " + voltage);

            final ContextValue lastContextValueBatteryLevel = ContextValue.createContextValue(SCOPE_BATTERY_LEVEL, level);
            final ContextValue lastContextValueBatteryVoltage = ContextValue.createContextValue(SCOPE_BATTERY_VOLTAGE, voltage);
            final ContextValue lastContextValueBatteryTemp = ContextValue.createContextValue(SCOPE_BATTERY_TEMP, temp);

            notifyListener(lastContextValueBatteryLevel);
            notifyListener(lastContextValueBatteryTemp);
            notifyListener(lastContextValueBatteryVoltage);
        }
    };
}