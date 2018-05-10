package org.nozzy.android.AAU_Chat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mMembersDatabase;
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
        return "Requests";
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
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrent_user_id).child("chats");
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

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
                mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(list_chat_id).child("messages");

                // By default, the message box will say that there are no messages
                // This gets replaced by the following query which tries to get the last message
                convViewHolder.setMessage("No messages yet.");
                
                // Query to get the last message in a conversation
                Query lastMessageQuery = mMessageDatabase.limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // Gets the last message and puts it into the RecyclerView
                        String message = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(message);

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

                // Adds a listener to get the type of the chat
                getRef(i).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Gets the type of the chat
                        final String chatType = dataSnapshot.getValue(String.class);

                        // Reference to the chat name
                        DatabaseReference chatNameRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(list_chat_id).child("chatName");
                        chatNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Gets the name of the chat
                                    final String chatName = dataSnapshot.getValue().toString();
//                                    convViewHolder.setName(chatName);

                                // Reference to all of the members in the conversation
                                mMembersDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(list_chat_id).child("members");
                                mMembersDatabase.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        // Gets the id of each member
                                        final String memberId = dataSnapshot.getKey();
                                        // Checks if that member is not the current user
                                        if (!memberId.equals(mCurrent_user_id)) {
                                            // Goes to the Users reference for that specific user
                                            mUsersDatabase.child(memberId).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    // Gets the name and image of the user
                                                    final String userName = dataSnapshot.child("name").getValue().toString();
                                                    String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                                                    // If the chat is direct, set the name to the other user's name
                                                    if (chatType.equals("direct"))
                                                        convViewHolder.setName(userName);
                                                    // Else, set the name to the group chat's name
                                                    else convViewHolder.setName(chatName);

                                                    // If the chat is direct, set the image to the other user's image
                                                    if (chatType.equals("direct"))
                                                        convViewHolder.setUserImage(userThumb, getContext());
                                                    else {
                                                        // Else, set the image to the group chat's image
                                                        DatabaseReference chatImageRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(list_chat_id).child("chatImage");
                                                        chatImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                // Chack to see if there is an image to assign before doing so
                                                                if (dataSnapshot.hasChild("chatImage")) {
                                                                    convViewHolder.setUserImage(dataSnapshot.getValue().toString(), getContext());
                                                                }
                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) { }
                                                        });
                                                    }





                                                    // Get the online status of the user and set the online indicator accordingly
                                                    String userOnline = dataSnapshot.child("online").getValue().toString();
                                                    convViewHolder.setUserOnline(userOnline);

                                                    // Whenever a conversation is clicked, it should lead to that chat
                                                    convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                            chatIntent.putExtra("chat_id", list_chat_id);
                                                            chatIntent.putExtra("chat_type", chatType);
                                                            chatIntent.putExtra("chat_name", chatName);
                                                            chatIntent.putExtra("user_id", memberId);
                                                            chatIntent.putExtra("user_name", userName);
                                                            startActivity(chatIntent);
                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { }
                                            });
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
                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });





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
        public void setMessage(String message){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);
        }

        // Sets the name for the name text field
        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        // Sets the image for the image view
        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.generic).into(userImageView);

        }

        // Sets the online indicator's visibility
        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.user_online_indicator);
            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }



}