package org.aspectsense.rscm.sensor.user.activity;

import android.app.IntentService;
import android.content.Intent;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * User: Nearchos Paspallis
 * Date: 5/24/13
 * Time: 10:14 PM
 */
public class ActivityRecognitionIntentService extends IntentService
{
    public ActivityRecognitionIntentService()
    {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override protected void onHandleIntent(Intent intent)
    {
        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent))
        {
            // Get the update
            final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the most probable activity from the list of activities in the update
            final DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity [0-100]
            final int confidence = mostProbableActivity.getConfidence();

            // Get the type of activity
            final int activityType = mostProbableActivity.getType();

            // todo
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType)
    {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}