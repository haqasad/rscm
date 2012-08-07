package org.aspectsense.rscm.battery_predictor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import org.aspectsense.rscm.battery_predictor.db.DatabaseHelper;

import static org.aspectsense.rscm.battery_predictor.db.DatabaseMetadata.ContextValuesTableMetadata;

public class ActivityHome extends Activity
{
    private Button startStopServiceButton;
    private Button exportButton;
    private Button refreshButton;
    private ListView contextListView;

    private DatabaseHelper databaseHelper;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        this.databaseHelper = DatabaseHelper.getDatabaseHelper(this);

        startStopServiceButton = (Button) findViewById(R.id.start_stop_button);
        startStopServiceButton.setText(isBackgroundServiceRunning() ? R.string.Stop : R.string.Start);
        startStopServiceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                if(isBackgroundServiceRunning())
                {
                    // stop service
                    stopService(new Intent(ActivityHome.this, BackgroundService.class));
                    startStopServiceButton.setText(R.string.Start);
                }
                else
                {
                    // start service
                    startService(new Intent(ActivityHome.this, BackgroundService.class));
                    startStopServiceButton.setText(R.string.Stop);
                }
            }
        });

        refreshButton = (Button) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                updateListView();
            }
        });
        contextListView = (ListView) findViewById(R.id.context_list_view);
    }

    @Override protected void onResume()
    {
        super.onResume();

        updateListView();
    }

    private void updateListView()
    {
        Toast.makeText(this, "Updating the context values list", Toast.LENGTH_SHORT).show();

        final Cursor contextValuesCursor = databaseHelper.getContextValuesCursor();
        final SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.view_context_value,
                contextValuesCursor,
                new String[] {ContextValuesTableMetadata.TIMESTAMP, ContextValuesTableMetadata.BATTERY_LEVEL, ContextValuesTableMetadata.POWER_CONNECTED, ContextValuesTableMetadata.LATITUDE, ContextValuesTableMetadata.LONGITUDE},
                new int[] { R.id.view_context_value_timestamp, R.id.view_context_value_battery_level, R.id.view_context_value_power_connected, R.id.view_context_value_latitude, R.id.view_context_value_longitude},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        contextListView.setAdapter(simpleCursorAdapter);
    }

    private boolean isBackgroundServiceRunning()
    {
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (final ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE))
        {
            if (BackgroundService.class.getCanonicalName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}