package com.unified.healthfitness;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import java.util.List;

@Entity(tableName = "daily_data")
public class DailyData {
    @PrimaryKey
    @NonNull
    private String date; // Format: "YYYY-MM-DD"

    private int steps;
    private double distance;
    private int calories;

    // This constructor is used by Room
    public DailyData() {
        this.date = "";
    }

    // This constructor is for our own use and is ignored by Room
    @Ignore
    public DailyData(@NonNull String date) {
        this.date = date;
        this.steps = 0;
        this.distance = 0;
        this.calories = 0;
    }

    // --- Getters and Setters ---
    @NonNull
    public String getDate() { return date; }
    public void setDate(@NonNull String date) { this.date = date; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
}

@Dao
interface DailyDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyData data);

    @Query("SELECT * FROM daily_data ORDER BY date DESC LIMIT 7")
    LiveData<List<DailyData>> getLastSevenDays();

    @Query("UPDATE daily_data SET steps = :steps, distance = :distance, calories = :calories WHERE date = :date")
    void updateStepsForDate(String date, int steps, double distance, int calories);
}