package org.nozzy.android.AAU_Chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
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

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersFragment extends BaseFragment {

    // Initialization of UI as well as a database reference
    private static final String TAG = UsersFragment.class.getSimpleName();

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private EditText etSearch;
    private TextView tvNoReporter;

    public static String searchString = "";
    public static String name;
    public static Boolean newGroupChat = false;

    private ArrayList<String> users = new ArrayList<>();


    @Override
    public String getFragmentTitle() {
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
        tvNoReporter = getView().findViewById(R.id.tvNoReporter);




        final FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Select Users to start a new newGroupChat", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                tvNoReporter.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_next_white);
               if(!newGroupChat){
                   newGroupChat = true;
               }
               if(!users.isEmpty()) {

                   UsersFragment fragment = new UsersFragment();
                   Bundle bundle = new Bundle();
                   bundle.putString("users", Arrays.toString(users.toArray()));
                   fragment.setArguments(bundle);
                   ((MainActivity) getActivity()).changeContentFragment(getFragmentManager(), NewGroupChatFragment.getFragmentTag(), new NewGroupChatFragment(), R.id.flFragmentsContainer, true);

               }
            }
        });


        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));

        // and finally we point our database reference to the Users database
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        etSearch = getView().findViewById(R.id.etSearch);
        setupSearchInput();

    }
    @Override
    public void onStart() {
        super.onStart();

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

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout.LayoutParams params;

        View mView;
        RelativeLayout rlSingleUser;
        private String mStatus;
        CheckBox checkBox;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;


            checkBox = itemView.findViewById(R.id.cbSelectUser);
            rlSingleUser = itemView.findViewById(R.id.rlSingleUser);
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        private void changeState() {
            if (checkBox.isChecked())
                checkBox.setChecked(false);
            else
                checkBox.setChecked(true);
        }

        private void hideLayout() {
            params.height = 0;
            rlSingleUser.setLayoutParams(params);
        }
        private void showLayout() {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            rlSingleUser.setLayoutParams(params);
        }


        // Sets the name for the name text field
        public void setName(String name) {
            TextView mUserNameView = mView.findViewById(R.id.user_single_name);
            mUserNameView.setText(name);
        }

//        private void addUser(final String userId,final Context ctx){
//            final CheckBox checkBox = mView.findViewById(R.id.cbSelectUser);
//            checkBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    final boolean isChecked = checkBox.isChecked();
//                    if(isChecked) {
//                        Toast.makeText(ctx, userId, Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//            });
//        }

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
    public void onPause() {
        super.onPause();
        mUsersDatabase.child(mCurrentUserID).child("online").setValue(ServerValue.TIMESTAMP);

    }
    @Override
    public void onResume(){
        super.onResume();
        newGroupChat = false;
        searchUser(searchString);
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
        // Query is being passed with information from edittext
        Query searchQuery = mUsersDatabase.orderByChild("name").startAt(searchString).endAt(searchString + "\uf8ff");

        // We setup our Firebase recycler adapter with help from our Users class, a UsersViewHolder class, and the layout we have created to show users.
        final FirebaseRecyclerAdapter<Users, UsersFragment.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersFragment.UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout_all_users,
                UsersFragment.UsersViewHolder.class,
                searchQuery
        ) {
            @Override
            // This method is used to populate our RecyclerView with each of our users
            protected void populateViewHolder(final UsersFragment.UsersViewHolder viewHolder, Users model, final int position) {

                if (name.equals(model.getName())) {
                    viewHolder.hideLayout();
                }
                //else {
                    //viewHolder.showLayout();

                    // Inside each view we retrieve the value for name, status and image with our Users class, and set it to the view as needed
                    viewHolder.setName(model.getName());
                    viewHolder.setStatus(model.getStatus());
                    viewHolder.setUserImage(model.getThumbImage(), getActivity().getApplicationContext());

                    // Then we get the user ID for the view
                    final String user_id = getRef(position).getKey();

                    // When the user clicks on the view, we then pass the correct user ID to the intent
                    // that leads them to the ProfileActivity, so that we can show the correct information
                    // in that Activity
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (!newGroupChat) {
                                Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);
                                startActivity(profileIntent);
                            } else {
                                //Toast.makeText(getActivity(),user_id,Toast.LENGTH_SHORT).show();
                                viewHolder.checkBox.setVisibility(View.VISIBLE);
                                if(users.contains(user_id)){
                                    users.remove(users.indexOf(user_id));
                                } else {
                                    users.add(user_id);
                                }
                               Log.e ("List",(Arrays.toString(users.toArray())));
                                viewHolder.changeState();

                            }

                        }
                    });


               // }
            }
        };
        // Finally we set the adapter for our recycler view
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }
}
