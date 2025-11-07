package com.unified.healthfitness.models;

public class UserProfile {
    private String name;
    private String email;
    private int age;
    private double height; // in cm
    private double weight; // in kg
    private String gender; // Male/Female/Other
    private String goal; // Lose Weight, Maintain, Gain Weight, Build Muscle
    private String activityLevel;
    private String bodyType; // Ectomorph, Mesomorph, Endomorph

    // Calculated values
    private double bmi;
    private String bmiCategory;
    private int dailyCalories;
    private int proteinGrams;
    private int carbsGrams;
    private int fatGrams;
    private double waterLiters;
    private int bmr; // Basal Metabolic Rate

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public String getBmiCategory() { return bmiCategory; }
    public void setBmiCategory(String bmiCategory) { this.bmiCategory = bmiCategory; }

    public int getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(int dailyCalories) { this.dailyCalories = dailyCalories; }

    public int getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(int proteinGrams) { this.proteinGrams = proteinGrams; }

    public int getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(int carbsGrams) { this.carbsGrams = carbsGrams; }

    public int getFatGrams() { return fatGrams; }
    public void setFatGrams(int fatGrams) { this.fatGrams = fatGrams; }

    public double getWaterLiters() { return waterLiters; }
    public void setWaterLiters(double waterLiters) { this.waterLiters = waterLiters; }

    public int getBmr() { return bmr; }
    public void setBmr(int bmr) { this.bmr = bmr; }
}