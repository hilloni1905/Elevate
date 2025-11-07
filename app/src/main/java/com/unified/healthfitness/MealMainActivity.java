package com.unified.healthfitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.unified.healthfitness.R;
import com.unified.healthfitness.RecipeSearchActivity;
import com.unified.healthfitness.MealTrackerActivity;
import com.unified.healthfitness.RecipeSearchActivity;
import com.unified.healthfitness.CaloriesActivity;
import com.unified.healthfitness.ProfileActivity;

public class MealMainActivity extends AppCompatActivity {
    private TextView greetingText, dateText, caloriesConsumed, caloriesGoal;
    private TextView proteinValue, carbsValue, fatValue;
    private CircularProgressIndicator caloriesProgress;
    private CardView recipeCard, trackerCard, caloriesCard, profileCard;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_enhanced);

        prefs = getSharedPreferences("MealTrackerPrefs", MODE_PRIVATE);

        initViews();
        setupGreeting();
        loadTodayStats();
        setupClickListeners();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greetingText);
        dateText = findViewById(R.id.dateText);
        caloriesConsumed = findViewById(R.id.caloriesConsumed);
        caloriesGoal = findViewById(R.id.caloriesGoal);
        caloriesProgress = findViewById(R.id.caloriesProgress);
        proteinValue = findViewById(R.id.proteinValue);
        carbsValue = findViewById(R.id.carbsValue);
        fatValue = findViewById(R.id.fatValue);

        recipeCard = findViewById(R.id.recipeCard);
        trackerCard = findViewById(R.id.trackerCard);
        caloriesCard = findViewById(R.id.caloriesCard);
        profileCard = findViewById(R.id.profileCard);
    }

    private void setupGreeting() {
        String userName = prefs.getString("userName", "User");
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour < 12) greeting = "Good Morning";
        else if (hour < 18) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        greetingText.setText(greeting + ", " + userName + "!");

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));
    }

    private void loadTodayStats() {
        float consumed = prefs.getFloat("todayCalories", 0);
        float goal = prefs.getFloat("caloriesGoal", 2000);
        float protein = prefs.getFloat("todayProtein", 0);
        float carbs = prefs.getFloat("todayCarbs", 0);
        float fat = prefs.getFloat("todayFat", 0);

        caloriesConsumed.setText(String.format("%.0f", consumed));
        caloriesGoal.setText(String.format("/ %.0f cal", goal));

        int progress = (int) ((consumed / goal) * 100);
        caloriesProgress.setProgress(Math.min(progress, 100));

        proteinValue.setText(String.format("%.1fg", protein));
        carbsValue.setText(String.format("%.1fg", carbs));
        fatValue.setText(String.format("%.1fg", fat));
    }

    private void setupClickListeners() {
        recipeCard.setOnClickListener(v -> {
            startActivity(new Intent(MealMainActivity.this, RecipeSearchActivity.class));
        });

        trackerCard.setOnClickListener(v -> {
            startActivity(new Intent(MealMainActivity.this, MealTrackerActivity.class));
        });

        caloriesCard.setOnClickListener(v -> {
            startActivity(new Intent(MealMainActivity.this, CaloriesActivity.class));
        });

        profileCard.setOnClickListener(v -> {
            startActivity(new Intent(MealMainActivity.this, ProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayStats();
    }
}