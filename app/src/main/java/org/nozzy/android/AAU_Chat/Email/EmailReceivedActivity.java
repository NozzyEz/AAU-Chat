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


public class EmailReceivedActivity extends AppCompatActivity implements FoldersAdaptor.ItemClickListener {

    FoldersAdaptor adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received);

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvFolders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoldersAdaptor(this, getFolderNames());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);


    }

    private ArrayList<String> getFolderNames() {

        ArrayList<String> listFolderNames = new ArrayList<>();


        try {


            SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

            Cursor cursor = database.rawQuery("select * from mails", null);

            cursor.moveToFirst();

            do {
                if (!listFolderNames.contains(cursor.getString(1)))
                    listFolderNames.add(cursor.getString(1));

            } while (cursor.moveToNext());

            database.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return listFolderNames;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent iDisplayFolder = new Intent(this, EmailDisplayFolderActivity.class);

        iDisplayFolder.putExtra("folder", adapter.getItem(position));
        startActivity(iDisplayFolder);

        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
