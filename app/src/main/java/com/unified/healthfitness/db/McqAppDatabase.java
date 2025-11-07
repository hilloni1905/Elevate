package com.unified.healthfitness.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UserAnswers.class}, version = 1)
public abstract class McqAppDatabase extends RoomDatabase {
    public abstract UserAnswersDao userAnswersDao();
}
