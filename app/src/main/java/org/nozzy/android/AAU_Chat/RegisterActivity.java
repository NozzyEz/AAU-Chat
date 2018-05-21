package org.nozzy.android.AAU_Chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// This activity is accessed from the startImageSelection activity when the user taps the register new account
// button. In this activity we facilitate this functionality by letting the user sign up with their
// email, name and password.
public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    // UI
    private TextInputEditText mDisplayName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private TextInputEditText mRepeatPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;
    private Spinner mSemesterSpinner;
    private Spinner mCoursesSpinner;
    private ArrayAdapter<CharSequence> mCharSequenceArrayAdapterCourses;
    private ArrayAdapter<CharSequence> mCharSequenceArrayAdapterSemester;


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

        // We create a progress dialog which is a popup that displays as the user waits for the task
        // to complete when they register and the data needs to be sent to the database
        mRegProgress = new ProgressDialog(this);

        // Setting up the UI
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mRepeatPassword = findViewById(R.id.repeat_password);
        mCreateBtn = findViewById(R.id.reg_create_btn);

        mCoursesSpinner = findViewById(R.id.list_with_studyprogramme_register);
        mSemesterSpinner = findViewById(R.id.semester_register);

        mCoursesSpinner.setOnItemSelectedListener(this);
        mSemesterSpinner.setOnItemSelectedListener(this);

        //setting up the array with choices for the user
        mCharSequenceArrayAdapterCourses=ArrayAdapter.createFromResource(this, R.array.courses_list, R.layout.spinner_text_view);
        mCharSequenceArrayAdapterSemester=ArrayAdapter.createFromResource(this, R.array.semester_list, R.layout.spinner_text_view);

        mCharSequenceArrayAdapterCourses.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mCharSequenceArrayAdapterSemester.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        //adding to the spinner the arrayAdapter, or adding the items in the spinner
        mCoursesSpinner.setAdapter(mCharSequenceArrayAdapterCourses);
        mSemesterSpinner.setAdapter(mCharSequenceArrayAdapterSemester);

        // Here we make a listener for the create account button, so that when it is tapped we can
        // startImageSelection to proceed with the data the user has put into our form
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard();

                String display_name = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String repeat_password = mRepeatPassword.getText().toString();

                String course = mCoursesSpinner.getSelectedItem().toString();
                String semester = mSemesterSpinner.getSelectedItem().toString();
                long courseID = mCoursesSpinner.getSelectedItemId();
                long semesterID = mSemesterSpinner.getSelectedItemId();

                String type = email.endsWith("student.aau.dk") ? "Student" : "Teacher";

                // Check if name is alphabetic
                if (!display_name.matches("[a-zA-Z ]+"))
                    Toast.makeText(RegisterActivity.this, "Please only use letters for the name", Toast.LENGTH_LONG).show();
                // Check if email is an AAU email
                else if (!email.endsWith("aau.dk"))
                    Toast.makeText(RegisterActivity.this, "Please enter a valid AAU email", Toast.LENGTH_LONG).show();
                // Check if password is at least 6 characters
                else if (password.length() < 6)
                    Toast.makeText(RegisterActivity.this, "Please make your password at least 6 characters", Toast.LENGTH_LONG).show();
                // Check if passwords match
                else if (!password.equals(repeat_password))
                    Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
                // Check if the course has been selected
                else if (courseID == 0)
                    Toast.makeText(RegisterActivity.this, "Please select a course", Toast.LENGTH_LONG).show();
                // Check if the study year has been selected
                else if (semesterID == 0)
                    Toast.makeText(RegisterActivity.this, "Please select your study year", Toast.LENGTH_LONG).show();
                // Check if all fields are filled out
                else if (TextUtils.isEmpty(display_name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repeat_password)) {
                    Toast.makeText(RegisterActivity.this, "Please fill out all fields", Toast.LENGTH_LONG).show();
                }
                else {
                    // Then we startImageSelection our progress dialog with some information for the user so that
                    // they know that we are actively working in the background
                    mRegProgress.setTitle("Creating Account");
                    mRegProgress.setMessage("Please wait while an account is being created");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    // Once we have a progress dialog we continue our effort of registering the user
                    // in another method.
                    registerUser(display_name, email, password, course, semester, type);
                }
            }
        });


        mCoursesSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideKeyboard();
                }
                return false;
            }
        });

        mSemesterSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideKeyboard();
                }
                return false;
            }
        });

    }

    private void registerUser(final String display_name, final String email, final String password, final String course, final String semester, final String type) {

        // First we create the user entry in our database with a listener
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // If we created our user entry successfully we continue
                if(task.isSuccessful()) {

                    // First we need the unique user ID for the new account
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = currentUser.getUid();

                    // Then we point our database reference to that very ID
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    // A Hashmap to store all tags that the user is in, including programmes
                    HashMap<String, Boolean> tagsMap = new HashMap<>();
                    tagsMap.put(type, true);
                    tagsMap.put(course, true);
                    tagsMap.put(semester, true);

                    // Then we create a HashMap and and fill in the users information for that
                    // account, everything but the display name is default values
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("device_token", deviceToken);
                    userMap.put("name", display_name);
                    userMap.put("status", course + " " + semester);
                    userMap.put("image", "default");
                    userMap.put("online", "never");
                    userMap.put("thumb_image", "default");
                    userMap.put("tags", tagsMap);

                    // Once we create our HashMap we can set the values inside the database under the correct User ID
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // If we succeed we remove the progress dialog that we've made earlier
                            // and then we the user to the MainActivity through an intent, with a
                            // finish method call so the user cannot use the back button to return
                            // to the register activity
                            if(task.isSuccessful()) {

                                // Adding the user to the corresponding channels based on their tags
                                ArrayList<String> tags = new ArrayList<>();
                                tags.add(type);
                                tags.add(course);
                                tags.add(semester);
                                addToChannels(uid, tags);

                                // Adding the user to everyone's friend list
                                addToFriends(uid, display_name);

                                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Toast.makeText(RegisterActivity.this, "Check your email to verify it", Toast.LENGTH_LONG).show();
                                                    else
                                                        Toast.makeText(RegisterActivity.this, "Unable to send verification email", Toast.LENGTH_LONG).show();

                                                }
                                            });
                                        }

                                        mAuth.signOut();

                                        mRegProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                });

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
    private void addToChannels(final String currentUserID, final ArrayList<String> tags) {

        // Root reference
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // References to all chats and users
        final DatabaseReference chatsRef = rootRef.child("Chats");
        final DatabaseReference usersRef = rootRef.child("Users");

        // A query to get all channels
        Query getChannels = chatsRef.orderByChild("chat_type").equalTo("channel");
        getChannels.addChildEventListener(new ChildEventListener() {
            @Override
            // For each channel
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the ID of the channel
                String chatID = dataSnapshot.getKey();
                // For each tag included in the channel
                Iterable<DataSnapshot> includedTags = dataSnapshot.child("includes").getChildren();
                for (DataSnapshot tag : includedTags) {
                    // Check if the user's tags contain that channel's tag
                    if (tags.contains(tag.getKey())) {
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

    // A method to add all users to the friends list
    private void addToFriends(final String currentUserID, final String currentUserName) {



        // Root reference
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // References to all friends and users
        final DatabaseReference friendsRef = rootRef.child("Friends");
        final DatabaseReference usersRef = rootRef.child("Users");

        // Adds a listener to go through all users
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> allUsers = dataSnapshot.getChildren();
                for (DataSnapshot user : allUsers) {
                    // If the user is not yourself
                    if (!user.getKey().equals(currentUserID)) {
                        // Add that user to your friends, and add yourself as their friend
                        friendsRef.child(currentUserID).child(user.getKey()).child("name").setValue(user.child("name").getValue(String.class));
                        friendsRef.child(user.getKey()).child(currentUserID).child("name").setValue(currentUserName);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    // A method to hide the keyboard
    private void hideKeyboard() {
        // Hides the keyboard
        if (RegisterActivity.this.getWindow() != null && RegisterActivity.this.getWindow().getDecorView() != null && RegisterActivity.this.getWindow().getDecorView().getWindowToken() != null) {
            InputMethodManager mgr = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(RegisterActivity.this.getWindow().getDecorView().getWindowToken(), 0);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


