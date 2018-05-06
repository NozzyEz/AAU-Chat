package org.nozzy.android.AAU_Chat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


// This fragment shows all of the user's friends in a recycler view.
/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends BaseFragment {

    // UI
    private RecyclerView mFriendsList;

    private View mMainView;

    // Firebase
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    public FriendsFragment() {
        // Required empty public constructor
    }

    private static final String TAG = FriendsFragment.class.getSimpleName();


    @Override
    public String getFragmentTitle() {
        return "Friends";
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_friends;
    }
    public static String getFragmentTag() {
        return TAG;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // The main view which holds all the fragments
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        // RecyclerView setup
        mFriendsList = mMainView.findViewById(R.id.friend_list);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Authentication to get current user
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        // Database reference setup, keeps them synced for offline use
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Adapts the users friends to the RecyclerView
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            // This method is used to populate our RecyclerView with each of our friends
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends friends, int position) {

                // Gets the ID of the friend
                final String list_user_id = getRef(position).getKey();

                // Adds a listener to each friend in the database in order to update the list in real time
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        // Inside each view we retrieve the value for name, date and image with our Friends class, and set it to the view as needed
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setDate(friends.getDate());
                        friendsViewHolder.setThumb(userThumb, getContext());

                        // Sets a listener so that when a friend is clicked, the user can choose to either view profile, or send a message
                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // The selection will have two options - view profile or chat
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                // An alert dialog is displayed with these two options
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Click event for each item: 0 for viewing profile, 1 for chatting
                                        switch (i) {
                                            case 0:
                                                sendToProfile(list_user_id);
                                                break;
                                            case 1:
                                                sendToChat(list_user_id, userName);
                                                break;
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                        // Set the online indicator to online if the friend is online
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setOnline(userOnline);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
        };

        // Finally, puts all of the friends into our RecyclerView
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    // Sends the user to the ChatActivity where they can chat with the selected friend
    private void sendToChat(String list_user_id, String userName) {
        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.putExtra("user_id", list_user_id);
        chatIntent.putExtra("user_name", userName);
        startActivity(chatIntent);
    }

    // Sends the user to the ProfileActivity where they can view the selected friends profile
    private void sendToProfile(String list_user_id) {
        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
        profileIntent.putExtra("user_id", list_user_id);
        startActivity(profileIntent);
    }

    // A ViewHolder class made for displaying a single friend in the recycler view
    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        // Sets the date for the date text field
        public void setDate(String date) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText("Friends since: " + date);
        }
        // Sets the name for the name text field
        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        // Sets the thumbnail image for the image view
        public void setThumb(final String thumb, final Context ctx) {
            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.generic).into(userImageView, new Callback() {
                @Override
                public void onSuccess() { }
                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb).placeholder(R.drawable.generic).into(userImageView);
                }
            });
        }
        // Makes the online indicator visible/invisible
        public void setOnline(String online) {
            ImageView userOnlineView = mView.findViewById(R.id.user_online_indicator);
            if(online.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
