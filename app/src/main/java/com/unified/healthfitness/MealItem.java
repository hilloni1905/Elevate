package com.unified.healthfitness;

public class MealItem {
    public long id = -1; // Database ID (-1 means not saved yet)
    public String name;
    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public String mealType; // "Breakfast", "Lunch", "Dinner", "Snacks"

    public MealItem(String name, double calories, double protein, double carbs, double fat) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    @Override
    public String toString() {
        return name + " - " + calories + " cal";
    }
}