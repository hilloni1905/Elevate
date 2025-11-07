
// ========================================
// FILE 3: AppDatabase.java
// Package: com.unified.healthfitness.database
// Room Database Configuration
// ========================================

package com.unified.healthfitness;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.unified.healthfitness.UserDao;
import com.unified.healthfitness.UserEntity;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class UserAppdatabase extends RoomDatabase {

    private static volatile UserAppdatabase INSTANCE;

    public abstract UserDao userDao();

    public static UserAppdatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserAppdatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    UserAppdatabase.class,
                                    "health_fitness_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}