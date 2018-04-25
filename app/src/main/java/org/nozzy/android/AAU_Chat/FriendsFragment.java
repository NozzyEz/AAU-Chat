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


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = mMainView.findViewById(R.id.friend_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends friends, int position) {


                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setDate(friends.getDate());
                        friendsViewHolder.setThumb(userThumb, getContext());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // Click Event for each item
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

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setOnline(userOnline);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    private void sendToChat(String list_user_id, String userName) {
        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.putExtra("user_id", list_user_id);
        chatIntent.putExtra("user_name", userName);
        startActivity(chatIntent);
    }

    private void sendToProfile(String list_user_id) {
        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
        profileIntent.putExtra("user_id", list_user_id);
        startActivity(profileIntent);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText("Friends since: " + date);
        }

        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setThumb(final String thumb, final Context ctx) {

            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.generic).into(userImageView, new Callback() {


                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb).placeholder(R.drawable.generic).into(userImageView);
                }
            });

        }
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
