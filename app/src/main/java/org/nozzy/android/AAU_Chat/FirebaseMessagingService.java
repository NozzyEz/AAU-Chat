package org.nozzy.android.AAU_Chat;

import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Nozzy on 11/04/2018.
 */
// This class is used for dealing with notifications received from Firebase
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private NotificationHelper mNotificationHelper;

    // Firebase
    // we need the user ID
    private String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //Then we need a database reference to the user's notifications
    private DatabaseReference notification_ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(user_id);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Here we get the notification title and message from our Firebase function
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();

        String chat_id = remoteMessage.getData().get("chat_id");
        String notification_id = remoteMessage.getData().get("notification_id");

        sendMessageNotification(notificationTitle, notificationBody, click_action, chat_id, notification_id);

    }

    private void sendMessageNotification(String title, String message, String click_action, String chat_id, String notification_id) {

        // Here we create a new NotificationHelper object from a class we've built to deal with our notifications
        mNotificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = mNotificationHelper.getChannel1MessageNotification(title, message, click_action, chat_id);
        mNotificationHelper.getManager().notify(1, nb.build());
        deleteMessageNotification(notification_id, notification_ref);

    }

    private void deleteMessageNotification(String notification_id, DatabaseReference notification_ref){

        //then delete the notification with the id gotten from the payload
        notification_ref.child(notification_id).removeValue();


    }
}
