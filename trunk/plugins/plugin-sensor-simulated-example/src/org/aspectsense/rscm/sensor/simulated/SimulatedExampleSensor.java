package org.aspectsense.rscm.sensor.simulated;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * Date: 12/07/13
 * Time: 16:14
 */
public class SimulatedExampleSensor extends SensorService
{
    public static final String TAG = "org.aspectsense.rscm.sensor.simulated.SimulatedExampleSensor";

    public static final String SCOPE_SIMULATED_EXAMPLE = "simulated.example";

    public static final String ACTION_SIMULATED_EXAMPLE = "ACTION_SIMULATED_EXAMPLE";

    public static final IntentFilter filter = new IntentFilter(ACTION_SIMULATED_EXAMPLE);

    @Override public IBinder onBind(Intent intent)
    {
        Toast.makeText(this, "Starting simulated example sensor", Toast.LENGTH_SHORT).show();
        registerReceiver(simulatedEventReceiver, filter);

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        Toast.makeText(this, "Stopping simulated example sensor", Toast.LENGTH_SHORT).show();
        unregisterReceiver(simulatedEventReceiver);

        return super.onUnbind(intent);
    }

    final BroadcastReceiver simulatedEventReceiver = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            final String value = intent.getExtras().getString("value");
            final ContextValue contextValueBatteryTemp = ContextValue.createContextValue(SCOPE_SIMULATED_EXAMPLE, value);
            notifyListener(contextValueBatteryTemp);
        }
    };
}
