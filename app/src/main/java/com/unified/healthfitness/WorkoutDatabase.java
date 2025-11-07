package com.unified.healthfitness;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WorkoutLogEntity.class}, version = 1, exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {

    private static WorkoutDatabase instance;

    public abstract WorkoutLogDao workoutLogDao();

    public static synchronized WorkoutDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            WorkoutDatabase.class,
                            "workout_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
