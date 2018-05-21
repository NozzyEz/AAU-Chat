package org.nozzy.android.AAU_Chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPassword extends AppCompatActivity {

    //setting up the UI
    private TextView mDisplayInfo;
    private TextInputEditText mEmail;
    private Button mResendPassword;
    private Toolbar mToolbar;

        //Firebase reference
        private FirebaseAuth mAuth;
        private DatabaseReference mUserDatabase;

        private static final String TAG="Reset password";

        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);


        mToolbar = findViewById(R.id.reset_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reset password");

        mEmail=findViewById(R.id.email_reset_password);
        mResendPassword=findViewById(R.id.reset_password_button);
        mDisplayInfo=findViewById(R.id.type_email_for_forgotten_password);


        //setting up the firebase reference
            mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
            mAuth=FirebaseAuth.getInstance();

            //Button for re-setting the password with the e-mail the user was looged in
            mResendPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String emailAddress=mEmail.getText().toString();
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
}
