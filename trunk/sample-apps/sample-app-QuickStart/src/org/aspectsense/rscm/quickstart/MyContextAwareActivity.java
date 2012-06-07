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
    @Override public String[] getRequestedScopes()
    {
        return new String[] { "battery.level" };
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
            appendMessage(new Date() + ": The battery level is " + contextValue.getValueAsInteger() + "%");
        }
        catch (JSONException jsone)
        {
            Toast.makeText(this, "Error while displaying context event: " + contextValue, Toast.LENGTH_SHORT).show();
        }
    }
}