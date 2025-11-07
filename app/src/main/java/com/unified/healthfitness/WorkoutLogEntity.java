package com.unified.healthfitness;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_logs")
public class WorkoutLogEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String workoutName;
    public String bodyPart;
    public String targetMuscle;
    public String equipment;
    public String difficulty;
    public int sets;
    public int reps;
    public String date;          // Format: yyyy-MM-dd
    public String dateTime;      // Format: MMM dd, yyyy HH:mm
    public long timestamp;       // Unix timestamp for sorting
    public String notes;

    public WorkoutLogEntity() {}

    public WorkoutLogEntity(String workoutName, String bodyPart, String targetMuscle,
                            String equipment, String difficulty, int sets, int reps,
                            String date, String dateTime, long timestamp, String notes) {
        this.workoutName = workoutName;
        this.bodyPart = bodyPart;
        this.targetMuscle = targetMuscle;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.sets = sets;
        this.reps = reps;
        this.date = date;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
        this.notes = notes;
    }
}

