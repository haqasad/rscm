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

package org.aspectsense.rscm.quickstart;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.client.ContextListenerActivity;
import org.json.JSONException;

import java.util.Date;

public class MyContextAwareActivity extends ContextListenerActivity
{
    public static final String BATTERY_LEVEL = "battery.level";
    public static final String POWER_CONNECTED = "power.connected";

    @Override public String[] getRequestedScopes()
    {
        return new String[] { BATTERY_LEVEL, POWER_CONNECTED };
    }

    private TextView batteryProgressLabel;
    private ProgressBar batteryProgressBar;
    private CheckBox connectedCheckBox;
    private TextView logMessageTextView;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        batteryProgressLabel = (TextView) findViewById(R.id.battery_progress_label);
        batteryProgressBar = (ProgressBar) findViewById(R.id.battery_progress_bar);
        connectedCheckBox = (CheckBox) findViewById(R.id.connected_check_box);
        logMessageTextView = (TextView) findViewById(R.id.log_message);

        appendMessage("Activity created");
    }

    private void appendMessage(final String message)
    {
        final String currentMessage = logMessageTextView.getText().toString();
        logMessageTextView.setText(currentMessage + "\n" + message);
    }

    @Override public void onContextValueChanged(ContextValue contextValue)
    {
        try
        {
            if(BATTERY_LEVEL.equals(contextValue.getScope()))
            {
                int batteryLevel = contextValue.getValueAsInteger();
                batteryProgressLabel.setText(batteryLevel + "%");
                batteryProgressBar.setProgress(batteryLevel);
                appendMessage(new Date() + ": The battery level is " + batteryLevel + "%");
            }
            else if(POWER_CONNECTED.equals(contextValue.getScope()))
            {
                boolean connected = contextValue.getValueAsBoolean();
                connectedCheckBox.setChecked(connected);
                connectedCheckBox.setText(connected ? R.string.Plugged_in : R.string.Unplugged);
                appendMessage(new Date() + ": The power status is " + (connected ? "connected" : "disconnected"));
            }
        }
        catch (JSONException jsone)
        {
            Toast.makeText(this, "Error while displaying context event: " + contextValue, Toast.LENGTH_SHORT).show();
        }
    }
}