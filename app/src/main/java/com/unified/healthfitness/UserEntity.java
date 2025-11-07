// ========================================
// FILE 1: UserEntity.java
// Package: com.unified.healthfitness.database
// Room Database Entity for User Data
// ========================================

package com.unified.healthfitness;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // Authentication
    private String userId;          // Firebase UID
    private String email;
    private String username;

    // Personal Information
    private String fullName;
    private String dateOfBirth;     // Format: "yyyy-MM-dd"
    private int age;
    private String gender;          // Male, Female, Other
    private String phoneNumber;

    // Body Measurements
    private float height;           // in cm
    private float weight;           // in kg
    private float bmi;              // Calculated
    private String bodyType;        // Ectomorph, Mesomorph, Endomorph

    // Fitness Goals
    private String fitnessGoal;     // Lose Weight, Gain Muscle, Maintain, etc.
    private String activityLevel;   // Sedentary, Light, Moderate, Very Active, Extreme
    private int targetWeight;       // in kg
    private String targetDate;      // Format: "yyyy-MM-dd"

    // Nutrition Goals
    private int dailyCalories;
    private int proteinGoal;        // in grams
    private int carbsGoal;          // in grams
    private int fatGoal;            // in grams
    private float waterGoal;        // in liters

    // App Settings
    private boolean notificationsEnabled;
    private String profilePictureUrl;
    private long createdAt;
    private long lastUpdated;

    // Constructor
    public UserEntity() {
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public float getBmi() { return bmi; }
    public void setBmi(float bmi) { this.bmi = bmi; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public int getTargetWeight() { return targetWeight; }
    public void setTargetWeight(int targetWeight) { this.targetWeight = targetWeight; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public int getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(int dailyCalories) { this.dailyCalories = dailyCalories; }

    public int getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(int proteinGoal) { this.proteinGoal = proteinGoal; }

    public int getCarbsGoal() { return carbsGoal; }
    public void setCarbsGoal(int carbsGoal) { this.carbsGoal = carbsGoal; }

    public int getFatGoal() { return fatGoal; }
    public void setFatGoal(int fatGoal) { this.fatGoal = fatGoal; }

    public float getWaterGoal() { return waterGoal; }
    public void setWaterGoal(float waterGoal) { this.waterGoal = waterGoal; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    // Utility method to calculate BMI
    public void calculateBMI() {
        if (height > 0 && weight > 0) {
            float heightInMeters = height / 100f;
            this.bmi = weight / (heightInMeters * heightInMeters);
        }
    }

    // Utility method to calculate age from date of birth
    public void calculateAge() {
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            try {
                String[] parts = dateOfBirth.split("-");
                int birthYear = Integer.parseInt(parts[0]);
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                int currentYear = calendar.get(java.util.Calendar.YEAR);
                this.age = currentYear - birthYear;
            } catch (Exception e) {
                this.age = 0;
            }
        }
    }
}
