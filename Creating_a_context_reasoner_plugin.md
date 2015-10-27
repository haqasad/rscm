# Introduction #

Context reasoner (and sensor) plugins are essential components of the RSCM middleware. Unlike the sensor plugins (discussed [earlier](Creating_a_context_sensor_plugin.md)), context reasoners are not just context producers but also context consumers. Typically, context reasoners are used to process (i.e. consume) lower-level context data with the intent of producing higher-level context. For example, a _user activity_ reasoner plugin could leverage input from an accelerometer (and other) plugins with the purpose of guessing the user's activity (e.g. sitting, walking, running, sleeping, etc).

This page will guide you through the required steps for building a context reasoner plugin.

# Basics #

Like sensors, context reasoner plugins are basically ANDROID Bound services. The required steps for creating a reasoner are:
  1. Define the provided **and required** context types
  1. Intercept incoming context change notifications and process them accordingly
  1. Optionally, also intercept the [bound service lifecycle](http://developer.android.com/guide/topics/fundamentals/bound-services.html) methods to register (and unregister) from the underlying context source
  1. Create context change notifications

# Example #

These steps are better illustrated with an example. This example creates a trivial reasoner plugin that produces events related to the user activity based on readings from the [battery sensor](Creating_a_context_sensor_plugin.md) discussed earlier. Here are the required steps:

## 1. Create a new project ##

Just like before, and using the IDE of your choice, create a new ANDROID project. Make sure that the project includes the RSCM library as an external library.

## 2. Create a new reasoner service ##

Create a new [Service](http://developer.android.com/guide/topics/fundamentals/services.html). Edit your service so that it _extends_ the [org.aspectsense.rscm.context.plugin.ReasonerService.java](http://code.google.com/p/rscm/source/browse/trunk/rscm-library/src/org/aspectsense/rscm/context/plugin/ReasonerService.java) instead of the android.app.Service class.

The ReasonerService is abstract and requires the definition of one method:
  1. `@Override protected void onContextValueChanged(ContextValue contextValue);`

The resulting code should look as follows:

```
public class UserActivityReasoner extends ReasonerService
{
    @Override protected void onContextValueChanged(ContextValue contextValue)
    {
        // todo
    }
}
```

The _base class_ provides a protected method, that the implemented plugin must invoke in order to communicate the generated context values. The signature of this method is ` protected void notifyListener(final ContextValue contextValue); `

## 3. Bind to the underlying context source ##

In this example, we will not bind directly to any underlying context sources. Nevertheless, if this is necessary, then it can be done by overloading the `onBind(...)` and `onUnbind(...)` methods, as it was described in the [Creating a context sensor plugin](Creating_a_context_sensor_plugin.md) example.

Instead, in this example we will only leverage incoming context values. For the purposes of the example, we will simply aim at detecting whether
the remaining battery level is increasing or decreasing, and update the _user.activity_ scope accordingly.

The [ContextValue](http://code.google.com/p/rscm/source/browse/trunk/rscm-library/src/org/aspectsense/rscm/ContextValue.java) are created as usual, by defining the **scope** of the generated context value, and its actual value.

The complete code (available [here](http://code.google.com/p/rscm/source/browse/trunk/plugins/plugin-reasoner-user-activity/src/org/aspectsense/rscm/reasoner/user_activity/UserActivityReasoner.java)) is shown below:

```
package org.aspectsense.rscm.reasoner.user_activity;

import android.util.Log;

import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.ReasonerService;
import org.json.JSONException;

public class UserActivityReasoner extends ReasonerService
{
    public static final String TAG = "org.aspectsense.rscm.reasoner.user_activity.UserActivityReasoner";

    public static final String SCOPE_USER_ACTIVITY = "user.activity";

    public static final int BATTERY_LEVEL_UNKNOWN = -1;

    private int previous_battery_level = BATTERY_LEVEL_UNKNOWN;

    @Override protected void onContextValueChanged(ContextValue contextValue)
    {
        try
        {
            if("battery.level".equals(contextValue.getScope()))
            {
                final int battery_level = contextValue.getValueAsInteger();
                if(previous_battery_level != BATTERY_LEVEL_UNKNOWN && previous_battery_level > battery_level)
                {
                    notifyListener(ContextValue.createContextValue(SCOPE_USER_ACTIVITY, "The user is discharging his phone (" + battery_level + "% full)"));
                }
                else if(previous_battery_level != BATTERY_LEVEL_UNKNOWN && previous_battery_level < battery_level)
                {
                    notifyListener(ContextValue.createContextValue(SCOPE_USER_ACTIVITY, "The user is charging his phone (" + battery_level + "% full)"));
                }
                previous_battery_level = battery_level;
            }
            else if("power.connected".equals(contextValue.getScope()))
            {
                final boolean is_connected = contextValue.getValueAsBoolean();
                notifyListener(ContextValue.createContextValue(SCOPE_USER_ACTIVITY, "The user has " + (is_connected ? "connected his phone to the charger" : "disconnected his phone from the charger")));
            }
        }
        catch (JSONException jsone)
        {
            Log.e(TAG, "JSON exception while parsing context value: " + contextValue, jsone);
        }
    }
}
```

## 4. Editing the manifest ##

The final step in creating a context sensor plugin is defining its manifest file. Most IDEs would have already created a structure similar to the following for your app:

```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.aspectsense.rscm.reasoner.user_activity"
          android:versionCode="1"
          android:versionName="1.0">

    <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="15"/>

    <application android:label="@string/app_name">
        <service android:name=".UserActivityReasoner">
        </service>
    </application>

</manifest>
```

To facilitate the dynamic discovery and selection of plugins, RSCM requires that context plugins are annotated with:
  1. An `intent-filter` specifying an **action** and a **category** (just like with context sensors). The action should have a fixed value `<action android:name="org.aspectsense.rscm.context.SELECT_CONTEXT_PLUGIN" />`. The category should point to the full (absolute) name of the class realizing the plugin (in this example, `<category android:name="org.aspectsense.rscm.sensor.battery.BatterySensor" />`.
  1. `meta-data` specifying the _provided scopes_ of the sensor plugin **and** the _required scopes_.

Also, make sure that the service corresponding to the plugin is marked with `android:exported="true"`.

The complete manifest for the example is illustrated here (and is also available at [AndroidManifest.xml](http://code.google.com/p/rscm/source/browse/trunk/plugins/plugin-reasoner-user-activity/AndroidManifest.xml)):

```
<?xml version="1.0" encoding="utf-8"?>

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.aspectsense.rscm.reasoner.user_activity"
          android:versionCode="1"
          android:versionName="1.0">

    <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="15"/>

    <application android:label="@string/app_name">

        <service android:name=".UserActivityReasoner" android:exported="true">
            <intent-filter>
                <!-- These are the interfaces supported by the service, which you can bind to. -->
                <action android:name="org.aspectsense.rscm.context.SELECT_CONTEXT_PLUGIN" />
                <!-- Each plugin must specify exactly one category property, in order to allow the middleware to pick
                     the right one. The category must be unique and match the service ID. -->
                <category android:name="org.aspectsense.rscm.reasoner.user_activity.UserActivityReasoner" />
            </intent-filter>
            <!-- The metadata can be used in later versions to allow for cleverly selecting the most appropriate plugin -->
            <meta-data android:name="provided_scopes" android:value="user.activity"/>
            <meta-data android:name="required_scopes" android:value="battery.level"/>
        </service>

    </application>

</manifest>
```

## 5. Deploying ##

Deploying a context sensor plugin (also a reasoner plugin) is as simple as installing the resulting APK on your device. If the device is published in the market, then it can be installed from there as well.