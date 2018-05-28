package org.nozzy.android.AAU_Chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// This activity is run when people open the app and aren't logged in, or when they log out, all
// this activity does is show our logo as well as hold options to either register with a new account
// or log in to an old one.

public class StartActivity extends AppCompatActivity {

    // UI
    private Button mRegBtn;
    private Button mLoggingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Two buttons in our UI for creating an account or logging in
        mRegBtn = findViewById(R.id.start_reg_button);
        mLoggingBtn = findViewById(R.id.start_login_btn);

        // The register button starts the RegisterActivity
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

        // The log in button starts the EmailLoginActivity
        mLoggingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login_intent);
            }
        });
    }
}
