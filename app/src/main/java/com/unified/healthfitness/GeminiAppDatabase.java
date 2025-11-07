package com.unified.healthfitness;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {ChatEntity.class}, version = 1, exportSchema = false)
public abstract class GeminiAppDatabase extends RoomDatabase {
    private static volatile GeminiAppDatabase INSTANCE;
    public abstract ChatDao chatDao();

    public static GeminiAppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GeminiAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    GeminiAppDatabase.class, "gemini_chat_db")
                            .allowMainThreadQueries() // simple for assignment; move to background thread for production
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
