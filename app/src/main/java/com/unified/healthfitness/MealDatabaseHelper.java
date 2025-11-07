package com.unified.healthfitness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MealTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_MEALS = "meals";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_MEAL_TYPE = "meal_type";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_PROTEIN = "protein";
    private static final String COLUMN_CARBS = "carbs";
    private static final String COLUMN_FAT = "fat";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Singleton instance
    private static MealDatabaseHelper instance;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static synchronized MealDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MealDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private MealDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEALS_TABLE = "CREATE TABLE " + TABLE_MEALS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT NOT NULL, "
                + COLUMN_MEAL_TYPE + " TEXT NOT NULL, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_CALORIES + " REAL NOT NULL, "
                + COLUMN_PROTEIN + " REAL NOT NULL, "
                + COLUMN_CARBS + " REAL NOT NULL, "
                + COLUMN_FAT + " REAL NOT NULL, "
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL"
                + ")";
        db.execSQL(CREATE_MEALS_TABLE);

        // Create index on date for faster queries
        db.execSQL("CREATE INDEX idx_date ON " + TABLE_MEALS + "(" + COLUMN_DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
        onCreate(db);
    }

    // Insert a new meal
    public long insertMeal(String date, String mealType, MealItem mealItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DATE, date);
        values.put(COLUMN_MEAL_TYPE, mealType);
        values.put(COLUMN_NAME, mealItem.name);
        values.put(COLUMN_CALORIES, mealItem.calories);
        values.put(COLUMN_PROTEIN, mealItem.protein);
        values.put(COLUMN_CARBS, mealItem.carbs);
        values.put(COLUMN_FAT, mealItem.fat);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        long id = db.insert(TABLE_MEALS, null, values);
        db.close();
        return id;
    }

    // Get all meals for a specific date
    public List<MealItem> getMealsForDate(String date) {
        List<MealItem> mealList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEALS +
                " WHERE " + COLUMN_DATE + " = ? " +
                " ORDER BY " + COLUMN_TIMESTAMP + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                MealItem meal = new MealItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROTEIN)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CARBS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FAT))
                );
                meal.id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                meal.mealType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TYPE));
                mealList.add(meal);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mealList;
    }

    // Get meals by date and meal type
    public List<MealItem> getMealsByDateAndType(String date, String mealType) {
        List<MealItem> mealList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEALS +
                " WHERE " + COLUMN_DATE + " = ? AND " + COLUMN_MEAL_TYPE + " = ?" +
                " ORDER BY " + COLUMN_TIMESTAMP + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date, mealType});

        if (cursor.moveToFirst()) {
            do {
                MealItem meal = new MealItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROTEIN)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CARBS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FAT))
                );
                meal.id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                meal.mealType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TYPE));
                mealList.add(meal);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mealList;
    }

    // Delete a meal by ID
    public boolean deleteMeal(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MEALS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    // Delete a meal by MealItem (finds by name, date, type)
    public boolean deleteMeal(String date, String mealType, MealItem mealItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MEALS,
                COLUMN_DATE + " = ? AND " + COLUMN_MEAL_TYPE + " = ? AND " +
                        COLUMN_NAME + " = ? AND " + COLUMN_CALORIES + " = ?",
                new String[]{date, mealType, mealItem.name, String.valueOf(mealItem.calories)});
        db.close();
        return result > 0;
    }

    // Update a meal
    public boolean updateMeal(long id, MealItem mealItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, mealItem.name);
        values.put(COLUMN_CALORIES, mealItem.calories);
        values.put(COLUMN_PROTEIN, mealItem.protein);
        values.put(COLUMN_CARBS, mealItem.carbs);
        values.put(COLUMN_FAT, mealItem.fat);

        int result = db.update(TABLE_MEALS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    // Get total calories for a date
    public double getTotalCalories(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_CALORIES + ") FROM " + TABLE_MEALS +
                " WHERE " + COLUMN_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{date});
        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }

    // Get nutrition totals for a date
    public NutritionTotals getNutritionTotals(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        NutritionTotals totals = new NutritionTotals();

        String query = "SELECT " +
                "SUM(" + COLUMN_CALORIES + ") as total_calories, " +
                "SUM(" + COLUMN_PROTEIN + ") as total_protein, " +
                "SUM(" + COLUMN_CARBS + ") as total_carbs, " +
                "SUM(" + COLUMN_FAT + ") as total_fat " +
                "FROM " + TABLE_MEALS +
                " WHERE " + COLUMN_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            totals.calories = cursor.getDouble(0);
            totals.protein = cursor.getDouble(1);
            totals.carbs = cursor.getDouble(2);
            totals.fat = cursor.getDouble(3);
        }

        cursor.close();
        db.close();
        return totals;
    }

    // Get all dates that have meals (for history/calendar view)
    public List<String> getAllDatesWithMeals() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT " + COLUMN_DATE + " FROM " + TABLE_MEALS +
                " ORDER BY " + COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dates;
    }

    // Delete all meals for a specific date
    public boolean deleteAllMealsForDate(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MEALS, COLUMN_DATE + " = ?", new String[]{date});
        db.close();
        return result > 0;
    }

    // Delete old meals (older than specified days)
    public int deleteOldMeals(int daysToKeep) {
        SQLiteDatabase db = this.getWritableDatabase();
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
        int result = db.delete(TABLE_MEALS, COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(cutoffTime)});
        db.close();
        return result;
    }

    // Inner class for nutrition totals
    public static class NutritionTotals {
        public double calories = 0;
        public double protein = 0;
        public double carbs = 0;
        public double fat = 0;
    }
}