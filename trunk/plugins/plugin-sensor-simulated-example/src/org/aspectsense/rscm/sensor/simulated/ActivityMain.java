package org.aspectsense.rscm.sensor.simulated;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Date: 12/07/13
 * Time: 16:30
 */
public class ActivityMain extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        final EditText editTextContextValue = (EditText) findViewById(R.id.editTextContextValue);
        final String contextValue = editTextContextValue.getText().toString();
        findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Intent simulatedContextEvent = new Intent(SimulatedExampleSensor.ACTION_SIMULATED_EXAMPLE);
                simulatedContextEvent.putExtra("value", contextValue);
                sendBroadcast(simulatedContextEvent);
                Toast.makeText(ActivityMain.this, "Sending broadcast: " + contextValue, Toast.LENGTH_SHORT).show();
            }
        });
    }
}