package org.nozzy.android.AAU_Chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersFragment extends BaseFragment {

    // Initialization of UI as well as a database reference
    private static final String TAG = UsersFragment.class.getSimpleName();

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private EditText etSearch;

    public static String searchString = "";

    public static String getFragmentTitle() {
        return "All Users";
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_users;
    }

    public static String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Here we setup our RecyclerView which we use to show all the users, one by one
        mUsersList = getView().findViewById(R.id.users_list);

        final FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   ((MainActivity) getActivity()).changeContentFragment(getFragmentManager(), NewGroupChatFragment.getFragmentTag(), new NewGroupChatFragment(), R.id.flFragmentsContainer, true, NewGroupChatFragment.getFragmentTitle());
            }
        });

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));

        // and finally we point our database reference to the Users database
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        etSearch = getView().findViewById(R.id.etSearch);
        setupSearchInput();

    }
    @Override
    public void onStart() {
        super.onStart();

        searchUser(searchString);
    }

    private void setupSearchInput() {
        // Allows to search by pressing enter button on keyboard
        etSearch.clearFocus();
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    searchString = etSearch.getText().toString();
                    searchUser(searchString);
                    // Hides the keyboard
                    if (getActivity().getWindow() != null && getActivity().getWindow().getDecorView() != null && getActivity().getWindow().getDecorView().getWindowToken() != null) {
                        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void searchUser(String searchString) {
        // Query is being passed with information from EditText
        Query searchQuery = mFriendsDatabase.child(mCurrentUserID).orderByChild("name").startAt(searchString).endAt(searchString + "\uf8ff");

        // Adapts the users friends to the RecyclerView
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                searchQuery
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
                        final String userStatus = dataSnapshot.child("status").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        // Inside each view we retrieve the value for name, date and image with our Friends class, and set it to the view as needed
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setStatus(userStatus);
                        friendsViewHolder.setThumb(userThumb, getContext());

                        // Sets a listener so that when a friend is clicked, the user can choose to either view profile, or send a message
                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", list_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };

        // Finally, puts all of the friends into our RecyclerView
        mUsersList.setAdapter(friendsRecyclerViewAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mUsersDatabase.child(mCurrentUserID).child("online").setValue(ServerValue.TIMESTAMP);

    }
    @Override
    public void onResume(){
        super.onResume();
        searchUser(searchString);
    }

    // A ViewHolder class made for displaying a single friend in the recycler view
    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CheckBox checkBox;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            checkBox = itemView.findViewById(R.id.cbSelectUser);
        }
        // Sets the date for the date text field
        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
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
        public void changeState() {
            if (checkBox.isChecked())
                checkBox.setChecked(false);
            else
                checkBox.setChecked(true);
        }

    }
}
