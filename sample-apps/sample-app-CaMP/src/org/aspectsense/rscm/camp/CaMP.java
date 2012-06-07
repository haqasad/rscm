package org.aspectsense.rscm.camp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.client.ContextListenerActivity;
import org.json.JSONException;

/**
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 *         Date: 3/8/12
 *         Time: 5:05 PM
 */
public class CaMP extends ContextListenerActivity
{
    public static final String TAG = "org.aspectsense.rscm.camp.CaMP";

    public static final String [] REQUESTED_SCOPES = new String[] {
            "user.activity"
    };

    private TextView messageTextView;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camp);

        messageTextView = (TextView) findViewById(R.id.camp_message);
        appendMessage("Not attached");
    }

    private void appendMessage(final String message)
    {
        final String currentMessage = messageTextView.getText().toString();
        messageTextView.setText(currentMessage + "\n" + message);
    }

    @Override public String[] getRequestedScopes()
    {
        return REQUESTED_SCOPES;
    }

    @Override public void onContextValueChanged(ContextValue contextValue)
    {
        try
        {
            appendMessage(contextValue.getValueAsString());
        }
        catch (JSONException jsone)
        {
            Toast.makeText(this, "Error while displaying context event: " + contextValue, Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onContextServiceConnected()
    {
        super.onContextServiceConnected();
        appendMessage("Attached");
    }

    @Override protected void onContextServiceDisconnected()
    {
        super.onContextServiceConnected();
        appendMessage("Not attached");
    }
}