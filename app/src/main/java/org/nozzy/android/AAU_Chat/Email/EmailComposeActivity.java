package org.nozzy.android.AAU_Chat.Email;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import org.nozzy.android.AAU_Chat.R;

public class EmailComposeActivity extends AppCompatActivity {

    EditText etTo;
    EditText etSubject;
    EditText etBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        Bundle extras = getIntent().getExtras();

        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);

        if (extras != null) {
            etTo.setText(extras.getString("from"));
            etSubject.setText(extras.getString("subject"));
            etBody.setText(extras.getString("body"));
        }

    }

    public void send(View view) {
        sendEmail();
        Intent iMain = new Intent(this, EmailMainActivity.class);
        startActivity(iMain);
    }

    private void sendEmail() {
        //Getting content for email
        String strTo = etTo.getText().toString().trim();
        String strSubject = etSubject.getText().toString().trim();
        String strMessage = etBody.getText().toString().trim();


        /* TEST FOR DATABASE CONNECTION
        strMessage = "";


        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("select * from mails", null);

        cursor.moveToFirst();


        do {

            strMessage += "ID: " + cursor.getString(0) + "\n";
            strMessage += "Folder: " + cursor.getString(1) + "\n";
            strMessage += "Is read: " + cursor.getString(2) + "\n";
            strMessage += "From: " + cursor.getString(3) + "\n";
            strMessage += "Subject: " + cursor.getString(4) + "\n";
            strMessage += "Message: " + cursor.getString(5) + "\n";
            strMessage += "Time sent: " + cursor.getString(6) + "\n";

        }while(cursor.moveToNext());*/

        //Creating SendMail object
        SendMail sm = new SendMail(this, strTo, strSubject, strMessage);

        //Executing sendmail to send email
        sm.execute();



    }



}
