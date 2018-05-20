package org.nozzy.android.AAU_Chat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

// This fragment shows all current conversations in order of recency

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends BaseFragment {

    // UI
    private RecyclerView mConvList;

    private static final String TAG = ChatsFragment.class.getSimpleName();

    private View mMainView;

    // Firebase
    private DatabaseReference mRootDatabase;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mChatsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_requests;
    }
    public static String getFragmentTag() {
        return TAG;
    }

    @Override
    public String getFragmentTitle() {
        return "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The main view which holds all the fragments
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        // RecyclerView setup
        mConvList = mMainView.findViewById(R.id.conv_list);
        mConvList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // Authentication to get current user
        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        // Database reference setup, keeps them synced for offline use
        mRootDatabase = FirebaseDatabase.getInstance().getReference();
        mConvDatabase = mRootDatabase.child("Users").child(mCurrent_user_id).child("chats");
        mConvDatabase.keepSynced(true);
        mUsersDatabase = mRootDatabase.child("Users");
        mUsersDatabase.keepSynced(true);
        mChatsDatabase = mRootDatabase.child("Chats");
        mChatsDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        // Query to get the conversations in order of last activity
        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        // We setup our Firebase recycler adapter with help from our Conv class, a ConvViewHolder class, and the layout we have created to show conversations.
        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            // This method is used to populate our RecyclerView with each of our conversations
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {

                // Gets the id of the chat
                final String list_chat_id = getRef(i).getKey();

                // Database reference to the messages
                DatabaseReference messageDatabase = mChatsDatabase.child(list_chat_id).child("messages");

                // By default, the message box will say that there are no messages
                // This gets replaced by the following query which tries to get the last message
                convViewHolder.setMessage("No messages yet.", true);

                // Query to get the last message in a conversation
                Query lastMessageQuery = messageDatabase.limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // Gets the last message text
                        final String message;
                        if (dataSnapshot.child("message").getValue().toString().startsWith("https://firebasestorage.googleapis.com/")) {
                            message = "Image uploaded.";
                        } else  message = dataSnapshot.child("message").getValue().toString();

                        // Gets the key of the message
                        final String messageKey = dataSnapshot.getKey();

                        // Listener to check if the user has seen the last message
                        final DatabaseReference seenDatabase = mChatsDatabase.child(list_chat_id).child("seen");
                        seenDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Checks if the seen table contains the current user
                                if (dataSnapshot.hasChild(mCurrent_user_id)) {
                                    // If the user's last seen message is equal to the last message in the chat, make it normal
                                    if (dataSnapshot.child(mCurrent_user_id).getValue().toString().equals(messageKey))
                                        convViewHolder.setMessage(message, true);
                                        // Otherwise, make it bold
                                    else convViewHolder.setMessage(message, false);
                                } else {
                                    // If the user doesn't have a seen value yet, create a blank one
                                    seenDatabase.child(mCurrent_user_id).setValue("");
                                    convViewHolder.setMessage(message, false);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
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

                // Reference to get all the chat details
                DatabaseReference chatRef = mChatsDatabase.child(list_chat_id);
                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Gets the type of the chat
                        final String chatType = dataSnapshot.child("chat_type").getValue(String.class);

                        // Checks if the type is "direct"
                        if (chatType.equals("direct")) {
                            // Goes through all members, gets the one that is not the current user
                            Iterable<DataSnapshot> chatMembers = dataSnapshot.child("members").getChildren();
                            for (DataSnapshot member: chatMembers) {
                                if (!member.getKey().equals(mCurrent_user_id)) {
                                    // Gets the ID of that other member
                                    final String directMemberID = member.getKey();

                                    // A listener is added so we can get that member's data
                                    DatabaseReference userRef = mUsersDatabase.child(directMemberID);
                                    userRef.keepSynced(true);
                                    userRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // Gets the name, image and online values of the member
                                            final String directMemberName = dataSnapshot.child("name").getValue().toString();
                                            final String directMemberImage = dataSnapshot.child("thumb_image").getValue().toString();
                                            final String directMemberOnline = dataSnapshot.child("online").getValue().toString();

                                            // Sets the name, image and the online value accordingly
                                            convViewHolder.setName(directMemberName);
                                            convViewHolder.setImage(directMemberImage, getContext());
                                            convViewHolder.setUserOnline(directMemberOnline);

                                            // Whenever a conversation is clicked, it should lead to that chat
                                            convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("chat_id", list_chat_id);
                                                    startActivity(chatIntent);
                                                }
                                            });
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });
                                    break;
                                }
                            }
                        } else {
                            // Else, if the chat is not direct, get the name and image from the database
                            final String chatName = dataSnapshot.child("chat_name").getValue(String.class);
                            final String chatImage = dataSnapshot.child("chat_image").getValue(String.class);

                            // Set the name and image accordingly
                            convViewHolder.setName(chatName);
                            convViewHolder.setImage(chatImage, getContext());
                            convViewHolder.setUserOnline("false");

                            // Whenever a conversation is clicked, it should lead to that chat
                            convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("chat_id", list_chat_id);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
        };

        // Finally we set the adapter for our recycler view
        mConvList.setAdapter(firebaseConvAdapter);
    }

    // A ViewHolder class made for displaying a single conversation in the recycler view
    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        // Sets the message for the message text field
        // If the message wasn't seen, set the text to bold
        public void setMessage(String message, boolean isSeen){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            if(!isSeen)
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            else
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
        }

        // Sets the name for the name text field
        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        // Sets the image for the image view
        public void setImage(String thumb_image, Context ctx){
            if (!thumb_image.equals("")) {
                CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
                Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.generic).into(userImageView);
            }
        }

        // Sets the online indicator's visibility
        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.user_online_indicator);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.GONE);
            }

        }


    }



}