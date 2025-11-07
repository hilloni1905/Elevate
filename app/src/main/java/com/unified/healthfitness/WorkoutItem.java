// ============================================
// FILE 1: WorkoutItem.java
// ============================================
package com.unified.healthfitness;

public class WorkoutItem {
    public String name;
    public String type;
    public String muscle;
    public String equipment;
    public String difficulty;
    public String instructions;
    public String gifUrl;
    public String secondaryMuscles;

    public WorkoutItem(String name, String type, String muscle, String equipment,
                       String difficulty, String instructions) {
        this.name = name;
        this.type = type;
        this.muscle = muscle;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.instructions = instructions;
        this.gifUrl = "";
        this.secondaryMuscles = "";
    }

    public WorkoutItem(String name, String type, String muscle, String equipment,
                       String difficulty, String instructions, String gifUrl, String secondaryMuscles) {
        this.name = name;
        this.type = type;
        this.muscle = muscle;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.instructions = instructions;
        this.gifUrl = gifUrl;
        this.secondaryMuscles = secondaryMuscles;
    }
}
