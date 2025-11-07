package com.unified.healthfitness.models;

public class Workout {
    private String name;
    private int duration;
    private int caloriesBurned;
    private String type;

    public Workout(String name, int duration, int caloriesBurned, String type) {
        this.name = name;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.type = type;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}