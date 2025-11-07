package com.unified.healthfitness;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DailyData.class}, version = 2, exportSchema = false) // Incremented version to 2
public abstract class StepsAppDatabase extends RoomDatabase {
    public abstract DailyDataDao dailyDataDao();

    private static volatile StepsAppDatabase INSTANCE;

    public static StepsAppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (StepsAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StepsAppDatabase.class, "distance-database")
                            .fallbackToDestructiveMigration() // Added to handle version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
