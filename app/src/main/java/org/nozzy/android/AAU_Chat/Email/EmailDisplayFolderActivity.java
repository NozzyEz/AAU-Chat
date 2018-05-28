package org.nozzy.android.AAU_Chat.Email;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.nozzy.android.AAU_Chat.R;

import java.util.ArrayList;
import java.util.Collections;


public class EmailDisplayFolderActivity extends AppCompatActivity implements MailsAdaptor.ItemClickListener {

    MailsAdaptor adapter;
    Bundle extras;
    String foldername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_folder);

        extras = getIntent().getExtras();
        foldername = extras.getString("folder");
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvMails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));

        adapter = new MailsAdaptor(this, getMailSubjects(), getMailSender(), getMailDate(), getRead());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }
    private ArrayList<String> getMailSubjects() {

        ArrayList<String> listMailSubjects = new ArrayList<>();
        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("select * from mails", null);

        cursor.moveToFirst();

        do {
            if(cursor.getString(1).equals(foldername)){
                listMailSubjects.add(cursor.getString((4)));
            }


        }while(cursor.moveToNext());

        database.close();

        Collections.reverse(listMailSubjects);
        return listMailSubjects;
    }

    private ArrayList<String> getMailDate() {

        ArrayList<String> listMailDate = new ArrayList<>();
        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("select * from mails", null);

        cursor.moveToFirst();

        do {
            if(cursor.getString(1).equals(foldername)){
                listMailDate.add(cursor.getString((6)));
            }


        }while(cursor.moveToNext());

        database.close();

        Collections.reverse(listMailDate);
        return listMailDate;
    }

    private ArrayList<String> getMailSender() {

        ArrayList<String> listMailSender = new ArrayList<>();
        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("select * from mails", null);

        cursor.moveToFirst();

        do {
            if(cursor.getString(1).equals(foldername)){
                listMailSender.add(cursor.getString((3)));
            }


        }while(cursor.moveToNext());

        database.close();

        Collections.reverse(listMailSender);
        return listMailSender;
    }

    private ArrayList<String> getRead() {

        ArrayList<String> listRead = new ArrayList<>();
        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("select * from mails", null);

        cursor.moveToFirst();

        do {
            if(cursor.getString(1).equals(foldername)){
                listRead.add(cursor.getString((2)));
            }


        }while(cursor.moveToNext());

        database.close();

        Collections.reverse(listRead );
        return listRead ;
    }



    @Override
    public void onItemClick(View view, int position) {
        Intent iDisplayMail = new Intent(this, EmailDisplayMailActivity.class);

        iDisplayMail.putExtra("subject", adapter.getItem(position));
        startActivity(iDisplayMail);

        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
