package com.unified.healthfitness;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataMigration {
    private static final String TAG = "DataMigration";
    private static final String MIGRATION_KEY = "data_migrated_to_sqlite";

    public static void migrateToSQLite(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MealTrackerPrefs", Context.MODE_PRIVATE);

        // Check if migration already done
        if (prefs.getBoolean(MIGRATION_KEY, false)) {
            Log.d(TAG, "Data already migrated to SQLite");
            return;
        }

        Log.d(TAG, "Starting migration from SharedPreferences to SQLite");

        MealDatabaseHelper dbHelper = MealDatabaseHelper.getInstance(context);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());

        try {
            // Migrate old format (without dates)
            migrateOldFormat(prefs, dbHelper, todayDate);

            // Mark migration as complete
            prefs.edit().putBoolean(MIGRATION_KEY, true).apply();

            Log.d(TAG, "Migration completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during migration", e);
        }
    }

    private static void migrateOldFormat(SharedPreferences prefs,
                                         MealDatabaseHelper dbHelper,
                                         String date) {
        try {
            // Migrate breakfast
            String breakfastJson = prefs.getString("breakfast", "[]");
            migrateMealType(dbHelper, date, "Breakfast", breakfastJson);

            // Migrate lunch
            String lunchJson = prefs.getString("lunch", "[]");
            migrateMealType(dbHelper, date, "Lunch", lunchJson);

            // Migrate dinner
            String dinnerJson = prefs.getString("dinner", "[]");
            migrateMealType(dbHelper, date, "Dinner", dinnerJson);

            // Migrate snacks
            String snacksJson = prefs.getString("snacks", "[]");
            migrateMealType(dbHelper, date, "Snacks", snacksJson);

            Log.d(TAG, "Migrated old format data to date: " + date);
        } catch (Exception e) {
            Log.e(TAG, "Error migrating old format", e);
        }
    }

    private static void migrateMealType(MealDatabaseHelper dbHelper,
                                        String date,
                                        String mealType,
                                        String json) {
        try {
            JSONArray array = new JSONArray(json);
            int count = 0;

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                MealItem meal = new MealItem(
                        obj.getString("name"),
                        obj.getDouble("calories"),
                        obj.getDouble("protein"),
                        obj.getDouble("carbs"),
                        obj.getDouble("fat")
                );

                dbHelper.insertMeal(date, mealType, meal);
                count++;
            }

            Log.d(TAG, "Migrated " + count + " meals for " + mealType);
        } catch (Exception e) {
            Log.e(TAG, "Error migrating " + mealType, e);
        }
    }

    // Optional: Migrate date-based SharedPreferences format to SQLite
    public static void migrateDateBasedFormat(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MealTrackerPrefs", Context.MODE_PRIVATE);
        MealDatabaseHelper dbHelper = MealDatabaseHelper.getInstance(context);

        try {
            // Get all keys from SharedPreferences
            for (String key : prefs.getAll().keySet()) {
                // Check if key is date-based (format: yyyy-MM-dd_mealtype)
                if (key.matches("\\d{4}-\\d{2}-\\d{2}_(breakfast|lunch|dinner|snacks)")) {
                    String[] parts = key.split("_");
                    String date = parts[0];
                    String mealType = capitalizeFirst(parts[1]);

                    String json = prefs.getString(key, "[]");
                    migrateMealType(dbHelper, date, mealType, json);

                    Log.d(TAG, "Migrated date-based data: " + key);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error migrating date-based format", e);
        }
    }

    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}