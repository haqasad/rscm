/*
 * Really Simple Context Middleware (RSCM)
 *
 * Copyright (c) 2013 The RSCM Team
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

package org.aspectsense.rscm.reasoner.location_home_or_work.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import org.aspectsense.rscm.reasoner.location_home_or_work.LocationHomeOrWorkReasoner;
import org.aspectsense.rscm.reasoner.location_home_or_work.R;
import org.aspectsense.rscm.reasoner.location_home_or_work.data.Coordinates;
import org.aspectsense.rscm.reasoner.location_home_or_work.db.DatabaseHelper;

/**
 * User: Nearchos Paspallis
 * Date: 5/9/13
 * Time: 7:50 PM
 */
public class ActivityPreferences extends Activity
{
    private Button startStopButton;
    private Button refreshButton;
    private Button clearButton;
    private Button exportButton;
    private ListView listView;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        startStopButton = (Button) findViewById(R.id.start_stop_button);
        refreshButton = (Button) findViewById(R.id.refresh_button);
        clearButton = (Button) findViewById(R.id.clear_button);
        exportButton = (Button) findViewById(R.id.export);
        listView = (ListView) findViewById(R.id.list_view);

        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                refreshList();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                DatabaseHelper.getDatabaseHelper(ActivityPreferences.this).cleanup(System.currentTimeMillis());
                refreshList();
            }
        });

        startStopButton.setText(isBackgroundServiceRunning() ? R.string.Stop : R.string.Start);
        startStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                if(isBackgroundServiceRunning())
                {
                    // stop service
                    stopService(new Intent(ActivityPreferences.this, BackgroundService.class));
                    startStopButton.setText(R.string.Start);
                }
                else
                {
                    // start service
                    startService(new Intent(ActivityPreferences.this, BackgroundService.class));
                    startStopButton.setText(R.string.Stop);
                }
            }
        });    }

    @Override protected void onResume()
    {
        super.onResume();

        refreshList();
    }

    private void refreshList()
    {
        final long now = System.currentTimeMillis();
        final Coordinates [] coordinates = DatabaseHelper.getDatabaseHelper(this).getCoordinates(now - LocationHomeOrWorkReasoner.TWO_WEEKS, now);

        exportButton.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                export(coordinates);
            }
        });

        listView.setAdapter(new CoordinatesListAdapter(this, coordinates));
    }

    private void export(final Coordinates [] coordinates)
    {
        // form JSON string
        final StringBuilder stringBuilder = new StringBuilder("{ \n")
                                                      .append("  \"values\": [\n");
        for(int i = 0; i < coordinates.length; i++)
        {
            final Coordinates myCoordinates = coordinates[i];
            final char where = myCoordinates.getWhere() == Coordinates.TIME_AT_HOME ? 'H'
                    : myCoordinates.getWhere() == Coordinates.TIME_AT_WORK ? 'W'
                    : 'E';

            stringBuilder.append("    {\n");
            stringBuilder.append("      \"t\": ").append(myCoordinates.getTimestamp()).append(",\n");
            stringBuilder.append("      \"w\": \"").append(where).append("\",\n");
            stringBuilder.append("      \"lat\": ").append(myCoordinates.getLat()).append(",\n");
            stringBuilder.append("      \"lng\": ").append(myCoordinates.getLng()).append("\n");
            stringBuilder.append("    }");
            if(i < coordinates.length - 1)
            {
                stringBuilder.append(",\n");
            }
            else
            {
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("  ]\n}");

        // share it
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
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