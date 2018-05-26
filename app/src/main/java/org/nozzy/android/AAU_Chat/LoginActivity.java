package org.nozzy.android.AAU_Chat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // UI
    private EditText mLoginEmail;
    private EditText mLoginPassword;
    private Button mLogin_btn;

    private ProgressDialog mLoginProgress;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setting up the UI
        mLoginEmail = findViewById(R.id.username_login_activity);
        mLoginPassword = findViewById(R.id.password_login_activity);
        mLogin_btn = findViewById(R.id.login_button);

        mLoginProgress = new ProgressDialog(this);

        // Setting up Firebase references
        mAuth = FirebaseAuth.getInstance();

        // Button action for logging in with the input the user has typed in
        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Gets the typed in email and password
                String email = mLoginEmail.getText().toString();
                String password = mLoginPassword.getText().toString();

                if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {
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

        // Clickable part of the string where you can sign up for a new account
        SpannableString ss1 = new SpannableString(getResources().getString(R.string.do_not_have_account));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getResources().getColor(R.color.ColorPrimary, getTheme()));
            }
        };
        ss1.setSpan(clickableSpan1, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView1 = findViewById(R.id.sign_up);
        textView1.setText(ss1);
        textView1.setMovementMethod(LinkMovementMethod.getInstance());
        textView1.setHighlightColor(Color.TRANSPARENT);

        // Clickable part of the string where you can reset your password
        SpannableString ss2 = new SpannableString(getResources().getString(R.string.password_forgot));
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                showPasswordResetDialog(mLoginEmail.getText().toString());
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getResources().getColor(R.color.ColorPrimary, getTheme()));
            }
        };
        ss2.setSpan(clickableSpan2, 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView2 = findViewById(R.id.forgot_password);
        textView2.setText(ss2);
        textView2.setMovementMethod(LinkMovementMethod.getInstance());
        textView2.setHighlightColor(Color.TRANSPARENT);

    }

    // Method for logging the user in
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Hides the progress dialog
                    mLoginProgress.dismiss();

                    if (!mAuth.getCurrentUser().isEmailVerified()) {
                        mAuth.signOut();
                        mLoginProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "Account not verified.\nPlease check your email", Toast.LENGTH_LONG).show();
                    } else {
                        // Starts the MainActivity
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    }
                }
                else {
                    // If there is an error while logging in, hide the progress dialog and show the error
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, "User could not be logged in,\n" +
                            "please check and try again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Method for showing the password reset dialog
    private void showPasswordResetDialog(final String email) {
        // Building the edit dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_edit_message, null);
        dialogBuilder.setView(dialogView);

        // Edit text field for editing the message
        final EditText editText = dialogView.findViewById(R.id.edit1);
        editText.setText(email);

        // Sets the title of the dialog
        dialogBuilder.setTitle("Enter your email");
        // Sets the title and action of the "Done" button
        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String resetEmail = editText.getText().toString();

                mAuth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if (task.isSuccessful()) {
                             Toast.makeText(LoginActivity.this, "Check your email to continue", Toast.LENGTH_LONG).show();
                         } else
                             Toast.makeText(LoginActivity.this, "Email not found", Toast.LENGTH_LONG).show();
                     }
                 });
            }
        });
        // Sets the title and action of the "Cancel" button
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });

        // Shows the dialog
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

}
