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

        String title = "Friend Request";
        String message = "You have received a new friend request, congrats";

        sendFriendReqNotification(title, message);


    }

    public void sendFriendReqNotification(String title, String message){

        mNotificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = mNotificationHelper.getChannnel1Notification(title, message);
        mNotificationHelper.getManager().notify(1, nb.build());



    }
}
