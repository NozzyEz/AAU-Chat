package org.nozzy.android.AAU_Chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    // UI
    private Toolbar mToolbar;
    private EditText mLoginEmail;
    private EditText mLoginPassword;
    private Button mLogin_btn;
    private TextView mForgetPassword;
    private Button mResetPassword;
    private static final String TAG="Login Activity";

    private ProgressDialog mLoginProgress;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setting up the UI
        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mLoginEmail = findViewById(R.id.username_login_activity);
        mLoginPassword = findViewById(R.id.password_login_activity);
        mLogin_btn = findViewById(R.id.login_button);
        mForgetPassword=findViewById(R.id.forgot_password_text_view);
        mResetPassword=findViewById(R.id.reset_password_button);

        mLoginProgress = new ProgressDialog(this);

        // Setting up Firebase references
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        // Button action for logging in with the input the user has typed in
        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Gets the typed in email and password
                String email = mLoginEmail.getText().toString();
                String password = mLoginPassword.getText().toString();

                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    // If both fields are filled in, shows a progress dialog (loading screen)
                    mLoginProgress.setTitle("Logging in");
                    mLoginProgress.setMessage("Please wait while logging in");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    // Logs the user in
                    loginUser(email, password);
                }
                else {
                    // If at least one of the fields is left empty, show a message to the user
                    Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_LONG).show();
                }

            }
        });
        //reset password with e-mail
        mResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailAddress = mLoginEmail.getText().toString();

                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email not sent.");
                                }
                                else
                                    Log.d(TAG, "email was sent");
                            }
                        });



            }
        });
    }

    // Method for logging the user in
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Hides the progress dialog
                    mLoginProgress.dismiss();

                    // Adding the device token to the user upon logging in
                    String user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    // Starts the MainActivity
                    mUserDatabase.child(user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                } else {
                    // If there is an error while logging in, hide the progress dialog and show the error
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, "user could not be logged in," +
                            " please check and try again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



}
