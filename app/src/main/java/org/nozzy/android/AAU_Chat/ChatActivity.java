package org.nozzy.android.AAU_Chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


// This activity is used for chatting with other users.
public class ChatActivity extends AppCompatActivity {

    // ID of the user being chatted with
    // It will only be used if the type of the chat is "direct"
    private String mDirectUserID;

    // Parameters of the chat
    private String mChatID;
    private String mChatType;
    private String mChatName;
    private String mChatImage;

    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;

    private ChildEventListener mMessageAddedListener;
    private DatabaseReference mMessageRef;

    // UI
    private Toolbar mChatToolbar;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    // Variables for displaying messages
    private static final int  TOTAL_ITEMS_TO_LOAD = 10;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    // Variable for detecting a gallery pick result
    private static final int GALLERY_PICK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Setting up the Firebase references
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        // Storage reference for storing images
        mImageStorage = FirebaseStorage.getInstance().getReference();

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

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList, this);

        mMessagesList = findViewById(R.id.chat_messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);


        // Sets the name, image and online values
        mRootRef.child("Chats").child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatType = dataSnapshot.child("chat_type").getValue(String.class);
                if (mChatType.equals("direct")) {
                    Iterable<DataSnapshot> chatMembers = dataSnapshot.child("members").getChildren();
                    for (DataSnapshot member: chatMembers) {
                        if (!member.getKey().equals(mCurrentUserID)) {
                            // Gets the ID of that other member
                            mDirectUserID = member.getKey();
                            DatabaseReference userRef = mRootRef.child("Users").child(mDirectUserID);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mChatName = dataSnapshot.child("name").getValue(String.class);
                                    mChatImage = dataSnapshot.child("image").getValue(String.class);
                                    mTitleView.setText(mChatName);
                                    // Loads the thumbnail image to the top
                                    if (!mChatImage.equals("")) {
                                        Picasso.with(getApplicationContext()).load(mChatImage).networkPolicy(NetworkPolicy.OFFLINE)
                                                .placeholder(R.drawable.generic).into(mProfileImage, new Callback() {
                                            @Override
                                            public void onSuccess() { }
                                            @Override
                                            public void onError() {
                                                Picasso.with(getApplicationContext()).load(mChatImage).placeholder(R.drawable.generic).into(mProfileImage);
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                            // Adds a listener to the user being chatted with for setting their current online state
                            userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Gets either a true value or the timestamp of the last time the user was online
                                    String online = dataSnapshot.child("online").getValue().toString();
                                    // If the user is online, set the text at the top to 'Online'
                                    if (online.equals("true")) {
                                        mLastSeenView.setText("Online");
                                    } else {
                                        // If the user isn't online, turns the timestamp into a long value
                                        long lastTime = Long.parseLong(online);
                                        // Converts it into a readable format, sets the text at the top to that value
                                        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                                        mLastSeenView.setText(lastSeenTime);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                            break;
                        }
                    }

                } else {
                    // Else, if it is a group chat or a channel
                    mChatName = dataSnapshot.child("chat_name").getValue(String.class);
                    mChatImage = dataSnapshot.child("chat_image").getValue(String.class);
                    mTitleView.setText(mChatName);
                    // Loads the thumbnail image to the top
                    if (!mChatImage.equals("")) {
                        Picasso.with(getApplicationContext()).load(mChatImage).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.generic).into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() { }
                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(mChatImage).placeholder(R.drawable.generic).into(mProfileImage);
                            }
                        });
                    }
                    // TODO set the text to the number of members in the group
                    mLastSeenView.setText("Group chat");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });


        // Loads the first messages
        loadMessages();

        // Updates the last seen message of the current user
        updateSeen();

        // Button event for sending a message
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // Button event for sending an image
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
            }
        });

        // Sets a listener whenever we refresh for older messages
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Increments current page of messages loaded
                itemPos = 0;
                // Loads older messages
                loadMoreMessages();
            }
        });
    }

    @Override
    // We use this method when an image is being picked, in here the user picks an image from their
    // external storage, we crop it, compress it, and we store it with firebase.
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        // Checks if the activity was the default gallery picker - if so, start the cropper
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).start(this);
        }

        // Checks if the activity was the image cropper
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            // Reference to the messages in the database
            final String messages_ref = "Chats" + "/" + mChatID + "/" + "messages";
            final String notification_ref = "Notifications/" + mDirectUserID;


            // Gets the semi-random key of the message about to be stored
            DatabaseReference user_message_push = mRootRef.child("Chats").child(mChatID).child("messages").push();
            final String push_id = user_message_push.getKey();

            // Gets the result from the image cropper activity
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            // Gets the cropped image
            Uri imageUri = result.getUri();
            // Creates an image file (with the path of the cropped one) for compression
            final File imageToCompress = new File(imageUri.getPath());

            // A byte array for storing the image
            byte[] imageByteArray = new byte[0];
            try {
                // Creates a compressor, loads the image onto it
                Bitmap compressedImageBitmap = new Compressor(this)
                        .setQuality(25)
                        .compressToBitmap(imageToCompress);
                // After creating a new bitmap we then need a byte array output stream
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // We use the Bitmap.compress() method to write our compressed image into a byte array output stream
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                //And with the byte array output stream we can store it back in our byte array
                imageByteArray = baos.toByteArray();
            } catch (IOException e) {
                // Shows a message if there was an error during compression
                e.printStackTrace();
                Toast.makeText(this, "Error in compression", Toast.LENGTH_LONG).show();
            }

            // Before we can upload the compressed image we have to tell our app where in the storage we want to put it
            StorageReference compressedFilePath = mImageStorage.child("message_images").child("compressed").child(push_id + ".jpg");

            // And once that is done, we use an UploadTask to upload the compressed image
            UploadTask uploadTask = compressedFilePath.putBytes(imageByteArray);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    // And in its onCompleteListener we retrieve the URL of the image if the task was successful
                    if (task.isSuccessful()) {
                        String download_url = task.getResult().getDownloadUrl().toString();

                        // Which we then store in the database entry for the message that is being sent
                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);

                        // A Hashmap to store the notification
                        Map notifyMap = new HashMap();
                        notifyMap.put("from", mCurrentUserID);
                        notifyMap.put("type", "message");

                        // Put this message into the messages table inside the current chat
                        Map messageUserMap = new HashMap();
                        messageUserMap.put(messages_ref + "/" + push_id, messageMap);
                        messageUserMap.put(notification_ref + "/" + push_id, notifyMap);


                        // Attempts to store all data in the database
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                // If there is an error, output it into the log
                                if (databaseError != null) {
                                    Log.d("CHAT_LOG", databaseError.getMessage());
                                }
                            }
                        });

                        // Updates the chat's timestamp for each user
                        updateChatTimestamp();
                    }
                }
            });
        }
    }

    // Method for loading in older messages when refreshing
    private void loadMoreMessages() {

        // Reference for getting messages
        DatabaseReference messagesRef = mRootRef.child("Chats").child(mChatID).child("messages");

        // Query for getting the specific 10 messages which end at the oldest currently displayed message
        // Note - actually loads in 11 messages, but the last one is a repeat, so it isn't shown.
        Query messageQuery = messagesRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD+1);

        // For each of these messages
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            // When each message is added
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the text and id of the message
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                // Loads the message if it's not the last one (since that one is already on the screen)
                // If it is the last one, set the previous last key to the new last one
                if (!messageKey.equals(mPrevKey)) {
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                // If it's the first message, set the new last key to that message's key
                // Note - this does not affect the current query which goes until the last key, only the next one
                if (itemPos == 1) {
                    mLastKey = messageKey;

                    mAdapter.notifyDataSetChanged();

                    // Stops the refreshing from continuing
                    mRefreshLayout.setRefreshing(false);
                    // Scrolls to the bottom of older messages, effectively showing you the first message
                    mLinearLayout.scrollToPositionWithOffset(itemPos - 1,0);
                } else {
                    mAdapter.notifyDataSetChanged();

                    // Stops the refreshing from continuing
                    mRefreshLayout.setRefreshing(false);
                    // Scrolls to the bottom of older messages, effectively showing you the first message
                    mLinearLayout.scrollToPositionWithOffset(itemPos - 1,0);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


    // Method for loading in the first messages
    private void loadMessages() {

        // Reference for getting messages
        DatabaseReference messageRef = mRootRef.child("Chats").child(mChatID).child("messages");

        // A query to load the last 10 messages, from the oldest to the newest one
        Query messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

        // Adds a listener to messages
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            // Triggers whenever a new message is added
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                // If it's the first (oldest) message in that set
                if (itemPos == 0) {
                    // Set the last key and previous key to that message's key
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                // Increment the position so that other messages don't get counted as first
                itemPos++;

                // Adds the new message to the list
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                // Scroll the view to the bottom when a message is sent or received.
                mMessagesList.scrollToPosition(messagesList.size() - 1);

                // Stops the refreshing
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    // Method for sending a simple text message
    private void sendMessage() {

        // Gets the message text
        String message = mChatMessageView.getText().toString();
        // Checks if the message isn't empty
        if (!TextUtils.isEmpty(message)) {
            // Reference to the messages in the Chats table in the database and for the notifications table as well
            String messages_ref = "Chats/" + mChatID + "/" + "messages";
            String notification_ref = "Notifications/" + mDirectUserID;

            // Gets the semi-random key of the message about to be stored
            DatabaseReference user_message_push = mRootRef.child("Chats").child(mChatID).child("messages").push();
            String push_id = user_message_push.getKey();

            // A hashmap for storing a message
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);

            // A Hashmap to store the notification
            Map notifyMap = new HashMap();
            notifyMap.put("from", mCurrentUserID);
            notifyMap.put("type", "message");

            // Put this message into the messages table inside the current chat
            Map messageUserMap = new HashMap();
            messageUserMap.put(messages_ref + "/" + push_id, messageMap);
            messageUserMap.put(notification_ref + "/" + push_id, notifyMap);

            // Refreshes the text window to be empty
            mChatMessageView.setText("");

            // Attempts to put all data into the database
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage());
                    }
                }
            });

            // Updates the chat's timestamp for each user
            updateChatTimestamp();
        }
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

        mMessageRef.removeEventListener(mMessageAddedListener);
    }

    // Method used for updating the chat's timestamp for each user
    private void updateChatTimestamp() {
        // Reference for getting members
        DatabaseReference membersRef = mRootRef.child("Chats").child(mChatID).child("members");

        // For each user, update this chat's timestamp value
        membersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Gets the ID of each user
                String userID = dataSnapshot.getKey();
                // Updates the timestamp value representing recent activity
                mRootRef.child("Users").child(userID).child("chats").child(mChatID).child("timestamp").setValue(ServerValue.TIMESTAMP);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    // Method used for updating seen values
    private void updateSeen() {
        // Reference to the seen value of the current user
        final DatabaseReference seenRef = mRootRef.child("Chats").child(mChatID).child("seen").child(mCurrentUserID);

        // Reference to all messages of this chat
        mMessageRef = mRootRef.child("Chats").child(mChatID).child("messages");

        // Query to get the last message added
        Query lastMessageQuery = mMessageRef.limitToLast(1);

        mMessageAddedListener = lastMessageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Set the seen value to that new message
                seenRef.setValue(dataSnapshot.getKey());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
