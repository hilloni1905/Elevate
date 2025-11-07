package com.unified.healthfitness;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * UserDataManager - Central utility for managing user data across the app
 * Ensures consistent data access and synchronization
 */
public class UserDataManager {

    private static final String PREFS_NAME = "UnifiedHealthPrefs";
    private static UserDataManager instance;
    private SharedPreferences prefs;
    private Context context;

    private UserDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized UserDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserDataManager(context);
        }
        return instance;
    }

    // ========================================
    // Getters for User Data
    // ========================================

    public String getUserId() {
        return prefs.getString("userId", null);
    }

    public String getUserName() {
        return prefs.getString("userName", "User");
    }

    public String getUserEmail() {
        return prefs.getString("userEmail", "user@example.com");
    }

    public String getUsername() {
        return prefs.getString("username", "user");
    }

    public int getUserAge() {
        return prefs.getInt("userAge", 25);
    }

    public float getUserHeight() {
        return prefs.getFloat("userHeight", 170.0f);
    }

    public float getUserWeight() {
        return prefs.getFloat("userWeight", 70.0f);
    }

    public String getUserGender() {
        return prefs.getString("userGender", "Not specified");
    }

    public String getUserGoal() {
        return prefs.getString("userGoal", "Maintain fitness");
    }

    public String getActivityLevel() {
        return prefs.getString("userActivityLevel", "Moderate");
    }

    public String getBodyType() {
        return prefs.getString("userBodyType", "Average");
    }

    public int getCalorieGoal() {
        return prefs.getInt("calorieGoal", 2000);
    }

    public int getProteinGoal() {
        return prefs.getInt("proteinGoal", 150);
    }

    public int getCarbsGoal() {
        return prefs.getInt("carbsGoal", 250);
    }

    public int getFatGoal() {
        return prefs.getInt("fatGoal", 70);
    }

    public float getWaterGoal() {
        return prefs.getFloat("waterGoal", 2.5f);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean("notificationsEnabled", true);
    }

    // ========================================
    // Today's Activity Stats
    // ========================================

    public int getTodaySteps() {
        return prefs.getInt("todaySteps", 0);
    }

    public void setTodaySteps(int steps) {
        prefs.edit().putInt("todaySteps", steps).apply();
    }

    public int getTodayCalories() {
        return prefs.getInt("todayCalories", 0);
    }

    public void setTodayCalories(int calories) {
        prefs.edit().putInt("todayCalories", calories).apply();
    }

    public float getTodayWater() {
        return prefs.getFloat("todayWater", 0.0f);
    }

    public void setTodayWater(float water) {
        prefs.edit().putFloat("todayWater", water).apply();
    }

    public int getTodayWorkouts() {
        return prefs.getInt("todayWorkouts", 0);
    }

    public void setTodayWorkouts(int workouts) {
        prefs.edit().putInt("todayWorkouts", workouts).apply();
    }

    // ========================================
    // Update User Profile
    // ========================================

    public void updateUserProfile(String name, String email, int age, float height,
                                  float weight, String gender, String goal,
                                  String activityLevel, String bodyType) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userName", name);
        editor.putString("userEmail", email);
        editor.putInt("userAge", age);
        editor.putFloat("userHeight", height);
        editor.putFloat("userWeight", weight);
        editor.putString("userGender", gender);
        editor.putString("userGoal", goal);
        editor.putString("userActivityLevel", activityLevel);
        editor.putString("userBodyType", bodyType);
        editor.putLong("userLastUpdated", System.currentTimeMillis());
        editor.apply();

        // Sync to legacy preferences
        syncToLegacyPreferences();
    }

    public void updateNutritionGoals(int calories, int protein, int carbs, int fat) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("calorieGoal", calories);
        editor.putInt("proteinGoal", protein);
        editor.putInt("carbsGoal", carbs);
        editor.putInt("fatGoal", fat);
        editor.apply();

        // Sync to legacy preferences
        syncToLegacyPreferences();
    }

    // ========================================
    // Login/Logout Management
    // ========================================

    public void setLoggedIn(String userId, String userName, String userEmail, String username) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", userId);
        editor.putString("userName", userName);
        editor.putString("userEmail", userEmail);
        editor.putString("username", username);
        editor.apply();
    }

    public void logout() {
        prefs.edit().clear().apply();

        // Clear other preference files
        context.getSharedPreferences("FitLifePrefs", Context.MODE_PRIVATE)
                .edit().clear().apply();
        context.getSharedPreferences("MealTrackerPrefs", Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    // ========================================
    // Sync to Legacy Preferences
    // ========================================

    private void syncToLegacyPreferences() {
        String userName = getUserName();
        int calorieGoal = getCalorieGoal();
        int proteinGoal = getProteinGoal();
        int carbsGoal = getCarbsGoal();
        int fatGoal = getFatGoal();

        // Sync to FitLife AI preferences
        SharedPreferences fitLifePrefs = context.getSharedPreferences("FitLifePrefs", Context.MODE_PRIVATE);
        fitLifePrefs.edit()
                .putString("userName", userName)
                .putInt("caloriesGoal", calorieGoal)
                .apply();

        // Sync to Meal Tracker preferences
        SharedPreferences mealPrefs = context.getSharedPreferences("MealTrackerPrefs", Context.MODE_PRIVATE);
        mealPrefs.edit()
                .putString("userName", userName)
                .putFloat("caloriesGoal", (float) calorieGoal)
                .putFloat("proteinGoal", (float) proteinGoal)
                .putFloat("carbsGoal", (float) carbsGoal)
                .putFloat("fatGoal", (float) fatGoal)
                .apply();
    }

    // ========================================
    // Reset Today's Stats (call at midnight)
    // ========================================

    public void resetDailyStats() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("todaySteps", 0);
        editor.putInt("todayCalories", 0);
        editor.putFloat("todayWater", 0.0f);
        editor.putInt("todayWorkouts", 0);
        editor.apply();
    }

    // ========================================
    // Get All User Data as Object
    // ========================================

    public UserData getAllUserData() {
        UserData userData = new UserData();
        userData.userId = getUserId();
        userData.userName = getUserName();
        userData.userEmail = getUserEmail();
        userData.username = getUsername();
        userData.age = getUserAge();
        userData.height = getUserHeight();
        userData.weight = getUserWeight();
        userData.gender = getUserGender();
        userData.goal = getUserGoal();
        userData.activityLevel = getActivityLevel();
        userData.bodyType = getBodyType();
        userData.calorieGoal = getCalorieGoal();
        userData.proteinGoal = getProteinGoal();
        userData.carbsGoal = getCarbsGoal();
        userData.fatGoal = getFatGoal();
        userData.waterGoal = getWaterGoal();
        userData.isLoggedIn = isLoggedIn();
        userData.todaySteps = getTodaySteps();
        userData.todayCalories = getTodayCalories();
        userData.todayWater = getTodayWater();
        userData.todayWorkouts = getTodayWorkouts();
        return userData;
    }

    // ========================================
    // User Data Class
    // ========================================

    public static class UserData {
        public String userId;
        public String userName;
        public String userEmail;
        public String username;
        public int age;
        public float height;
        public float weight;
        public String gender;
        public String goal;
        public String activityLevel;
        public String bodyType;
        public int calorieGoal;
        public int proteinGoal;
        public int carbsGoal;
        public int fatGoal;
        public float waterGoal;
        public boolean isLoggedIn;
        public int todaySteps;
        public int todayCalories;
        public float todayWater;
        public int todayWorkouts;
    }
}