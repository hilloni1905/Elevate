package com.unified.healthfitness;

import android.app.Application;
import androidx.room.Room;
import com.unified.healthfitness.db.McqAppDatabase;
import com.unified.healthfitness.db.McqAppDatabase;

public class MyApplication extends Application {
    private McqAppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(),
                McqAppDatabase.class, "user-plans-db")
                .allowMainThreadQueries() // Added for debugging database issues
                .build();
    }

    public McqAppDatabase getDatabase() {
        return database;
    }
}
