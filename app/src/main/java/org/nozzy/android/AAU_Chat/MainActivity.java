package org.nozzy.android.AAU_Chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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

    public Toolbar getToolbar(){
        return toolbar;
    }

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


        // TODO: Commenting
        final NavigationView navigationView = findViewById(R.id.nav_view);

        final View header = navigationView.getHeaderView(0);

        // Assigning our nav bar items to their views in the side nav bar
        mHeaderBackground = header.findViewById(R.id.nav_bar_background);
        mProfileThumb = header.findViewById(R.id.nav_bar_profile_image);
        mProfileName = header.findViewById(R.id.nav_profile_name);
        mProfileInfo = header.findViewById(R.id.nav_profile_info);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeContentFragment(getSupportFragmentManager(), SettingsFragment.getFragmentTag(),new SettingsFragment(),R.id.flFragmentsContainer,false);
                drawer.closeDrawer(Gravity.LEFT);
            }
        });


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

                    setSideNavBar(name, image, info, thumbnail);

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
    }

    private void setSideNavBar(String name, final String image, String info, final String thumbnail) {


        // Set the background to a blurred profile image
        Picasso.with(getApplicationContext()).load(image).transform(new BlurTransformation(getApplicationContext(), 10, 10))
                .networkPolicy(NetworkPolicy.OFFLINE).into(mHeaderBackground, new Callback() {
            @Override
            public void onSuccess() {

                // Create a drawable from the image that picasso has assigned
                Drawable fromPicasso = mHeaderBackground.getDrawable();

                // Convert the drawable to a bitmap
                Bitmap headerBitmap = drawableToBitmap(fromPicasso);

                // Check to see if the bitmap is considered light or dark and set the text color of the text views accordingly
                if(!isDark(headerBitmap)) {
                    mProfileName.setTextColor(Color.BLACK);
                    mProfileInfo.setTextColor(Color.BLACK);
                } else {
                    mProfileName.setTextColor(Color.WHITE);
                    mProfileInfo.setTextColor(Color.WHITE);
                }


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
        if (!thumbnail.equals("default")) {
            // We use the Picasso library to do the image loading, this way we can store the image offline for faster loading
            Picasso.with(getApplicationContext()).load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.generic).into(mProfileThumb, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    // If the image fails to load, set the image to the default one
                    Picasso.with(getApplicationContext()).load(thumbnail).placeholder(R.drawable.generic).into(mProfileThumb);
                }
            });
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isDark(Bitmap bitmap){
        boolean dark=false;

        float darkThreshold = bitmap.getWidth()*(bitmap.getHeight()/2)*0.95f;
        int darkPixels=0;

        int[] pixels = new int[bitmap.getWidth()*(bitmap.getHeight()/2)];
        bitmap.getPixels(pixels,0,bitmap.getWidth(),0,bitmap.getHeight()/2,bitmap.getWidth(),bitmap.getHeight()/2);

        for(int i = 0; i < pixels.length; i++){
            int color = pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            double luminance = (0.299*r+0.0f + 0.587*g+0.0f + 0.114*b+0.0f);
            if (luminance<150) {
                darkPixels++;
            }
        }

        if (darkPixels >= darkThreshold) {
            dark = true;
        }
        return dark;
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

    public void changeContentFragment(FragmentManager fm, String fragmentTag, BaseFragment frag, int containerId, boolean shouldAddToBackStack, ArrayList<String> list) {

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
            setEmptyDeviceToken();
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
    public void setEmptyDeviceToken() {

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        // Get the device token from firebase
        String deviceToken = "";
        // And put that token into the current users database entry, so that it is updated whenever the user opens the app
        mUserRef.child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("string", "onComplete: Works");
            }
        });

    }




    // Method to create a channel and add all relevant users to it
    private void createChannel(String name, String image, final ArrayList<String> includes) {

        // Root reference
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // Creates a semi-random key for the new channel being created
        DatabaseReference chat_push = rootRef.child("Chats").push();
        final String chatID = chat_push.getKey();

        // Creating the chat, adding the type, name, image, and included tags
        final DatabaseReference chatRef = rootRef.child("Chats").child(chatID);
        chatRef.child("chat_type").setValue("channel");
        chatRef.child("chat_name").setValue(name);
        chatRef.child("chat_image").setValue(image);
        for (String tag : includes) {
            chatRef.child("includes").child(tag).setValue(true);
        }

        // Reference to all the users
        final DatabaseReference usersRef = rootRef.child("Users");
        Query getAllUsers = usersRef.orderByKey();

        // Check each user
        getAllUsers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the ID of the user
                final String userID = dataSnapshot.getKey();
                // For each tag of that user
                Iterable<DataSnapshot> userTags = dataSnapshot.child("tags").getChildren();
                for (DataSnapshot tag : userTags) {
                    // If the tag is one of those included in this channel
                    if (includes.contains(tag.getKey())) {
                        // Add the channel into the chats of the user
                        usersRef.child(userID).child("chats").child(chatID).child("timestamp").setValue(ServerValue.TIMESTAMP);
                        // Add the user into the members of the channel
                        chatRef.child("members").child(userID).setValue("user");
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
}

