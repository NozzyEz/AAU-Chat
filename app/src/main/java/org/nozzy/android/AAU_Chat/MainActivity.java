package org.nozzy.android.AAU_Chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

// This is our main activity, where most of the app foundation is laid down, we have our toolbar
// with an options menu, a tabview where we can switch between different tabs.
public class MainActivity extends AppCompatActivity {

    // Initializing the Firebase Authentication, the default toolbar, the viewpager 'toolbar' as
    // well as the tab layout for Requests, Chats and friends.
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;

    private TabLayout mTabLayout;

    private BaseFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeContentFragment(getSupportFragmentManager(), ChatsFragment.getFragmentTag(),new ChatsFragment(),R.id.flFragmentsContainer,false);


        // Get the current instance of our authentication system
        mAuth = FirebaseAuth.getInstance();

        // Toolbar setup
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("AAU Chat");

        if (mAuth.getCurrentUser() != null) {
            // Point our database reference to the current user's ID, so that we can manipulate fields within
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

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

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Checks to see if a user is logged in, if not, send user to the start page for
        // registration or login
        if (currentUser == null) {
            sendToStart();
        } else {

            mUserRef.child("online").setValue("true");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuth.getCurrentUser() != null) {
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

    @Override
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

    @Override
    // Inflating our options menu in the toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    // After inflation of the options menu, we use a switch statement to determine which option the
    // user selects, and depending on which one, we act accordingly
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            // If the Account Settings button was pressed, go to the SettingsActivity
//            case R.id.main_settings_btn:
//                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
//                startActivity(settingsIntent);
//                break;
//            // If the All Users button was pressed, go to the UsersActivity
//            case R.id.main_users_btn:
//                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
//                startActivity(usersIntent);
//                break;
            // If the Log Out button was pressed, log the user out and go to the StartActivity
            case R.id.main_settings:
                changeContentFragment(getSupportFragmentManager(), SettingsFragment.getFragmentTag(),new SettingsFragment(),R.id.flFragmentsContainer,false);
                break;
            case R.id.main_chats:
                changeContentFragment(getSupportFragmentManager(), ChatsFragment.getFragmentTag(),new ChatsFragment(),R.id.flFragmentsContainer,false);
                break;
            case R.id.main_friends:
                changeContentFragment(getSupportFragmentManager(), FriendsFragment.getFragmentTag(),new FriendsFragment(),R.id.flFragmentsContainer,false);
                break;
            case R.id.main_users:
                changeContentFragment(getSupportFragmentManager(), UsersFragment.getFragmentTag(),new UsersFragment(),R.id.flFragmentsContainer,false);
                break;
            case R.id.main_logout_btn:
                if (mAuth.getCurrentUser() != null) {
                    mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                }
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;

            default:
                break;

        }

        return true;
    }



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
}

