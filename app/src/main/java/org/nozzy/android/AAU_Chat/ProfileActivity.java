package org.nozzy.android.AAU_Chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

// This activity is used when a user views another user's profile. From here,
// they can block/unblock the user, start a new direct chat or just view info about them.
public class ProfileActivity extends AppCompatActivity {

    // UI
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mBlockBtn, mChatBtn;

    private ProgressDialog mProgressDialog;

    // Firebase references to fetch profile in and manipulate friend requests
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mRootRef;

    // Firebase user so we can get the users uid for sending and receiving friend requests.
    private FirebaseUser mCurrent_user;
    private String mCurrentUserID;

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
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Set up the current user
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrent_user.getUid();

        // We set up the UI
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mBlockBtn = findViewById(R.id.profile_send_req_btn);
        mChatBtn = findViewById(R.id.profile_decline_req_btn);

        // Here we set the current state:
        // 1 = Friends, 2 = user viewed is blocked by you, 3 = you are blocked by the user
        mCurrent_state = 1;

        // A progress dialog which is displayed while the profile info is loaded
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading profile");
        mProgressDialog.setMessage("Please wait while the profile is loaded");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        // Here we setup the profile page with the info read from the database
        mUsersDatabase.child(profile_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Gets the current user's name, status and image
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                // Sets the name and status in the text fields
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
                        // If the image fails to load, set it to the default profile image
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.generic).into(mProfileImage);
                    }
                });

                // Check to see if user and the other user are friends
                mFriendsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(profile_id).hasChild(mCurrentUserID) && dataSnapshot.child(mCurrentUserID).hasChild(profile_id)) {
                            // If the current user is already friends with the user being viewed,
                            // Sets the current state to "Friends"
                            setCurrentState(1);
                            mProgressDialog.dismiss();
                        } else {
                            // Check the current user's block list
                            mUsersDatabase.child(mCurrentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // If the block list exists and it contains the other user
                                    if (dataSnapshot.hasChild("blocked") && dataSnapshot.child("blocked").hasChild(profile_id)) {
                                        // Set the current state to "User is Blocked"
                                        setCurrentState(2);
                                    }
                                    // if the block list does not exist, or it doesn't contain the other user
                                    // Set the current state to "You are Blocked"
                                    else setCurrentState(3);
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        // This button is for blocking/unblocking the user we are viewing
        // The button will check the current state and the button action being done will be set accordingly
        mBlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // First we disable the button so the user can only tap it once and not make duplicate queries in our database
                mBlockBtn.setEnabled(false);

                // If the current user is already friends with the user being viewed, this button can block the person
                //--------FRIENDS--------//
                if (mCurrent_state == 1) {
                    // Blocks the user
                    blockUser(profile_id);
                    // Sets the current state to "User is Blocked"
                    setCurrentState(2);
                    // Shows a toast that the user has been blocked
                    Toast.makeText(ProfileActivity.this, "User Blocked", Toast.LENGTH_LONG).show();
                }

                // If the user has already been blocked, this button can unblock the user
                //--------USER IS BLOCKED--------//
                else if (mCurrent_state == 2) {
                    // Unblocks the user
                    unblockUser(profile_id);
                    // Sets the current state to "Friends"
                    setCurrentState(1);
                    // Shows a toast that the user has been unblocked
                    Toast.makeText(ProfileActivity.this, "User Unblocked", Toast.LENGTH_LONG).show();
                }

                //--------YOU ARE BLOCKED--------//
                // If you are blocked by the other user, the button should be invisible and inactive

            }
        });

        // The chat button is used for starting a new chat with the user
        mChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatBtn.setEnabled(false);

                // Generates chat ID
                mRootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference chat_push = mRootRef.child("Chats").push();
                final String push_id = chat_push.getKey();

                // Adding the chat with timestamp to the Users table
                mRootRef.child("Users").child(mCurrentUserID).child("chats").child(push_id).child("timestamp").setValue(ServerValue.TIMESTAMP);
                mRootRef.child("Users").child(profile_id).child("chats").child(push_id).child("timestamp").setValue(ServerValue.TIMESTAMP);

                // Creating the chat in the Chats table with members, name, type, image and seen values
                mRootRef.child("Chats").child(push_id).child("members").child(mCurrentUserID).setValue("admin");
                mRootRef.child("Chats").child(push_id).child("members").child(profile_id).setValue("admin");
                mRootRef.child("Chats").child(push_id).child("chat_name").setValue("New Chat");
                mRootRef.child("Chats").child(push_id).child("chat_type").setValue("direct");
                mRootRef.child("Chats").child(push_id).child("chat_image").setValue("");

                mRootRef.child("Chats").child(push_id).child("seen").child(profile_id).setValue("");
                mRootRef.child("Chats").child(push_id).child("seen").child(mCurrentUserID).setValue("");

                // Passing variables and starting ChatActivity
                Intent chatIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                chatIntent.putExtra("chat_id", push_id);
                startActivity(chatIntent);
            }
        });
    }

    // This method sets the current state and changes the UI accordingly
    private void setCurrentState(int state) {
        // Set the current status
        mCurrent_state = state;
        switch (state) {
            // "Friends"
            case 1:
                // Set the Block button text to 'Block this user'
                mBlockBtn.setText(R.string.block_user);
                mBlockBtn.setVisibility(View.VISIBLE);
                mBlockBtn.setEnabled(true);
                // Make the Chat button visible
                mChatBtn.setVisibility(View.VISIBLE);
                mChatBtn.setEnabled(true);
                break;
            // "User is Blocked"
            case 2:
                // Set the Block button text to 'Unblock this user'
                mBlockBtn.setText(R.string.unblock_user);
                mBlockBtn.setVisibility(View.VISIBLE);
                mBlockBtn.setEnabled(true);
                // Make the Decline button invisible
                mChatBtn.setVisibility(View.GONE);
                mChatBtn.setEnabled(false);
                break;
            // "You are Blocked"
            case 3:
                // Make both buttons invisible and inactive
                mBlockBtn.setVisibility(View.GONE);
                mBlockBtn.setEnabled(false);
                mChatBtn.setVisibility(View.GONE);
                mChatBtn.setEnabled(false);
                break;

            default:
                Toast.makeText(ProfileActivity.this, "Invalid state", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    // Sets the online value of the user back to true
    public void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue("true");
    }

    @Override
    // Sets the online value to the current timestamp if the activity is paused
    protected void onPause() {
        super.onPause();
        mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }




    // Method to block the user currently being viewed
    private void blockUser(final String blockedUser) {

        // Remove yourself from the user's friend list
        mRootRef.child("Friends").child(blockedUser).child(mCurrentUserID).removeValue();

        final DatabaseReference userBlockedRef = mRootRef.child("Users").child(mCurrentUserID).child("blocked").child(blockedUser);
        final DatabaseReference blockedUserChatsRef = mRootRef.child("Users").child(blockedUser).child("chats");
        final DatabaseReference userChatsRef = mRootRef.child("Users").child(mCurrentUserID).child("chats");
        final DatabaseReference allChatsRef = mRootRef.child("Chats");

        // Find all direct chats with this user and move them to the "blocked" section
        Query userChatQuery = userChatsRef.orderByChild("timestamp");
        userChatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> userChats = dataSnapshot.getChildren();
                for (DataSnapshot userChat : userChats) {
                    final String chatKey = userChat.getKey();

                    allChatsRef.child(chatKey).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Looks at direct chats (those that have 2 members)
                            if (dataSnapshot.getChildrenCount() == 2) {
                                Iterable<DataSnapshot> chatMembers = dataSnapshot.getChildren();
                                for (DataSnapshot chatMember : chatMembers) {
                                    // Looks through the members; if this is a chat with the blocked user
                                    if (chatMember.getKey().equals(blockedUser)) {
                                        // Adds the chat key to the blocked list
                                        userBlockedRef.child(chatKey).setValue(true);
                                        // Removes the chat key from current user
                                        userChatsRef.child(chatKey).removeValue();
                                        // Removes the chat key from blocked user
                                        blockedUserChatsRef.child(chatKey).removeValue();
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }



    // Method to unblock the user currently being viewed
    private void unblockUser(final String blockedUser) {

        // DateTime to get the current time
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        // Add yourself to the user's friend list
        mRootRef.child("Friends").child(blockedUser).child(mCurrentUserID).child("date").setValue(currentDate);

        final DatabaseReference userBlockedRef = mRootRef.child("Users").child(mCurrentUserID).child("blocked").child(blockedUser);
        final DatabaseReference blockedUserChatsRef = mRootRef.child("Users").child(blockedUser).child("chats");
        final DatabaseReference userChatsRef = mRootRef.child("Users").child(mCurrentUserID).child("chats");

        userBlockedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> blockedChats = dataSnapshot.getChildren();
                for (DataSnapshot blockedChat : blockedChats) {
                    // Gets the key of the blocked chat
                    String blockedChatKey = blockedChat.getKey();
                    // Adds the chat to current user
                    userChatsRef.child(blockedChatKey).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    // Adds the chat to blocked user
                    blockedUserChatsRef.child(blockedChatKey).child("timestamp").setValue(ServerValue.TIMESTAMP);
                }
                // Removes the blocked user from the blocked list
                userBlockedRef.removeValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });


    }
}
