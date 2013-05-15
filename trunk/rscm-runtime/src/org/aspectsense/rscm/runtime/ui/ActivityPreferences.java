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

package org.aspectsense.rscm.runtime.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.aspectsense.rscm.runtime.R;

/**
 * User: Nearchos Paspallis
 * Date: 4/30/13
 * Time: 3:02 PM
 */
public class ActivityPreferences extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Button showPluginView = (Button) findViewById(R.id.button_show_plugins_viewer);
        showPluginView.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                startActivity(new Intent(ActivityPreferences.this, ActivityPluginViewer.class));
            }
        });

        Button showDatabaseView = (Button) findViewById(R.id.button_show_database_viewer);
        showDatabaseView.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                startActivity(new Intent(ActivityPreferences.this, ActivityDatabaseViewer.class));
            }
        });
    }
}