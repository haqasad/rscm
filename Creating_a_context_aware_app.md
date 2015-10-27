# Introduction #

The RSCM is a middleware for creating context-aware apps for ANDROID, using dynamically discovered components (i.e. context plugins)

# Basics #

Just like when you use the core services for ANDROID for getting context data (say location), similarly in RSCM you need to follow these steps:
  1. Get access to the context service
  1. Tell the context service the data you are interested in
  1. Implement the callback that will handle the notifications
  1. Ensure that the app's Manifest file includes the required permissions

# Example #

These steps are better illustrated with an example. This example creates a trivial app that simply displays the received context changes on the screen. Here are the required steps:

## 1. Create a new project ##

Using the IDE of your choice, create a new ANDROID project. Make sure that the project includes the _RSCM library_ as an external library.

## 2. Create a context-aware Activity ##

Create a new [Activity](http://developer.android.com/guide/topics/fundamentals/activities.html). Edit your activity so that it _extends_ the [org.aspectsense.rscm.context.client.ContextListenerActivity.java](http://code.google.com/p/rscm/source/browse/trunk/rscm-library/src/org/aspectsense/rscm/context/client/ContextListenerActivity.java) instead of the [android.app.Activity](http://developer.android.com/reference/android/app/Activity.html) class. The extended class is _abstract_ and requires the definition of two methods:
  1. `public String[] getRequestedScopes();`
  1. `public void onContextValueChanged(ContextValue contextValue);`

The resulting code should look like this:
```
package org.aspectsense.rscm.quickstart;

import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.client.ContextListenerActivity;

public class MyContextAwareActivity extends ContextListenerActivity
{
    @Override public String[] getRequestedScopes()
    {
        return new String[0]; // todo
    }

    @Override public void onContextValueChanged(ContextValue contextValue)
    {
        // todo
    }
}
```

## 3. Realize the `getRequestedScopes()` method ##

This method is used to define the scopes of the requested context types. In this example, let's just assume we are interested in the _battery.level_ scope. The code is edited as follows:

```
    @Override public String[] getRequestedScopes()
    {
        return new String[] { "battery.level" };
    }
```

## 4. Realize the `onContextValueChanged()` callback method ##

This callback method is automatically invoked by RSCM whenever there is a context change for any of the requested scopes. In this example, we simply display the time along with received context value on the phone's display.
```
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
```

The complete code, with a [TextView](http://developer.android.com/reference/android/widget/TextView.html) for displaying the received context changes, is illustrated below:

```
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
```

## 5. Deploy the app ##

Once you have your activity in place, you can go ahead and install it on your device and launch it. However, just like all other RSCM apps, a little preparation is needed. In particular:
  * You should make sure that the RSCM middleware is installed
  * You should make sure that the appropriate plugins are installed on your device. (In this example, there is only one required plugin, the [plugin-sensor-battery](http://code.google.com/p/rscm/source/browse/trunk/plugins/plugin-sensor-battery) which is also described in the [Creating a context sensor plugin](Creating_a_context_sensor_plugin.md) tutorial.)

If everything went smoothly, you should view something like this screenshot in your app:

![![](http://rscm.googlecode.com/svn/wiki/rscm_quickstart_screenshot.png)](http://rscm.googlecode.com/svn/wiki/rscm_quickstart_screenshot.png)