package org.aspectsense.rscm.runtime.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import org.aspectsense.rscm.*;
import org.aspectsense.rscm.runtime.R;
import org.aspectsense.rscm.runtime.db.DatabaseHelper;

import java.util.List;

/**
 * User: Nearchos Paspallis
 * Date: 5/23/13
 * Time: 1:24 PM
 */
public class ActivityContextEmulator extends Activity
{
    public static final String TAG = "org.aspectsense.rscm.runtime.ActivityContextEmulator";

    private Spinner scopeSelector;
    private EditText valueEditText;

    DatabaseHelper databaseHelper;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_emulator);

        databaseHelper = DatabaseHelper.getDatabaseHelper(this);

        scopeSelector = (Spinner) findViewById(R.id.activity_context_emulator_scope_selector);
        valueEditText = (EditText) findViewById(R.id.activity_context_emulator_value);
    }

    private String [] scopes;

    @Override protected void onResume()
    {
        super.onResume();

        scopes = databaseHelper.getDistinctScopes();

        scopeSelector.setAdapter(new ArrayAdapter<String>(this, R.layout.scope_row, scopes));
        scopeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                updateValueEditText();
            }

            @Override public void onNothingSelected(AdapterView<?> parent)
            {
                // nothing
            }
        });
    }

    private void updateValueEditText()
    {
        final String scope = scopes[scopeSelector.getSelectedItemPosition()];
        try
        {
            final ContextValue contextValue = contextAccess.getLastContextValue(scope);
            if(contextValue != null)
            {
                valueEditText.setText(contextValue.getValueAsJSONString());
            }
            else
            {
                valueEditText.setText("{\n}");
            }
        }
        catch(RemoteException re)
        {
            Log.e(TAG, re.getMessage(), re);
        }
    }

    @Override protected void onStart()
    {
        super.onResume();
        bindService(new Intent(IContextManagement.class.getName()), mContextManagementConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(IContextAccess.class.getName()), mContextAccessConnection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onStop()
    {
        super.onPause();
        unbindService(mContextManagementConnection);
        unbindService(mContextAccessConnection);
    }

    private IContextManagement contextManagement;
    private IContextAccess contextAccess;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mContextManagementConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            contextManagement = IContextManagement.Stub.asInterface(iBinder);
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            contextManagement = null;
        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mContextAccessConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            contextAccess = IContextAccess.Stub.asInterface(iBinder);
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            contextAccess = null;
        }
    };
}