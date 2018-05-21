package org.nozzy.android.AAU_Chat;

import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Nozzy on 11/04/2018.
 */
// This class is used for dealing with notifications received from Firebase
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private NotificationHelper mNotificationHelper;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Here we get the notification title and message from our Firebase function
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();

        String chat_id = remoteMessage.getData().get("chat_id");

        sendMessageNotification(notificationTitle, notificationBody, click_action, chat_id);

    }

    private void sendMessageNotification(String title, String message, String click_action, String chat_id) {

        // Here we create a new NotificationHelper object from a class we've built to deal with our notifications
        mNotificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = mNotificationHelper.getChannel1MessageNotification(title, message, click_action, chat_id);
        mNotificationHelper.getManager().notify(1, nb.build());

    }
}
