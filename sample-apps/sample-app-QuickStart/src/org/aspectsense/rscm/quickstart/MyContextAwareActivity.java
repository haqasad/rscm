package org.aspectsense.rscm.quickstart;

import android.os.Bundle;
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

    private TextView messageTextView;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        messageTextView = new TextView(this);
        setContentView(messageTextView);
        appendMessage("Activity created");
    }

    private void appendMessage(final String message)
    {
        final String currentMessage = messageTextView.getText().toString();
        messageTextView.setText(currentMessage + "\n" + message);
    }

    @Override public void onContextValueChanged(ContextValue contextValue)
    {
        try
        {
            if(BATTERY_LEVEL.equals(contextValue.getScope()))
            {
                int batteryLevel = contextValue.getValueAsInteger();
                appendMessage(new Date() + ": The battery level is " + batteryLevel + "%");
            }
            else if(POWER_CONNECTED.equals(contextValue.getScope()))
            {
                boolean connected = contextValue.getValueAsBoolean();
                appendMessage(new Date() + ": The power status is " + (connected ? "connected" : "disconnected"));
            }
        }
        catch (JSONException jsone)
        {
            Toast.makeText(this, "Error while displaying context event: " + contextValue, Toast.LENGTH_SHORT).show();
        }
    }
}