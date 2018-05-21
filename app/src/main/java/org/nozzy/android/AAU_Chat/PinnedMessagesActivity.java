package org.nozzy.android.AAU_Chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// This activity is used for chatting with other users.
public class PinnedMessagesActivity extends AppCompatActivity {

    // ID of the user being chatted with
    // It will only be used if the type of the chat is "direct"
    private String mDirectUserID;

    // Parameters of the chat
    private String mChatID;
    private String mChatType;
    private String mChatName;
    private String mChatImage;

    private DatabaseReference mRootRef;

    // UI
    private Toolbar mChatToolbar;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private PinsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_messages);

        // Setting up the Firebase references
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        // Setting up passed variables
        mChatID = getIntent().getStringExtra("chat_id");

        // Setting up the UI
        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        mLastSeenView.setText("Pinned messages");

        // Sets the name, image and online values
        // Adds a listener to get all the chat values
        mRootRef.child("Chats").child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Gets the type of the chat
                mChatType = dataSnapshot.child("chat_type").getValue(String.class);
                // Checks if the chat is direct
                if (mChatType.equals("direct")) {
                    // Goes through all members of the chat to find the other member
                    Iterable<DataSnapshot> chatMembers = dataSnapshot.child("members").getChildren();
                    for (DataSnapshot member : chatMembers) {
                        // If it's not the current user
                        if (!member.getKey().equals(mCurrentUserID)) {
                            // Gets the ID of that other member
                            mDirectUserID = member.getKey();
                            // Reference to that user to get his data
                            DatabaseReference userRef = mRootRef.child("Users").child(mDirectUserID);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Gets the name and image of the user
                                    mChatName = dataSnapshot.child("name").getValue(String.class);
                                    mChatImage = dataSnapshot.child("thumb_image").getValue(String.class);
                                    // Sets the name of the user as the title of the chat
                                    mTitleView.setText(mChatName);
                                    // Loads the user's image to the top
                                    if (!mChatImage.equals("")) {
                                        Picasso.with(getApplicationContext()).load(mChatImage).networkPolicy(NetworkPolicy.OFFLINE)
                                                .placeholder(R.drawable.generic).into(mProfileImage, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(getApplicationContext()).load(mChatImage).placeholder(R.drawable.generic).into(mProfileImage);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                            break;
                        }
                    }

                } else {
                    // Else, if it is a group chat or a channel
                    // Gets the chat's name and image
                    mChatName = dataSnapshot.child("chat_name").getValue(String.class);
                    mChatImage = dataSnapshot.child("chat_image").getValue(String.class);
                    // Sets the title of the chat to the chat's name
                    mTitleView.setText(mChatName);
                    // Loads the chat's image to the top
                    if (!mChatImage.equals("")) {
                        Picasso.with(getApplicationContext()).load(mChatImage).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.generic).into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(mChatImage).placeholder(R.drawable.generic).into(mProfileImage);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        // UI
        mAdapter = new PinsAdapter(messagesList, this, mChatID);

        mMessagesList = findViewById(R.id.chat_messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        // Loads the pinned messages
        loadMessages();
    }

    // Method for loading in all pinned messages
    private void loadMessages() {

        DatabaseReference pinnedRef = mRootRef.child("Chats").child(mChatID).child("pinned");
        pinnedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> pinnedMessages = dataSnapshot.getChildren();
                for (DataSnapshot pinnedMessage : pinnedMessages) {
                    final String messageKey = pinnedMessage.getKey();

                    DatabaseReference messageRef = mRootRef.child("Chats").child(mChatID).child("messages").child(messageKey);
                    messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Messages message = dataSnapshot.getValue(Messages.class);

                            // Set the Key value to the message - this is used in-app for deleting and pinning messages
                            message.setKey(messageKey);

                            // Adds the new message to the list
                            messagesList.add(message);
                            mAdapter.notifyDataSetChanged();

                            // Scroll the view to the bottom when a message is sent or received.
                            mMessagesList.scrollToPosition(messagesList.size() - 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
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
    public void onStart() {
        super.onStart();

        // Get the current user from Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If there is a current user, set them as online when the activity starts
        if (currentUser != null) {
            mRootRef.child("Users").child(mCurrentUserID).child("online").setValue("true");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Set the current user to offline with a timestamp when they leave the activity
        if (currentUser != null) {
            mRootRef.child("Users").child(mCurrentUserID).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    // Used by MessageAdapter to refresh messages after one of them has been edited/deleted
    protected void refreshMessages() {
        // Works fine for editing messages. Buggy with deleting
        messagesList.clear();
        loadMessages();
    }

}