package com.unified.healthfitness;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Phase.class}, version = 1, exportSchema = false)
public abstract class PeriodsAppDatabase extends RoomDatabase {
    public abstract PhaseDao phaseDao();

    private static volatile PeriodsAppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static PeriodsAppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PeriodsAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PeriodsAppDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                PhaseDao dao = INSTANCE.phaseDao();
                dao.deleteAll();

                Phase menstruation = new Phase("Menstruation", "&bull; Iron-rich foods (spinach, red meat)<br/>&bull; Dark chocolate<br/>&bull; Herbal tea", 5);
                Phase follicular = new Phase("Follicular", "&bull; Fresh vegetables<br/>&bull; Lean protein<br/>&bull; Whole grains<br/>&bull; Citrus fruits", 13);
                Phase ovulation = new Phase("Ovulation", "&bull; Colorful fruits & veggies<br/>&bull; Nuts<br/>&bull; Seeds<br/>&bull; Healthy fats", 17);
                Phase luteal = new Phase("Luteal", "&bull; Complex carbs<br/>&bull; Leafy greens<br/>&bull; Calcium-rich foods<br/>&bull; Healthy fats", 28);

                dao.insertAll(menstruation, follicular, ovulation, luteal);
            });
        }
    };
}
