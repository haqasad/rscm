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