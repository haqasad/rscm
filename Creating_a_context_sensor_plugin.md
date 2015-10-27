# Introduction #

Context sensor (and reasoner) plugins are essential components of the RSCM middleware. Instead of packing all context sensing (and reasoning functionality) in a single app, RSCM allows you to define plugins as individual, reusable and dynamically discoverable components.

This page will guide you through the required steps for building a context sensor plugin.

# Basics #

A context plugin is basically an ANDROID Bound service. The required steps are:
  1. Define the provided context types
  1. Intercept the [bound service lifecycle](http://developer.android.com/guide/topics/fundamentals/bound-services.html) methods to register (and unregister) from the underlying context source
  1. Create context change notifications

# Example #

These steps are better illustrated with an example. This example creates a trivial sensor plugin that produces events related to the (remaining) battery level. Here are the required steps:

## 1. Create a new project ##

Using the IDE of your choice, create a new ANDROID project. Make sure that the project includes the RSCM library as an external library.


## 2. Create a new sensor service ##

Create a new [Service](http://developer.android.com/guide/topics/fundamentals/services.html). Edit your service so that it _extends_ the [org.aspectsense.rscm.context.plugin.SensorService.java](http://code.google.com/p/rscm/source/browse/trunk/rscm-library/src/org/aspectsense/rscm/context/plugin/SensorService.java) instead of the android.app.Service class. The resulting code should look as follows:

```
public class BatterySensor extends SensorService
{
}
```

The _base class_ provides a protected method, that the implemented plugin must invoke in order to communicate the generated context values. The signature of this method is ` protected void notifyListener(final ContextValue contextValue); `

## 3. Bind to the underlying context source ##

Typically, context sensor plugins are _wrappers_ of underlying sensing functionality. For instance, the battery sensor plugin depends on ANDROID's own [BatteryManager](http://developer.android.com/reference/android/os/BatteryManager.html).

A properly behaving plugin should start monitoring the underlying context as soon as it is started (i.e. service is bound) and stop immediately before terminating (i.e. service is stopped). To realize this kind of behavior, the Service lifecycle is typically exploited. In particular, the following two methods are overriden with the purpose of controlling when to start/stop listening to context changes:
  * `@Override public IBinder onBind(Intent intent);`
  * `@Override public boolean onUnbind(Intent intent);`

In this example, the requirement is to start listening to changes in the battery level when the sensor plugin is active. This is achieved by realizing these two methods as follows:

```
    @Override public IBinder onBind(Intent intent)
    {
        registerReceiver(batteryReceiver, filter);

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        unregisterReceiver(batteryReceiver);

        return super.onUnbind(intent);
    }
```

The calls to the `registerReceiver(...)` and `unregisterReceiver(...)` methods (defined by ANDROID) allow for registering a [Receiver](http://developer.android.com/reference/android/content/BroadcastReceiver.html) component to receive certain events. In this case the type we are interested in is [Intent.ACTION\_BATTERY\_CHANGED](http://developer.android.com/reference/android/content/Intent.html#ACTION_BATTERY_CHANGED):

```
    public static final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
```

## 4. Create the context change events ##

Once we get hold of the values we are interested in, we need to notify the RSCM middleware. As mentioned above, this is done via the `notifyListener` method. This method takes as parameter a context value, which means that we need to create it beforehand.

The [ContextValue](http://code.google.com/p/rscm/source/browse/trunk/rscm-library/src/org/aspectsense/rscm/ContextValue.java) requires as input the **scope** of the generated context value, and its actual value. For now, let's assume that the context value is a primitive one (i.e., boolean, integer, double, or String). For instance, the battery level is an integer in the range [0, 100]m which means that it can be constructed using the following code:

```
    public static final String SCOPE_BATTERY_LEVEL = "battery.level";
    ...
    final int level = ...;
    final ContextValue lastContextValueBatteryLevel = ContextValue.createContextValue(SCOPE_BATTERY_LEVEL, level);

```

The complete code (available [here](http://code.google.com/p/rscm/source/browse/trunk/plugins/plugin-sensor-battery/src/org/aspectsense/rscm/sensor/battery/BatterySensor.java)) is shown below:

```
public class BatterySensor extends SensorService
{
    public static final String TAG = "org.aspectsense.rscm.sensor.battery.BatterySensor";

    public static final String SCOPE_BATTERY_LEVEL = "battery.level";

    public static final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    @Override public IBinder onBind(Intent intent)
    {
        registerReceiver(batteryReceiver, filter);

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        unregisterReceiver(batteryReceiver);

        return super.onUnbind(intent);
    }

    final BroadcastReceiver batteryReceiver = new BroadcastReceiver()
    {
        int level = -1;

        @Override public void onReceive(Context context, Intent intent)
        {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

            final ContextValue lastContextValueBatteryLevel = ContextValue.createContextValue(SCOPE_BATTERY_LEVEL, level);

            notifyListener(lastContextValueBatteryLevel);
        }
    };
}
```

## 5. Editing the manifest ##

The final step in creating a context sensor plugin is defining its manifest file. Most IDEs would have already created a structure similar to the following for your app:

```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.aspectsense.rscm.sensor.battery"
          android:versionCode="1"
          android:versionName="1.0">

    <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="15"/>

    <application android:label="@string/app_name">
        <service android:name=".BatterySensor">
        </service>
    </application>

</manifest>
```

To facilitate the dynamic discovery and selection of plugins, RSCM requires that context plugins are annotated with:
  1. An `intent-filter` specifying an **action** and a **category**. The action should have a fixed value `<action android:name="org.aspectsense.rscm.context.SELECT_CONTEXT_PLUGIN" />`. The category should point to the full (absolute) name of the class realizing the plugin (in this example, `<category android:name="org.aspectsense.rscm.sensor.battery.BatterySensor" />`.
  1. `meta-data` specifying the _provided scopes_ of the sensor plugin (also the _required scopes_ in case of a context reasoner plugin).

Also, make sure that the service corresponding to the plugin is marked with `android:exported="true"`.

The complete manifest for the example is illustrated here (and is also available at [AndroidManifest.xml](http://code.google.com/p/rscm/source/browse/trunk/plugins/plugin-sensor-battery/AndroidManifest.xml)):

```
<?xml version="1.0" encoding="utf-8"?>

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.aspectsense.rscm.sensor.battery"
          android:versionCode="1"
          android:versionName="1.0">

    <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="15"/>

    <application android:label="@string/app_name">
        <service android:name=".BatterySensor" android:exported="true">
            <intent-filter>
                <action android:name="org.aspectsense.rscm.context.SELECT_CONTEXT_PLUGIN" />
                <category android:name="org.aspectsense.rscm.sensor.battery.BatterySensor" />
            </intent-filter>
            <meta-data android:name="provided_scopes" android:value="battery.level, battery.voltage, battery.temp"/>
        </service>
    </application>

</manifest>
```

## 6. Deploying ##

Deploying a context sensor plugin (also a reasoner plugin) is as simple as installing the resulting APK on your device. If the device is published in the market, then it can be installed from there as well.