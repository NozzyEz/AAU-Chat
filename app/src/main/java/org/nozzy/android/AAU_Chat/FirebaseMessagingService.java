package org.nozzy.android.AAU_Chat;



import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Nozzy on 11/04/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private NotificationHelper mNotificationHelper;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Here we get the notification title and message from our Firebase function
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        // When that function calls this service we run this method after extracting the title and remote message
        sendFriendReqNotification(notificationTitle, notificationBody, click_action, from_user_id);

    }

    // This method takes care of our notifications
    public void sendFriendReqNotification(String title, String message, String click_action, String from_user_id){

        // Here we create a new NotificationHelper object from a class we've built to deal with our notifications
        mNotificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = mNotificationHelper.getChannnel1Notification(title, message, click_action, from_user_id);
        mNotificationHelper.getManager().notify(1, nb.build());

    }
}
