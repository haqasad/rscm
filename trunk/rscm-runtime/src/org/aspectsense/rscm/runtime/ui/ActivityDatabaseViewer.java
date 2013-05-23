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

package org.aspectsense.rscm.runtime.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.runtime.R;
import org.aspectsense.rscm.runtime.db.DatabaseHelper;

import java.util.Arrays;

/**
 * User: Nearchos Paspallis
 * Date: 5/1/13
 * Time: 6:20 PM
 */
public class ActivityDatabaseViewer extends Activity
{
    private Spinner scopeSelector;
    private ListView contextValuesListView;

    DatabaseHelper databaseHelper;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_viewer);

        databaseHelper = DatabaseHelper.getDatabaseHelper(this);

        scopeSelector = (Spinner) findViewById(R.id.activity_database_viewer_scope_selector);
        contextValuesListView = (ListView) findViewById(R.id.activity_database_viewer_list_view);
    }

    @Override protected void onResume()
    {
        super.onResume();

        final String [] scopes = databaseHelper.getDistinctScopes();

        scopeSelector.setAdapter(new ArrayAdapter<String>(this, R.layout.scope_row, scopes));
        scopeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                updateContextValuesList(scopes[position]);
            }

            @Override public void onNothingSelected(AdapterView<?> parent)
            {
                // nothing
            }
        });
    }

    private void updateContextValuesList(final String scope)
    {
        final ContextValue [] contextValues = databaseHelper.getAllValues(scope);
        final ContextValueListAdapter contextValueListAdapter = new ContextValueListAdapter(this, contextValues);
        contextValuesListView.setAdapter(contextValueListAdapter);
    }
}