package org.nozzy.android.AAU_Chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// This activity is accessed from the start activity when the user taps the register new account
// button. In this activity we facilitate this functionality by letting the user sign up with their
// email, name and password.
public class RegisterActivity extends AppCompatActivity {

    // UI
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;

    private ProgressDialog mRegProgress;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // First we need a Firebase authentication instance so that we can save the users input into
        // the database
        mAuth = FirebaseAuth.getInstance();

        // We setup the toolbar by first identifying, then setting it, then giving it a title, and
        // finally allow the user to return to the start activity with the arrow icon
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // We create a progress dialog which is a popup that displays as the user waits for the task
        // to complete when they register and the data needs to be sent to the database
        mRegProgress = new ProgressDialog(this);

        // Setting up the UI
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mCreateBtn = findViewById(R.id.reg_create_btn);

        // Here we make a listener for the create account button, so that when it is tapped we can
        // start to proceed with the data the user has put into our form
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                // Here we check to make sure that all the text fields has information put in so that the app does not crash
                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    // Then we start our progress dialog with some information for the user so that
                    // they know that we are actively working in the background
                    mRegProgress.setTitle("Creating Account");
                    mRegProgress.setMessage("Please wait while an account is being created");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    // Once we have a progress dialog we continue our effort of registering the user
                    // in another method.
                    registerUser(display_name,email,password);

                }

                else {
                    // If there are any empty fields, a toast is shown
                    Toast.makeText(RegisterActivity.this, "Please fill out all fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void registerUser(final String display_name, String email, String password) {

        // First we create the user entry in our database with a listener
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // If we created our user entry successfully we continue
                if(task.isSuccessful()) {

                    // First we need the unique user ID for the new account
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();

                    // Then we point our database reference to that very ID
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    // Then we create a HashMap and and fill in the users information for that
                    // account, everything but the display name is default values
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("device_token", deviceToken);
                    userMap.put("name", display_name);
                    userMap.put("status", "Default status");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    // Once we create our HashMap we can set the values inside the database under the correct User ID
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // If we succeed we remove the progress dialog that we've made earlier
                            // and then we the user to the MainActivity through an intent, with a
                            // finish method call so the user cannot use the back button to return
                            // to the register activity
                            if(task.isSuccessful()) {
                                mRegProgress.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });
                } else {
                    // If we are NOT successful we give an error message through a toast
                    // We set the error message based on what exception was thrown
                    String error;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        error = "Weak password";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error = "Invalid email";
                    } catch (FirebaseAuthUserCollisionException e) {
                        error = "Account already exists";
                    } catch (Exception e) {
                        error = "Account could not be created";
                        e.printStackTrace();
                    }
                    // We hide the progress dialog and show the error toast
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    // Method to add all relevant channels for the newly registered user
    // TODO work in progress
    private void addToChannels(final String currentUserID, final ArrayList<String> programmes) {

        // Root reference
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // References to all chats and users
        final DatabaseReference chatsRef = rootRef.child("Chats");
        final DatabaseReference usersRef = rootRef.child("Users");

        // A query to get all channels
        Query getChannels = chatsRef.orderByChild("chatType").equalTo("channel");
        getChannels.addChildEventListener(new ChildEventListener() {
            @Override
            // For each channel
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the ID of the channel
                String chatID = dataSnapshot.getKey();
                // For each programme included in the channel
                Iterable<DataSnapshot> includedProgrammes = dataSnapshot.child("includes").getChildren();
                for (DataSnapshot programme : includedProgrammes) {
                    // Check if the user's programmes contain that channel's programme
                    if (programmes.contains(programme.getKey())) {
                        // Add the channel into the chats of the user
                        usersRef.child(currentUserID).child("chats").child(chatID).child("timestamp").setValue(ServerValue.TIMESTAMP);
                        // Add the user into the members of the channel
                        chatsRef.child(chatID).child("members").child(currentUserID).setValue("user");
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



    // Method to create a channel and add all relevant users to it
    // TODO work in progress
    private void createChannel(String name, String image, final ArrayList<String> includes) {

        // Root reference
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // Creates a semi-random key for the new channel being created
        DatabaseReference chat_push = rootRef.child("Chats").push();
        final String chatID = chat_push.getKey();

        // Creating the chat, adding the type, name, image, and included programmes
        final DatabaseReference chatRef = rootRef.child("Chats").child(chatID);
        chatRef.child("chat_type").setValue("channel");
        chatRef.child("chat_name").setValue(name);
        chatRef.child("chat_image").setValue(image);
        for (String programme : includes) {
            chatRef.child("includes").child(programme).setValue(true);
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
                // For each programme of that user
                Iterable<DataSnapshot> userProgrammes = dataSnapshot.child("programmes").getChildren();
                for (DataSnapshot programme : userProgrammes) {
                    // If the programme name is one of those included in this channel
                    if (includes.contains(programme.getKey())) {
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
