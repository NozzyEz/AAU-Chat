package org.nozzy.android.lapitchat;



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

        // When that function calls this service we run this method after extracting the title and remote message
        sendFriendReqNotification(notificationTitle, notificationBody);


    }

    // This method takes care of our notifications
    public void sendFriendReqNotification(String title, String message){

        // Here we create a new NotificationHelper object from a class we've built to deal with our notifications
        mNotificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = mNotificationHelper.getChannnel1Notification(title, message);
        mNotificationHelper.getManager().notify(1, nb.build());



    }
}
