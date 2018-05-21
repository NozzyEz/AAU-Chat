package org.nozzy.android.AAU_Chat;

import android.support.v4.app.Fragment;

// This fragment shows all of the user's friends in a recycler view
/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends BaseFragment {

    // UI

    public FriendsFragment() {
        // Required empty public constructor
    }

    private static final String TAG = FriendsFragment.class.getSimpleName();

    public static String getFragmentTitle() {
        return "Email";
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_friends;
    }
    public static String getFragmentTag() {
        return TAG;
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//
//
//
//    }

    @Override
    public void onStart() {
        super.onStart();

        // Adapts the users friends to the RecyclerView
//        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
//
//                Friends.class,
//                R.layout.users_single_layout,
//                FriendsViewHolder.class,
//                mFriendsDatabase
//        ) {
//            @Override
//            // This method is used to populate our RecyclerView with each of our friends
//            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends friends, int position) {
//
//                // Gets the ID of the friend
//                final String list_user_id = getRef(position).getKey();
//
//                // Adds a listener to each friend in the database in order to update the list in real time
//                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                        final String userName = dataSnapshot.child("name").getValue().toString();
//                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
//
//                        // Inside each view we retrieve the value for name, date and image with our Friends class, and set it to the view as needed
//                        friendsViewHolder.setName(userName);
//                        friendsViewHolder.setDate(friends.getDate());
//                        friendsViewHolder.setThumb(userThumb, getContext());
//
//                        // Sets a listener so that when a friend is clicked, the user can choose to either view profile, or send a message
//                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//
//                                // The selection will have two options - view profile or chat
//                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
//                                // An alert dialog is displayed with these two options
//                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                builder.setTitle("Select Options");
//                                builder.setItems(options, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        // Click event for each item: 0 for viewing profile, 1 for chatting
//                                        switch (i) {
//                                            case 0:
//                                                sendToProfile(list_user_id);
//                                                break;
//                                            case 1:
//                                                sendToChat(list_user_id);
//                                                break;
//                                        }
//                                    }
//                                });
//                                builder.show();
//                            }
//                        });
//
//                        // Set the online indicator to online if the friend is online
//                        if (dataSnapshot.hasChild("online")) {
//                            String userOnline = dataSnapshot.child("online").getValue().toString();
//                            friendsViewHolder.setOnline(userOnline);
//                        }
//                    }
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) { }
//                });
//            }
//        };

        // Finally, puts all of the friends into our RecyclerView
      //  mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    // Sends the user to the ChatActivityOld where they can chat with the selected friend
    // Creates a chat room with the user
//    private void sendToChat(String list_user_id) {
//
//        // Generates chat ID
//        mRootRef = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference chat_push = mRootRef.child("Chats").push();
//        final String push_id = chat_push.getKey();
//
//        // Adding the chat with timestamp to the Users table
//        mRootRef.child("Users").child(mCurrent_user_id).child("chats").child(push_id).child("timestamp").setValue(ServerValue.TIMESTAMP);
//        mRootRef.child("Users").child(list_user_id).child("chats").child(push_id).child("timestamp").setValue(ServerValue.TIMESTAMP);
//
//        // Creating the chat in the Chats table with members, name, type, image and seen values
//        mRootRef.child("Chats").child(push_id).child("members").child(mCurrent_user_id).setValue("admin");
//        mRootRef.child("Chats").child(push_id).child("members").child(list_user_id).setValue("admin");
//        mRootRef.child("Chats").child(push_id).child("chat_name").setValue("New Chat");
//        mRootRef.child("Chats").child(push_id).child("chat_type").setValue("direct");
//        mRootRef.child("Chats").child(push_id).child("chat_image").setValue("");
//
//        mRootRef.child("Chats").child(push_id).child("seen").child(list_user_id).setValue("");
//        mRootRef.child("Chats").child(push_id).child("seen").child(mCurrent_user_id).setValue("");
//
//        // Passing variables and starting ChatActivity
//        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
//        chatIntent.putExtra("chat_id", push_id);
//        startActivity(chatIntent);
//    }

    // Sends the user to the ProfileActivity where they can view the selected friends profile
//    private void sendToProfile(String list_user_id) {
//        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
//        profileIntent.putExtra("user_id", list_user_id);
//        startActivity(profileIntent);
//    }
//
    // A ViewHolder class made for displaying a single friend in the recycler view
//    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
//
//        View mView;
//
//        public FriendsViewHolder(View itemView) {
//            super(itemView);
//            mView = itemView;
//        }
//        // Sets the date for the date text field
//        public void setDate(String date) {
//            TextView userStatusView = mView.findViewById(R.id.user_single_status);
//            userStatusView.setText("Friends since: " + date);
//        }
//        // Sets the name for the name text field
//        public void setName(String name) {
//            TextView userNameView = mView.findViewById(R.id.user_single_name);
//            userNameView.setText(name);
//        }
//        // Sets the thumbnail image for the image view
//        public void setThumb(final String thumb, final Context ctx) {
//            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
//            Picasso.with(ctx).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.generic).into(userImageView, new Callback() {
//                @Override
//                public void onSuccess() { }
//                @Override
//                public void onError() {
//                    Picasso.with(ctx).load(thumb).placeholder(R.drawable.generic).into(userImageView);
//                }
//            });
//        }
//        // Makes the online indicator visible/invisible
//    }
}
