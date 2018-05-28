package org.nozzy.android.AAU_Chat.Email;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.nozzy.android.AAU_Chat.R;

import java.io.File;

public class EmailMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_main);

    }

    public void compose(View view) {
        Intent iCompose = new Intent(this, EmailComposeActivity.class);

        startActivity(iCompose);
    }

    public void received(View view) {
        Intent iReceived = new Intent(this, EmailReceivedActivity.class);

        startActivity(iReceived);
    }

    public void sync(View view)
    {

        // Get Folders with mails
        getFolders();

        // Delete old database
        File dbFile = getDatabasePath("maildb");
        boolean deleted = dbFile.delete();

        SQLiteDatabase database = openOrCreateDatabase("maildb",
                MODE_PRIVATE, null);

        database.execSQL("create table if not exists mails(id text, folder text, isread text, sender text, subject text, message text, timestamp text)");


        database.execSQL("create table if not exists login(user text, password text)");

        ContentValues loginValues = new ContentValues();
        loginValues.put("user", Config.EMAIL);
        loginValues.put("password", Config.PASSWORD);
        database.insert("login", null, loginValues);


        // UPDATE CONFIG USER NAME AND PASSWORD HERE

        for(int i = 0; i < Config.listMails.size(); i++)
        {
            ContentValues mailValues = new ContentValues();

            mailValues.put("id", String.valueOf(Config.listMails.get(i).getID()));
            mailValues.put("folder", String.valueOf(Config.listMails.get(i).getFolder()));
            mailValues.put("isread", String.valueOf(Config.listMails.get(i).getIsRead()));
            mailValues.put("sender", String.valueOf(Config.listMails.get(i).getFrom()));
            mailValues.put("subject", String.valueOf(Config.listMails.get(i).getSubject()));
            mailValues.put("message", String.valueOf(Config.listMails.get(i).getMessage()));
            mailValues.put("timestamp", String.valueOf(Config.listMails.get(i).getTimeStamp()));

            database.insert("mails", null, mailValues);

        }

        database.close();

    }

    public void getFolders()
    {
        try {
            GetMails getMails = new GetMails(this);
            Object result = getMails.execute().get();
        }
        catch(Exception e) {
            e.printStackTrace();
        }



    }
}
