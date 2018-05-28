package org.nozzy.android.AAU_Chat.Email;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.nozzy.android.AAU_Chat.R;

public class EmailDisplayMailActivity extends AppCompatActivity {

    private static String strFrom = "";
    private static String strSubject = "";
    private static String strBody = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_mail);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();

        TextView tvFrom = findViewById(R.id.tvFrom);
        TextView tvSubject = findViewById(R.id.tvSubject);
        TextView tvBody = findViewById(R.id.tvBody);

        tvBody.setMovementMethod(new ScrollingMovementMethod());

        if(extras != null) {
            strSubject = extras.getString("subject");
        }

        SetMailContent();


        //fill the TextViews with the emails content
        tvFrom.setText(strFrom);
        tvSubject.setText(strSubject);
        tvBody.setText(strBody);

    }

    //retrieve the email content from the database
    private void SetMailContent()
    {


        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("select * from mails", null);

        cursor.moveToFirst();

        do {
            if(cursor.getString(4).equals(strSubject)){
                strFrom = cursor.getString(3);
                strBody = cursor.getString(5);
                break;
            }
        }while(cursor.moveToNext());

        database.close();
    }

    //start a new Compose Activity and auto-fill the fields
    public void reply(View view) {
        Intent iCompose = new Intent(this, EmailComposeActivity.class);


        iCompose.putExtra("from",strFrom);
        iCompose.putExtra("subject","RE: " +strSubject);
        iCompose.putExtra("body","");

        startActivity(iCompose);
    }


    //start a new Compose Activity and auto-fill the fields
    public void forward(View view) {
        Intent iCompose = new Intent(this, EmailComposeActivity.class);

        iCompose.putExtra("from","");
        iCompose.putExtra("subject","FWD: " +strSubject);
        iCompose.putExtra("body",strBody);


        startActivity(iCompose);
    }

    /*public void mark(View view) {

        // Mark mail as read

        MarkMail markMail = new MarkMail(this, strSubject);

        markMail.execute();

        Intent iMain = new Intent(this, EmailMainActivity.class);
        startActivity(iMain);

    } */

    public void delete(View view) {

        // Delete from database
        SQLiteDatabase database = openOrCreateDatabase("maildb", MODE_PRIVATE, null);

        database.execSQL("delete from " + "mails" + " where subject= '"+strSubject+"'");

        database.close();


        DeleteMail deleteMail = new DeleteMail(this, strSubject);

        deleteMail.execute();




        Intent iMain = new Intent(this, EmailMainActivity.class);
        startActivity(iMain);

    }
}
