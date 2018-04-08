package org.nozzy.android.lapitchat;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Nozzy on 05/04/2018.
 */

public class LapitChat extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // To add persistence to our firebase database, so we don't need to load the queries every time we open an activity
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Picasso persistence
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}
