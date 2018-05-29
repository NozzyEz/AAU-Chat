package org.nozzy.android.AAU_Chat.Email;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.nozzy.android.AAU_Chat.R;

import java.io.File;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Session;
import javax.mail.Store;

public class EmailLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //if the database exists load it
        File dbFile = getDatabasePath("maildb");

        if(dbFile.exists()) {

            SQLiteDatabase database = openOrCreateDatabase("maildb",
                    MODE_PRIVATE, null);

            database.execSQL("create table if not exists login(user text, password text)");


            Cursor cursor = database.rawQuery("select * from login", null);

            if (cursor.moveToFirst()) {
                EditText etEmail = (EditText) findViewById(R.id.etEmail);
                EditText etPassword = (EditText) findViewById(R.id.etPassword);

                etEmail.setText(cursor.getString(0));
                etPassword.setText(cursor.getString(1));
            }

            database.close();
        }



    }

    public void login(View view) {

        EditText etEmail = (EditText)findViewById(R.id.etEmail);
        EditText etPassword = (EditText)findViewById(R.id.etPassword);
        boolean valid = false;


        //try to connect to the imap server;
        //if the credentials are not valid the Exception will be caught and the user will be asked to try again
        try{
            // Create all the needed properties - empty!
            Properties connectionProperties = new Properties();
            // Create the session
            Session session = Session.getInstance(connectionProperties, null);

            System.out.print("Connecting to the IMAP server...");
            // Connecting to the server
            // Set the store depending on the parameter flag value
            Store store = session.getStore("imaps");

            // Set the server depending on the parameter flag value
            String server = "mail.aau.dk";
            store.connect(server, etEmail.getText().toString(), etPassword.getText().toString());


            valid = true;
        }
        catch(AuthenticationFailedException e)
        {
            Toast.makeText(getApplicationContext(), "Invalid email address or password", Toast.LENGTH_LONG).show();
            valid = false;
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        if(valid == true) {

            //a database exists and if it does it will compare the user input credentials
            // with the stored ones. If they are different it will delete the database and create a new one

            File dbFile = getDatabasePath("maildb");
            if (dbFile.exists()) {

                SQLiteDatabase database = openOrCreateDatabase("maildb",
                        MODE_PRIVATE, null);

                database.execSQL("create table if not exists login(user text, password text)");


                Cursor cursor = database.rawQuery("select * from login", null);

                if (cursor.moveToFirst()) {

                    if (!etEmail.getText().equals(cursor.getString(0))
                            || !etPassword.getText().equals(cursor.getString(1))
                            ) {
                        dbFile.delete();

                        database = openOrCreateDatabase("maildb",
                                MODE_PRIVATE, null);

                        database.execSQL("create table if not exists login(user text, password text)");

                        database.close();

                    }
                }


            }


            Config.EMAIL = etEmail.getText().toString();
            Config.PASSWORD = etPassword.getText().toString();


            Intent iMain = new Intent(this, EmailMainActivity.class);

                startActivity(iMain);
        }

    }
}
