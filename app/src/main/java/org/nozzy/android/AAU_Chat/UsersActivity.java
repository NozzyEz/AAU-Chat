package org.nozzy.android.AAU_Chat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        mUsersDatabase.child(mCurrentUserID).child("online").setValue("true");

        // We setup our Firebase recycler adapter with help from our Users class, a UsersViewHolder class, and the layout we have created to show users.
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            // This method is used to populate our RecyclerView with each of our users
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

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
        };

        // Finally we set the adapter for our recycler view
        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    // A ViewHolder class made for displaying a single user in the recycler view
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private String mStatus;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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
    protected void onPause() {
        super.onPause();
        mUsersDatabase.child(mCurrentUserID).child("online").setValue(ServerValue.TIMESTAMP);

    }
}
