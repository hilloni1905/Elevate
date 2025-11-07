package com.unified.healthfitness.models;

import java.util.ArrayList;
import java.util.List;
import com.unified.healthfitness.models.Workout;

public class DailyStats {
    private int steps;
    private int waterGlasses;
    private int caloriesConsumed;
    private int caloriesBurned;
    private int workoutMinutes;
    private String date;
    private List<Workout> workouts;

    public DailyStats() {
        this.workouts = new ArrayList<>();
    }

    // Getters and Setters
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public int getWaterGlasses() { return waterGlasses; }
    public void setWaterGlasses(int waterGlasses) { this.waterGlasses = waterGlasses; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }
    public int getWorkoutMinutes() { return workoutMinutes; }
    public void setWorkoutMinutes(int workoutMinutes) { this.workoutMinutes = workoutMinutes; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }


    public List<Workout> getWorkouts() { return workouts; }
    public void setWorkouts(List<Workout> workouts) { this.workouts = workouts; }

    public int getNetCalories() {
        return caloriesConsumed - caloriesBurned;
    }
}