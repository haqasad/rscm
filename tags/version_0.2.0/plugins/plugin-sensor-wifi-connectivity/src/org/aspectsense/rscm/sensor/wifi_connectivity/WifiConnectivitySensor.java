package org.aspectsense.rscm.sensor.wifi_connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * Todo: Verify this is correct
 * Todo: See: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
 *
 * Date: 4/30/12
 * Time: 3:36 PM
 */
public class WifiConnectivitySensor extends SensorService
{
    public static final String TAG = "org.aspectsense.rscm.sensor.wifi_connectivity.WifiConnectivitySensor";

    public static final String ACTION_NETWORK_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    public static final IntentFilter filter = new IntentFilter(ACTION_NETWORK_CONNECTIVITY_CHANGE);

    @Override public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "WifiConnectivitySensor: registerReceiver");
        registerReceiver(wifiConnectivityReceiver, filter);

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "WifiConnectivitySensor: unregisterReceiver");
        unregisterReceiver(wifiConnectivityReceiver);

        return super.onUnbind(intent);
    }

    public static final String SCOPE_NETWORK_WIFI_CONNECTED = "network.wifi_connected";

    final BroadcastReceiver wifiConnectivityReceiver = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "intent is " + intent);

            if(ACTION_NETWORK_CONNECTIVITY_CHANGE.equals(intent.getAction()))
            {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                notifyListener(ContextValue.createContextValue(SCOPE_NETWORK_WIFI_CONNECTED, wifiManager.isWifiEnabled()));
            }
        }
    };
}