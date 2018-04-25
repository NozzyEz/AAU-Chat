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

public class ProfileActivity extends AppCompatActivity {

    // UI
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    // This time we need two database references, one to fetch profile in, and one to manipulate friend requests
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    // Firebase user so we can get the users uid for sending and receiving friend requests.
    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgressDialog;

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
            profile_id =  getIntent().getStringExtra("user_id");


        // Here we point the databases to the correct places, as well as fetch the current user, which is not the same as the user for the profile page
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(profile_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Keeps the database synched, there is a 'bug' with using this, that makes it so the friends status does not update
        // mUsersDatabase.keepSynced(true);

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        // We set up the UI
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = findViewById(R.id.profile_decline_req_btn);

        // Here we set the current state, meaning friends state, 0 = not friends, 1 = friends 2 = request pending 3 = request received
        mCurrent_state = 0;
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

                // Friend status
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(profile_id)) {

                            String request_type = dataSnapshot.child(profile_id).child("request_type").getValue().toString();
                            if(request_type.equals("received")){

                                // Change the current status to 3, which means a request from the person has been received and is pending action from the user
                                mCurrent_state = 3;
                                mProfileSendReqBtn.setText(R.string.accept_friend_req);

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if(request_type.equals("sent")) {

                                // Change the current status to 2, which means a request is pending and change the button text
                                mCurrent_state = 2;
                                mProfileSendReqBtn.setText(R.string.cancel_friend_req);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        } else {

                            // Check to see if user and the other user are already friends
                            mFriendsDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                     if (dataSnapshot.hasChild(profile_id)) {

                                         mCurrent_state = 1;
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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // This button is for sending a friend request to the user we are viewing
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // First we disable the button so the user can only tap it once and not make duplicate queries in our database
                mProfileSendReqBtn.setEnabled(false);

                // We check to see if we are friends with the user, if 0 we are not friends, so
                // when we click the button we initialize and send a friend request
                //--------NOT FRIENDS--------//
                if (mCurrent_state == 0) {
                    DatabaseReference newNotificationref = mRootRef.child("Notifications").child(profile_id).push();
                    String newNotificationID = newNotificationref.getKey();


                  HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                   Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + profile_id + "/" + newNotificationID, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                // Change the current status to 2, which means a request is pending and change the button text
                                mCurrent_state = 2;
                                mProfileSendReqBtn.setText(R.string.cancel_friend_req);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Friend Request sent", Toast.LENGTH_LONG).show();
                                mProfileSendReqBtn.setEnabled(true);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //--------FRIEND REQUEST SENT--------//
                if(mCurrent_state == 2) {

                    Map cancelMap = new HashMap();
                    cancelMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id, null);
                    cancelMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mProfileSendReqBtn.setEnabled(true);

                                // Change the current status to 0, to reflect the request is no longer pending
                                mCurrent_state = 0;
                                mProfileSendReqBtn.setText(R.string.send_friend_req);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_LONG).show();
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //--------FRIEND REQUEST RECEIVED--------//
                if (mCurrent_state == 3) {

                    // Get the current date from the system
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + profile_id + "/date", currentDate);
                    friendsMap.put("Friends/" + profile_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id, null);
                    friendsMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mProfileSendReqBtn.setEnabled(true);
                                // Change the current status to 1, to reflect that the two users are now friends.
                                mCurrent_state = 1;
                                mProfileSendReqBtn.setText(R.string.unfriend);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "You are now friends", Toast.LENGTH_LONG).show();
                            }
                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                 if (mCurrent_state == 1) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + profile_id, null);
                    unfriendMap.put("Friends/" + profile_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = 0;
                                mProfileSendReqBtn.setText(R.string.send_friend_req);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "You ae no longer friends", Toast.LENGTH_LONG).show();
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                 }
            }
        });

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeclineBtn.setEnabled(false);

                Map declineMap = new HashMap();
                declineMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + profile_id, null);
                declineMap.put("Friend_req/" + profile_id + "/" + mCurrent_user.getUid(), null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null) {
                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = 0;
                            mProfileSendReqBtn.setText(R.string.send_friend_req);

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);

                            Toast.makeText(ProfileActivity.this, "You have declined the friend request", Toast.LENGTH_LONG).show();
                        } else {
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
