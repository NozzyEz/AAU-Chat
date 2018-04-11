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

public class NotificationHelper extends ContextWrapper{
    public static final String CHANNEL_ID_1 = "Friend Requests";
    public static final String CHANNEL_NAME_1 = "Friend Requests";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

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
