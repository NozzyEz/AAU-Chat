package org.nozzy.android.AAU_Chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

// This is an activity to show the current user all users registered with the app, for now it has no
// functionality other than showing the information about the other users display name and their
// current status.
public class UsersActivity extends AppCompatActivity {

    // Initialization of UI as well as a database reference
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private EditText etSearch;

    public static String searchString = "";
    public static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Firstly we set up out toolbar
        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Here we setup our RecyclerView which we use to show all the users, one by one
        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        // and finally we point our database reference to the Users database
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        etSearch = findViewById(R.id.etSearch);
        setupSearchInput();

        //mAuth.getCurrentUser().getDisplayName();


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Sets the online value of the user back to true
        mUsersDatabase.child(mCurrentUserID).child("online").setValue("true");

        //username =  mUsersDatabase.child(mCurrentUserID).child("name");

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Gets all the relevant user's data from the database
                name = dataSnapshot.child(mCurrentUserID).child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        searchUser(searchString);

    }

    // A ViewHolder class made for displaying a single user in the recycler view
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout.LayoutParams params;

        View mView;
        RelativeLayout rlSingleUser;
        private String mStatus;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            rlSingleUser = itemView.findViewById(R.id.rlSingleUser);
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void hideLayout() {
            params.height = 0;
            rlSingleUser.setLayoutParams(params);
        }
        public void showLayout() {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            rlSingleUser.setLayoutParams(params);
        }


        // Sets the name for the name text field
        public void setName(String name) {
            TextView mUserNameView = mView.findViewById(R.id.user_single_name);
            mUserNameView.setText(name);
        }
        // Sets the status for the status text field
        public void setStatus(String status) {
            TextView mStatusView = mView.findViewById(R.id.user_single_status);
            mStatusView.setText(status);
        }
        // Sets the image for the image view
        public void setUserImage(final String userImage, final Context ctx) {
            final CircleImageView mUserImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(userImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.generic).into(mUserImageView, new Callback() {
                @Override
                public void onSuccess() { }
                @Override
                public void onError() {
                    Picasso.with(ctx).load(userImage).placeholder(R.drawable.generic).into(mUserImageView);
                }
            });
        }
    }

    @Override
    // Sets the online value to the current timestamp if the activity is paused
    protected void onPause() {
        super.onPause();
        mUsersDatabase.child(mCurrentUserID).child("online").setValue(ServerValue.TIMESTAMP);

    }

    private void setupSearchInput() {
        //Allows to search by pressing enter button on keyboard
        etSearch.clearFocus();
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    searchString = etSearch.getText().toString();
                    searchUser(searchString);
                    // Hides the keyboard
                    if (getWindow() != null && getWindow().getDecorView() != null && getWindow().getDecorView().getWindowToken() != null) {
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void searchUser(String searchString) {
        // Query is being passed with information from edit text
        Query searchQuery = mUsersDatabase.orderByChild("name").startAt(searchString).endAt(searchString + "\uf8ff");

        // We setup our Firebase recycler adapter with help from our Users class, a UsersViewHolder class, and the layout we have created to show users.
        final FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                searchQuery
        ) {
            @Override
            // This method is used to populate our RecyclerView with each of our users
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                if (name.equals(model.getName())) {
                    viewHolder.hideLayout();
                } else {
                    viewHolder.showLayout();

                    // Inside each view we retrieve the value for name, status and image with our Users class, and set it to the view as needed
                    viewHolder.setName(model.getName());
                    viewHolder.setStatus(model.getStatus());
                    viewHolder.setUserImage(model.getThumbImage(), getApplicationContext());

                    // Then we get the user ID for the view
                    final String user_id = getRef(position).getKey();

                    // When the user clicks on the view, we then pass the correct user ID to the intent
                    // that leads them to the ProfileActivity, so that we can show the correct information
                    // in that Activity
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("user_id", user_id);
                            startActivity(profileIntent);

                        }
                    });
                }
            }

        };
            // Finally we set the adapter for our recycler view
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        }
}
