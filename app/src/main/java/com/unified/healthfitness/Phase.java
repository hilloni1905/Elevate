package com.unified.healthfitness;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "phases")
public class Phase {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String recommendedFoods;
    public int dayInCycle;

    public Phase(String name, String recommendedFoods, int dayInCycle) {
        this.name = name;
        this.recommendedFoods = recommendedFoods;
        this.dayInCycle = dayInCycle;
    }
}
