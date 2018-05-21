package org.nozzy.android.AAU_Chat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
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

    // Here we check to see if there is a manager in place, if that is not the case, we set it
    public NotificationManager getManager() {

        if(mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    // This is how the message notification is set up
    public NotificationCompat.Builder getChannel1MessageNotification(String title, String message, String click_action, String chat_id) {

        // Here we create an intent to send the user to the chat activity where the notification came from
        Intent resultIntent = new Intent(click_action);
        // With the intent we pass the chat ID of the conversation where the notification came from
        resultIntent.putExtra("chat_id", chat_id);

        // This intent is pending and ready to execute when the user taps it from within the app
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // And here we return the notification back to the FirebaseMessagingService class for showing
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID_1)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(resultPendingIntent);
    }

}
