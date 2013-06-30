package org.aspectsense.rscm.sensor.user.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import org.aspectsense.rscm.context.plugin.SensorService;

/**
 * User: Nearchos Paspallis
 * Date: 5/24/13
 * Time: 2:52 PM
 */
public class UserActivitySensor extends SensorService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener
{
    public static final String TAG = "org.aspectsense.rscm.sensor.user.activity.UserActivitySensor";

    // Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

//    // Store the PendingIntent used to send activity recognition events back to the app
    private PendingIntent activityRecognitionPendingIntent;

//    // Store the current activity recognition client
    private ActivityRecognitionClient activityRecognitionClient;

    @Override public IBinder onBind(Intent intent)
    {
        // check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // if Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode)
        {
            // Instantiate a new activity recognition client. Since the parent Activity implements the connection
            // listener and connection failure listener, the constructor uses "this" to specify the values of those
            // parameters.
            activityRecognitionClient = new ActivityRecognitionClient(this, this, this);

            // Return a PendingIntent that starts the IntentService.
            activityRecognitionPendingIntent = PendingIntent.getService(
                    this,
                    0,
                    new Intent(this, ActivityRecognitionIntentService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            activityRecognitionClient.connect();
        }
        else
        {
            // todo handle this with asking to install Google Play Services
            // (see http://developer.android.com/training/location/activity-recognition.html)
        }

        return super.onBind(intent);
    }

    @Override public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "UserActivitySensor: unregisterReceiver");
        activityRecognitionClient.disconnect();

        return super.onUnbind(intent);
    }

    // -------------------------- implementing GooglePlayServicesClient.ConnectionCallbacks ------------------------- //

    @Override public void onDisconnected()
    {
        if(activityRecognitionClient.isConnected())
        {
            activityRecognitionClient.removeActivityUpdates(activityRecognitionPendingIntent);
        }
    }

    @Override public void onConnected(Bundle bundle)
    {
        // assert activityRecognitionClient.isConnected() is true
        activityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, activityRecognitionPendingIntent);
    }

    // -------------------------- implementing GooglePlayServicesClient.ConnectionCallbacks ------------------------- //


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // todo handle this with asking to install Google Play Services
        // (see http://developer.android.com/training/location/activity-recognition.html)
    }
}