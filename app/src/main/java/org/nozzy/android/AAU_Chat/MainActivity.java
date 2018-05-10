package org.nozzy.android.AAU_Chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;

// This is our main activity, where most of the app foundation is laid down, we have our toolbar
// with an options menu, a tabview where we can switch between different tabs.
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Initializing the Firebase Authentication, the default toolbar, the viewpager 'toolbar' as
    // well as the tab layout for Requests, Chats and friends.
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

//    private ViewPager mViewPager;
//    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;

  //  private TabLayout mTabLayout;

    private BaseFragment currentFragment;

    // Nav bar initialization
    private ImageView mHeaderBackground;
    private CircleImageView mProfileThumb;
    private TextView mProfileName;
    private TextView mProfileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the current instance of our authentication system
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            sendToStart();
        } else {
            changeContentFragment(getSupportFragmentManager(), ChatsFragment.getFragmentTag(), new ChatsFragment(), R.id.flFragmentsContainer, false);
        }



        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AAU Chat");


        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        // TODO: Comemnting
        final NavigationView navigationView = findViewById(R.id.nav_view);

        final View header = navigationView.getHeaderView(0);

        // Assigning our nav bar items to their views in the side nav bar
        mHeaderBackground = header.findViewById(R.id.nav_bar_background);
        mProfileThumb = header.findViewById(R.id.nav_bar_profile_image);
        mProfileName = header.findViewById(R.id.nav_profile_name);
        mProfileInfo = header.findViewById(R.id.nav_profile_info);


        navigationView.setNavigationItemSelectedListener(this);

        if (mAuth.getCurrentUser() != null) {
            // Point our database reference to the current user's ID, so that we can manipulate fields within
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Gets all the relevant user's data from the database
                    String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    String info = dataSnapshot.child("status").getValue().toString();
                    final String thumbnail = dataSnapshot.child("thumb_image").getValue().toString();


                    // Set the background to a blurred profile image
                    Picasso.with(getApplicationContext()).load(image).transform(new BlurTransformation(getApplicationContext(), 10, 10))
                            .networkPolicy(NetworkPolicy.OFFLINE).into(mHeaderBackground, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(image).transform(new BlurTransformation(getApplicationContext(), 10, 10)).into(mHeaderBackground);
                        }
                    });




                    // Updates the name and info fields based on information in the database
                    mProfileName.setText(name);
                    mProfileInfo.setText(info);

                    // If the user's database entry for image is not 'default' we load their image into the UI, but with our generic image in place as a placeholder
                    if(!thumbnail.equals("default")) {
                        // We use the Picasso library to do the image loading, this way we can store the image offline for faster loading
                        Picasso.with(getApplicationContext()).load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.generic).into(mProfileThumb, new Callback() {
                            @Override
                            public void onSuccess() { }
                            @Override
                            public void onError() {
                                // If the image fails to load, set the image to the default one
                                Picasso.with(getApplicationContext()).load(thumbnail).placeholder(R.drawable.generic).into(mProfileThumb);
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Get the device token from firebase
            String deviceToken = FirebaseInstanceId.getInstance().getToken();
            // And put that token into the current users database entry, so that it is updated whenever the user opens the app
            mUserRef.child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }

        // Setting up our tabs
        //mViewPager = findViewById(R.id.main_tabpager);
        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //mViewPager.setAdapter(mSectionsPagerAdapter);
        //mTabLayout = findViewById(R.id.main_tabs);
        //mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Gets the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Checks to see if a user is logged in, if not, send user to the start page for
        // registration or login
        if (currentUser == null) {
            sendToStart();
        } else {
            // If the user is online, set his online value to true
            mUserRef.child("online").setValue("true");
            // update the device token
            setDeviceToken();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            // If the user is online, set his online value to true
            mUserRef.child("online").setValue("true");
            // update the device token
            setDeviceToken();
        }
    }

    @Override
    // Whenever the app is paused (closed), set the online value to the timestamp corresponding to
    // the last date the user was online
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    // We use this method when a user is not logged in upon opening the app, or if the user logs out
    // of the system, as they should no longer be on the main page.
    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

//    @Override
//    // Inflating our options menu in the toolbar
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//
//        return true;
//    }

//    @Override
//    // After inflation of the options menu, we use a switch statement to determine which option the
//    // user selects, and depending on which one, we act accordingly
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//
//        switch (item.getItemId()) {
//            // If the Account Settings button was pressed, go to the SettingsActivity
////            case R.id.main_settings_btn:
////                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
////                startActivity(settingsIntent);
////                break;
////            // If the All Users button was pressed, go to the UsersActivity
////            case R.id.main_users_btn:
////                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
////                startActivity(usersIntent);
////                break;
//            // If the Log Out button was pressed, log the user out and go to the StartActivity
//            case R.id.main_settings:
//                changeContentFragment(getSupportFragmentManager(), SettingsFragment.getFragmentTag(),new SettingsFragment(),R.id.flFragmentsContainer,false);
//                break;
//            case R.id.main_chats:
//                changeContentFragment(getSupportFragmentManager(), ChatsFragment.getFragmentTag(),new ChatsFragment(),R.id.flFragmentsContainer,false);
//                break;
//            case R.id.main_friends:
//                changeContentFragment(getSupportFragmentManager(), FriendsFragment.getFragmentTag(),new FriendsFragment(),R.id.flFragmentsContainer,false);
//                break;
//            case R.id.main_users:
//                changeContentFragment(getSupportFragmentManager(), UsersFragment.getFragmentTag(),new UsersFragment(),R.id.flFragmentsContainer,false);
//                break;
//            case R.id.main_logout_btn:
//                if (mAuth.getCurrentUser() != null) {
//                    mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//                }
//                FirebaseAuth.getInstance().signOut();
//                sendToStart();
//                break;
//
//            default:
//                break;
//
//        }
//
//        return true;
//    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        return false;
//    }
    public void changeContentFragment(FragmentManager fm, String fragmentTag, BaseFragment frag, int containerId, boolean shouldAddToBackStack) {

        // Check fragment manager to see if fragment exists
        currentFragment = fm.popBackStackImmediate(fragmentTag, 0)
                ? (BaseFragment) fm.findFragmentByTag(fragmentTag)
                : frag;

        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(containerId, frag, fragmentTag);
        if (shouldAddToBackStack) {
            transaction.addToBackStack(fragmentTag);
        }

        transaction.commitAllowingStateLoss();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.email_burger_menu) {
            changeContentFragment(getSupportFragmentManager(), FriendsFragment.getFragmentTag(),new FriendsFragment(),R.id.flFragmentsContainer,false);
        } else if (id == R.id.all_users_burger_menu) {
            changeContentFragment(getSupportFragmentManager(), UsersFragment.getFragmentTag(),new UsersFragment(),R.id.flFragmentsContainer,false);
        } else if (id == R.id.chats_burger_menu) {
            changeContentFragment(getSupportFragmentManager(), ChatsFragment.getFragmentTag(),new ChatsFragment(),R.id.flFragmentsContainer,false);
        } else if (id == R.id.settings_profile_burger_menu) {
            changeContentFragment(getSupportFragmentManager(), SettingsFragment.getFragmentTag(),new SettingsFragment(),R.id.flFragmentsContainer,false);
        } else if (id == R.id.log_out_burger_menu) {
            if (mAuth.getCurrentUser() != null) {
                mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            }
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setDeviceToken() {

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        // Get the device token from firebase
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        // And put that token into the current users database entry, so that it is updated whenever the user opens the app
        mUserRef.child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("string", "onComplete: Works");
            }
        });

    }
}

