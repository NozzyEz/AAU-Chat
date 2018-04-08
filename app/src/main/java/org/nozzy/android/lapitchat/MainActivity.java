package org.nozzy.android.lapitchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// This is our main activity, where most of the app foundation is laid down, we have our toolbar
// with an options menu, a tabview where we can switch between different tabs.
public class MainActivity extends AppCompatActivity {

    // Initializing the Firebase Authentication, the default toolbar, the viewpager 'toolbar' as
    // well as the tab layout for Requests, Chats and friends.
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the current instance of our authentication system
        mAuth = FirebaseAuth.getInstance();

        // Toolbar setup
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("LapitChat");

        // Setting up our tabs
        mViewPager = findViewById(R.id.main_tabpager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

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
            case R.id.main_logout_btn:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.main_settings_btn:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.main_users_btn:
                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(usersIntent);
                break;
            default:
                break;

        }

        return true;
    }
}

