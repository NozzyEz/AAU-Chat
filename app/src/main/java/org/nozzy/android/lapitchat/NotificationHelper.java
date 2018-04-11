package org.nozzy.android.lapitchat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Nozzy on 11/04/2018.
 */

// This class takes care of our notifications inside of the app for us
public class NotificationHelper extends ContextWrapper{
    public static final String CHANNEL_ID_1 = "Channel 1"; // Needs more specificity
    public static final String CHANNEL_NAME_1 = "Channel 1"; // So does this

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        // Inside of our constructor we first do a check for the users version of android, and only
        // continue with our method if the user runs Android Oreo 8.0 or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    // If the user does run Android Oreo, we have to create notification channels for our
    // notifications to be sent on, which is what we set up down below
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannels() {

        NotificationChannel channelReq = new NotificationChannel(CHANNEL_ID_1,CHANNEL_NAME_1, NotificationManager.IMPORTANCE_DEFAULT);
        channelReq.enableLights(true);
        channelReq.enableVibration(true);
        channelReq.setLightColor(R.color.colorPrimary);
        channelReq.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channelReq);
    }

    public NotificationManager getManager() {

        if(mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannnel1Notification(String title, String message) {

        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID_1)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.notification_icon);

    }


}
