package org.nozzy.android.AAU_Chat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.view.View.VISIBLE;


// This activity is used for chatting with other users.
public class ChatActivity extends AppCompatActivity {

    // Parameters of the chat
    private String mChatID;
    private String mChatType;
    private String mChatName;
    private String mChatImage;
    private String mChatRole;
    private ArrayList<String> mChatMembers = new ArrayList<>();
    private long mChatUserCount;

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
    private TextView tvPinedMessages;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    private RelativeLayout pinnedMessagesLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    // Variables for displaying messages
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    // Variable for detecting a gallery pick result
    private static final int MESSAGE_GALLERY_PICK = 1;
    private static final int CHAT_IMAGE_GALLERY_PICK = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;

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
        tvPinedMessages = findViewById(R.id.tvPinedMessages);
        pinnedMessagesLayout = findViewById(R.id.pinnedMessagesLayout);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);

        // Sets the name, image and online values
        // Adds a listener to get all the chat values
        mRootRef.child("Chats").child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Gets the user's role in the chat
                mChatRole = dataSnapshot.child("members").child(mCurrentUserID).getValue(String.class);

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
                            mChatMembers.add(member.getKey());
                            // Reference to that user to get his data
                            DatabaseReference userRef = mRootRef.child("Users").child(mChatMembers.get(0));
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
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                            break;
                        }
                    }

                } else {
                    // Else, if it is a group chat or a channel
                    // Gets the chat's name, image, as well as the count of all members
                    mChatUserCount = dataSnapshot.child("members").getChildrenCount();
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

                    // Get all members of the group
                    Iterable<DataSnapshot> members = dataSnapshot.child("members").getChildren();
                    for (DataSnapshot member : members)
                        if (!member.getKey().equals(mCurrentUserID))
                            mChatMembers.add(member.getKey());

                    // If the user is an admin
                    if (mChatRole.equals("admin")) {
                        // Adds a listener to the chat title to change it
                        mTitleView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showEditNameDialog(mChatName);
                            }
                        });
                        // Adds a listener to the chat image to change it
                        mProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Ask for permission to read storage data (needed for newer versions of Android)
                                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                                    // app-defined int constant that should be quite unique
                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                    return;
                                }
                                // Start the default gallery picker, which will lead into a cropper
                                Intent galleryIntent = new Intent();
                                galleryIntent.setType("image/*");
                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), CHAT_IMAGE_GALLERY_PICK);
                            }
                        });
                    }

                    // The online indicator displays the number of members in the chat instead
                    mLastSeenView.setText("Members: " + mChatUserCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        // UI
        mAdapter = new MessageAdapter(messagesList, this, mChatID);

        mMessagesList = findViewById(R.id.chat_messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);


        // Loads the first messages
        loadMessages();

        loadPinnedMessage();

        // Updates the last seen message of the current user
        updateSeen();

        // Button event for sending a message
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        pinnedMessagesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pinnedMessagesIntent = new Intent(getApplicationContext(), PinnedMessagesActivity.class);
                pinnedMessagesIntent.putExtra("chat_id", mChatID);
                startActivity(pinnedMessagesIntent);
            }
        });

        // Button event for sending an image
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask for permission to read storage data (needed for newer versions of Android)
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant that should be quite unique
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    return;
                }
                // Start the default gallery picker, which will compress and send the image
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), MESSAGE_GALLERY_PICK);
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
    // external storage, we startImageSelection it, compress it, and we store it with firebase.
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Checks if the activity was the image message gallery picker
        if (requestCode == MESSAGE_GALLERY_PICK && resultCode == RESULT_OK) {
            try {
                // Creates an image file (with the path of the cropped one) for compression
                File imageToCompress = new File(getPath(this, data.getData()));

                // Creates a compressor, loads the image onto it
                File compressedImage = new Compressor(ChatActivity.this)
                        .setQuality(25)
                        .compressToFile(imageToCompress);

                // Gets the Uri of the file for uploading
                Uri filePath = Uri.fromFile(compressedImage);

                // References to the messages and the notifications in the database
                final String messages_ref = "Chats" + "/" + mChatID + "/" + "messages";
                final String notification_ref = "Notifications" + "/";

                // Gets the semi-random key of the message about to be stored
                DatabaseReference user_message_push = mRootRef.child("Chats").child(mChatID).child("messages").push();
                final String push_id = user_message_push.getKey();

                // Attempts to upload the image to the storage
                StorageReference ref = mImageStorage.child("message_images").child("compressed").child(push_id + ".jpg");
                ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // If the upload was successful, gets the download url of the image
                        String download_url = taskSnapshot.getDownloadUrl().toString();

                        // A hashmap used for storing all message data
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", download_url);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);

                        // A Hashmap to store the notification
                        Map<String, Object> notifyMap = new HashMap<>();
                        notifyMap.put("from", mCurrentUserID);
                        notifyMap.put("type", "image");
                        notifyMap.put("chat_id", mChatID);

                        // Put the message and the notification into their corresponding tables
                        Map<String, Object> messageUserMap = new HashMap<>();
                        messageUserMap.put(messages_ref + "/" + push_id, messageMap);
                        for (String member : mChatMembers)
                            messageUserMap.put(notification_ref + member + "/" + push_id, notifyMap);

                        // Attempts to store all data in the database
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                // Reference to the seen value of the current user
                                final DatabaseReference seenRef = mRootRef.child("Chats").child(mChatID).child("seen").child(mCurrentUserID);
                                // Set the seen value to this new message
                                seenRef.setValue(push_id);

                                // If there is an error, output it into the log
                                if (databaseError != null) {
                                    Log.d("CHAT_LOG", databaseError.getMessage());
                                }
                            }
                        });

                        // Updates the chat's timestamp for each user
                        updateChatTimestamp();
                    }
                });

            } catch (IOException e) {
                // Shows a message if there was an error during compression
                e.printStackTrace();
                Toast.makeText(this, "Error in compression", Toast.LENGTH_LONG).show();
            }
        }


        // Checks if the activity was the chat image gallery picker - if so, start the cropper
        if (requestCode == CHAT_IMAGE_GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        // Checks if the activity was the image cropper
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            // Gets the result from the image cropper activity
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            try {
                // Gets the cropped image
                Uri imageUri = result.getUri();
                // Creates an image file (with the path of the cropped one) for compression
                final File imageToCompress = new File(imageUri.getPath());

                // Creates a compressor, loads the image onto it
                File compressedImage = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(25)
                        .compressToFile(imageToCompress);

                // Gets the Uri of the file for uploading
                Uri filePath = Uri.fromFile(compressedImage);

                // Attempts to upload the image to the storage
                StorageReference ref = mImageStorage.child("chat_images").child(mChatID + ".jpg");
                ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // If the upload was successful, gets the download url of the image
                        String download_url = taskSnapshot.getDownloadUrl().toString();
                        // Change the chat image value in the database
                        mRootRef.child("Chats").child(mChatID).child("chat_image").setValue(download_url);
                        Toast.makeText(ChatActivity.this, "Chat image changed", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                // Shows a message if there was an error during compression
                e.printStackTrace();
                Toast.makeText(this, "Error in compression", Toast.LENGTH_LONG).show();
            }

        }
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
                String messageKey = dataSnapshot.getKey();

                // Set the Key value to the message - this is used in-app for deleting and pinning messages
                message.setKey(messageKey);

                // If it's the first (oldest) message in that set
                if (itemPos == 0) {
                    // Set the last key and previous key to that message's key
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
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    // Method for loading in older messages when refreshing
    private void loadMoreMessages() {

        // Reference for getting messages
        DatabaseReference messagesRef = mRootRef.child("Chats").child(mChatID).child("messages");

        // Query for getting the specific 10 messages which end at the oldest currently displayed message
        // Note - actually loads in 11 messages, but the last one is a repeat, so it isn't shown.
        Query messageQuery = messagesRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD + 1);

        // For each of these messages
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            // When each message is added
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the text and id of the message
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                // Set the Key value to the message - this is used in-app for deleting and pinning messages
                message.setKey(messageKey);

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
                    mLinearLayout.scrollToPositionWithOffset(itemPos - 1, 0);
                } else {
                    mAdapter.notifyDataSetChanged();

                    // Stops the refreshing from continuing
                    mRefreshLayout.setRefreshing(false);
                    // Scrolls to the bottom of older messages, effectively showing you the first message
                    mLinearLayout.scrollToPositionWithOffset(itemPos - 1, 0);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadPinnedMessage() {

        DatabaseReference pinnedRef = mRootRef.child("Chats").child(mChatID).child("pinned");
        Query lastPinnedMessage = pinnedRef.limitToLast(1);
        lastPinnedMessage.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    DatabaseReference messageRef = mRootRef.child("Chats").child(mChatID).child("messages").child(dataSnapshot.getKey());
                    messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            pinnedMessagesLayout.setVisibility(VISIBLE);
                            tvPinedMessages.setText(dataSnapshot.child("message").getValue().toString());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



//
//        pinnedRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Iterable<DataSnapshot> pinnedMessages = dataSnapshot.getChildren();
//                for (DataSnapshot pinnedMessage : pinnedMessages) {
//                    final String messageKey = pinnedMessage.getKey();
//
//                    DatabaseReference messageRef = mRootRef.child("Chats").child(mChatID).child("messages").child(messageKey);
//                    messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Messages message = dataSnapshot.getValue(Messages.class);
//
//                            // Set the Key value to the message - this is used in-app for deleting and pinning messages
//                            message.setKey(messageKey);
//
//                            // If it's the first (oldest) message in that set
//                            if (itemPos == 0) {
//                                // Set the last key and previous key to that message's key
//                                mLastKey = messageKey;
//                                mPrevKey = messageKey;
//                            }
//
//                            // Increment the position so that other messages don't get counted as first
//                            itemPos++;
//
//                            // Adds the new message to the list
//                            messagesList.add(message);
//                            mAdapter.notifyDataSetChanged();
//
//                            // Scroll the view to the bottom when a message is sent or received.
//                            mMessagesList.scrollToPosition(messagesList.size() - 1);
//
//                            // Stops the refreshing
//                            mRefreshLayout.setRefreshing(false);
//                        }
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {}
//                    });
//
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//        });
    }

    // Method for sending a simple text message
    private void sendMessage() {

        // Gets the message text
        String message = mChatMessageView.getText().toString();
        // Checks if the message isn't empty
        if (!TextUtils.isEmpty(message)) {
            // Reference to the messages in the Chats table in the database and for the notifications table as well
            String messages_ref = "Chats" + "/" + mChatID + "/" + "messages";
            String notification_ref = "Notifications" + "/";

            // Gets the semi-random key of the message about to be stored
            DatabaseReference user_message_push = mRootRef.child("Chats").child(mChatID).child("messages").push();
            String push_id = user_message_push.getKey();

            // A hashmap for storing a message
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("from", mCurrentUserID);
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("type", "text");

            // A Hashmap to store the notification
            Map<String, Object> notifyMap = new HashMap<>();
            notifyMap.put("from", mCurrentUserID);
            notifyMap.put("type", "message");
            notifyMap.put("chat_id", mChatID);
            notifyMap.put("chat_message", message);

            // Put this message into the messages table inside the current chat
            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(messages_ref + "/" + push_id, messageMap);
            for (String member : mChatMembers)
                messageUserMap.put(notification_ref + member + "/" + push_id, notifyMap);

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

    private void showEditNameDialog(String oldName) {
        // Building the edit message dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_edit_message, null);
        dialogBuilder.setView(dialogView);

        // Edit text field for editing the message
        final EditText editText = dialogView.findViewById(R.id.edit1);
        editText.setText(oldName);

        // Sets the title of the dialog
        dialogBuilder.setTitle("Edit the chat name");
        // Sets the title and action of the "Done" button
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = editText.getText().toString();
                if (newName.equals(""))
                    Toast.makeText(ChatActivity.this, "Name can't be empty!", Toast.LENGTH_LONG).show();
                else {
                    mRootRef.child("Chats").child(mChatID).child("chat_name").setValue(newName);
                    mTitleView.setText(newName);
                    Toast.makeText(ChatActivity.this, "Chat name changed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Sets the title and action of the "Cancel" button
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        // Shows the dialog
        AlertDialog b = dialogBuilder.create();
        b.show();
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
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
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
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Used by MessageAdapter to refresh messages after one of them has been edited/deleted
    protected void refreshMessages() {
        // Works fine for editing messages. Buggy with deleting
        messagesList.clear();
        itemPos = 0;
//        mAdapter.notifyDataSetChanged();
        loadMessages();
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    private static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
