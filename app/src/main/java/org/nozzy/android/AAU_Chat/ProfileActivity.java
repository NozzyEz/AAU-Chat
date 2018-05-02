package org.nozzy.android.AAU_Chat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// This activity is used when a user views another user's profile. From here, they can send, cancel
// or decline a friend request, unfriend the user, or just view info about the user.
public class ProfileActivity extends AppCompatActivity {

    // UI
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private ProgressDialog mProgressDialog;

    // This time we need two database references, one to fetch profile in, and one to manipulate friend requests
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    // Firebase user so we can get the users uid for sending and receiving friend requests.
    private FirebaseUser mCurrent_user;


    // The current state which lets us know whether the profile viewed is of a user that is also a friend
    private int mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Here we are passed the profile's User ID, we use this to fetch the info from the database,
        // so that we can show it on the profile page
        final String profile_id;
        String data = getIntent().getStringExtra("user_id");
        if (data == null)
            profile_id = getIntent().getStringExtra("from_user_id");
        else
            profile_id = getIntent().getStringExtra("user_id");


        // Here we point the databases to the correct places, as well as fetch the current user, which is not the same as the user for the profile page
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(profile_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Keeps the database synced, there is a 'bug' with using this, that makes it so the friends status does not update
        // mUsersDatabase.keepSynced(true);

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        // We set up the UI
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = findViewById(R.id.profile_decline_req_btn);

        // Here we set the current state, meaning friends state:
        // 0 = not friends, 1 = friends, 2 = request sent and pending, 3 = request received
        mCurrent_state = 0;
        // By default, the Decline button is invisible - it is only used when a request is received
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        // A progress dialog which is displayed while the profile info is loaded
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading profile");
        mProgressDialog.setMessage("Please wait while the profile is loaded");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        // Here we setup the profile page with the info read from the database
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                // Once more we use picasso to load the profile image of the user we are showing
                Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.generic).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.generic).into(mProfileImage);
                    }
                });

                // Changes the friend status based on the current user's friend request type in the database
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // Checks if the current user has a request for the user being viewed
                        if(dataSnapshot.hasChild(profile_id)) {
                            // Gets the request type
                            String request_type = dataSnapshot.child(profile_id).child("request_type").getValue().toString();
                            if(request_type.equals("received")){
                                // If the current user has received a friend request from the user being viewed,
                                // Change the current status to 3
                                mCurrent_state = 3;
                                // Set the Request button text to 'Accept Friend Request'
                                mProfileSendReqBtn.setText(R.string.accept_friend_req);
                                // Also make the Decline button visible, so the user can decline the received request
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if(request_type.equals("sent")) {
                                // If the current user has sent a friend request to the user being viewed,
                                // Change the current status to 2
                                mCurrent_state = 2;
                                // Set the Request button text to 'Cancel Friend Request'
                                mProfileSendReqBtn.setText(R.string.cancel_friend_req);
                                // Also make the Decline button invisible - nothing to decline
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        } else {
                            // Else, if the current user has no requests with the user being viewed,
                            // Check to see if user and the other user are already friends
                            mFriendsDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(profile_id)) {
                                        // If the current user is already friends with the user being viewed,
                                        // Change the current status to 1
                                        mCurrent_state = 1;
                                        // Set the Request button text to 'Unfriend User'
                                        mProfileSendReqBtn.setText(R.string.unfriend);
                                    }
                                    mProgressDialog.dismiss();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                        // If neither of these are true, then the current user is not friends with the user being viewed
                        // Everything is left to default: status is 0, Decline button is invisible, Request button says 'Send Friend Request'
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        // This button is for sending/unfriending/canceling/accepting a friend request to the user we are viewing
        // The button will check the current state and the button action being done will be set accordingly
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // First we disable the button so the user can only tap it once and not make duplicate queries in our database
                mProfileSendReqBtn.setEnabled(false);

                // We check to see if we are friends with the user, if 0 we are not friends, so
                // when we click the button we initialize and send a friend request
                //--------NOT FRIENDS--------//
                if (mCurrent_state == 0) {
                    // Dealing with notifications for alerting the user that he received a friend request
                    DatabaseReference newNotificationref = mRootRef.child("Notifications").child(profile_id).push();
                    String newNotificationID = newNotificationref.getKey();

                    // A hashmap for storing notification data
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    // A hashmap used to store the two new requests, as well as notifications
                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + profile_id + "/" + newNotificationID, notificationData);

                    // Attempts to store all data in the database
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                // Change the current status to 2, which means a request is now sent and pending
                                mCurrent_state = 2;
                                // Set the Request button text to 'Cancel Friend Request'
                                mProfileSendReqBtn.setText(R.string.cancel_friend_req);
                                mProfileSendReqBtn.setEnabled(true);
                                // Make the Decline button invisible
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                                // Shows a toast that a request has been sent
                                Toast.makeText(ProfileActivity.this, "Friend Request sent", Toast.LENGTH_LONG).show();
                            } else {
                                // Shows a toast if there was an error in putting data into the database
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // If the current user is already friends with the user being viewed, this button can unfriend the person
                //--------FRIENDS--------//
                if (mCurrent_state == 1) {

                    // A hashmap with null values, used for clearing 'friends since' dates
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + profile_id, null);
                    unfriendMap.put("Friends/" + profile_id + "/" + mCurrent_user.getUid(), null);

                    // Attempts to store all data in the database
                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                // Change the current status to 0, which means that the users are not friends
                                mCurrent_state = 0;
                                // Set the Request button text to 'Send Friend Request'
                                mProfileSendReqBtn.setText(R.string.send_friend_req);
                                mProfileSendReqBtn.setEnabled(true);
                                // Make the Decline button invisible
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                                // Shows a toast that the users are no longer friends
                                Toast.makeText(ProfileActivity.this, "You are no longer friends", Toast.LENGTH_LONG).show();
                            } else {
                                // Shows a toast if there was an error in putting data into the database
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }

                // If a friend request has already been sent, this button can cancel the request
                //--------FRIEND REQUEST SENT--------//
                if(mCurrent_state == 2) {

                    // A hashmap with null values, used for clearing requests
                    Map cancelMap = new HashMap();
                    cancelMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id, null);
                    cancelMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid(), null);

                    // Attempts to store all data in the database
                    mRootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                // Change the current status to 0, which means that the request has been canceled
                                mCurrent_state = 0;
                                // Set the Request button text to 'Send Friend Request'
                                mProfileSendReqBtn.setText(R.string.send_friend_req);
                                mProfileSendReqBtn.setEnabled(true);
                                // Make the Decline button invisible
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                                // Shows a toast that the request has been canceled
                                Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_LONG).show();
                            } else {
                                // Shows a toast if there was an error in putting data into the database
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // If a friend request has been received, this button can accept the request
                //--------FRIEND REQUEST RECEIVED--------//
                if (mCurrent_state == 3) {

                    // Get the current date from the system
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    // A hashmap with currentDate values in the Friends table, as well as null values for requests
                    // Used for marking the date on which the users became friends, and for clearing the requests
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + profile_id + "/date", currentDate);
                    friendsMap.put("Friends/" + profile_id + "/" + mCurrent_user.getUid() + "/date", currentDate);
                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id, null);
                    friendsMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid(), null);

                    // Attempts to store all data into the database
                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                // Change the current status to 1, which means that the two users are now friends
                                mCurrent_state = 1;
                                // Set the Request button text to 'Unfriend User'
                                mProfileSendReqBtn.setText(R.string.unfriend);
                                mProfileSendReqBtn.setEnabled(true);
                                // Make the Decline button invisible
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                                // Shows a toast that the users have now become friends
                                Toast.makeText(ProfileActivity.this, "You are now friends", Toast.LENGTH_LONG).show();
                            }
                            else {
                                // Shows a toast if there was an error in putting data into the database
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        // The decline button is only used for declining a received friend request
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeclineBtn.setEnabled(false);

                // A hashmap with null values used for clearing the requests
                Map declineMap = new HashMap();
                declineMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id, null);
                declineMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid(), null);

                // Attempts to put all data into the database
                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // Change the current status to 0, which means that the two users are not friends
                            mCurrent_state = 0;
                            // Set the Request button text to 'Send Friend Request'
                            mProfileSendReqBtn.setText(R.string.send_friend_req);
                            mProfileSendReqBtn.setEnabled(true);
                            // Make the Decline button invisible
                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);
                            // Shows a toast that the user has declined the friend request
                            Toast.makeText(ProfileActivity.this, "You have declined the friend request", Toast.LENGTH_LONG).show();
                        } else {
                            // Shows a toast if there was an error in putting data into the database
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
