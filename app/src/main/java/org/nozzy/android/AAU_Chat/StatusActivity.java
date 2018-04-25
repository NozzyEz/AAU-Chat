package org.nozzy.android.AAU_Chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Class to change the status message of the current user
public class StatusActivity extends AppCompatActivity {

    // UI and Database initialization
    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSavebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Finding the current user, their user ID, and then set the database reference to the correct place
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        // Setting up the toolbar
        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // String to show the user's current status message
        String statusValue = getIntent().getStringExtra("statusValue");

        mStatus = findViewById(R.id.status_input);
        mSavebtn = findViewById(R.id.status_save_btn);

        // Setting the status field to show the current message before entering a new one.
        mStatus.getEditText().setText(statusValue);

        // Saving the changes to the database so the user can see the new status on the settings activity
        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("Changes are being saved, please wait a moment");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();

                // Set the value for the status message inside of our database in the field with the name 'status'
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            mProgress.dismiss();

                            // Sending the user back to the settings activity once the task is successful
                            Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            finish();


                        } else {

                            Toast.makeText(getApplicationContext(), "There was an error when saving changes", Toast.LENGTH_LONG).show();

                        }
                    }
                });


            }
        });



    }
}
