package com.unified.healthfitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * HOME ACTIVITY - FIXED VERSION
 * Properly updates username and navigation header
 */
public class HomeActivity extends AppCompatActivity {

    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView imgMenu, imgNotifications;
    private TextView tvGreeting, tvUserName, tvUserStats, tvDate;

    // Navigation Header Views
    private View navHeaderView;
    private TextView navHeaderName, navHeaderEmail;
    private ImageView navHeaderImage;

    // Feature Cards
    private CardView cardWorkoutPlanner, cardMealTracker, cardRecipeSearch, cardCalorieLookup;
    private CardView cardTDEECalculator, cardSleepCycle, cardSmartAlarms, cardAIAssistant;
    private CardView cardMoodAnalyzer, cardMyProfile, cardWeatherWorkout, cardWater;
    private CardView cardPersonalizedRecommendations, cardSpotify, cardSteps, cardPeriods;

    // Data Storage
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);

        prefs = getSharedPreferences("UnifiedHealthPrefs", MODE_PRIVATE);

        initializeViews();
        setupNavigationDrawer();
        updateNavigationHeader(); // FIXED: Update nav header on create
        loadUserData();
        setupFeatureCardListeners();
        setupHeaderButtons();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Get navigation header view
        navHeaderView = navigationView.getHeaderView(0);
        navHeaderName = navHeaderView.findViewById(R.id.navHeaderName);
        navHeaderEmail = navHeaderView.findViewById(R.id.navHeaderEmail);
        navHeaderImage = navHeaderView.findViewById(R.id.navHeaderImage);

        imgMenu = findViewById(R.id.imgMenu);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvDate = findViewById(R.id.tvDate);

        cardWorkoutPlanner = findViewById(R.id.cardWorkoutPlanner);
        cardMealTracker = findViewById(R.id.cardMealTracker);
        cardRecipeSearch = findViewById(R.id.cardRecipeSearch);
        cardCalorieLookup = findViewById(R.id.cardCalorieLookup);
        cardTDEECalculator = findViewById(R.id.cardTDEECalculator);
        cardSleepCycle = findViewById(R.id.cardSleepCycle);
        cardSmartAlarms = findViewById(R.id.cardSmartAlarms);
        cardAIAssistant = findViewById(R.id.cardAIAssistant);
        cardMoodAnalyzer = findViewById(R.id.cardMoodAnalyzer);
        cardMyProfile = findViewById(R.id.cardMyProfile);
        cardWeatherWorkout = findViewById(R.id.cardWeatherWorkout);
        cardWater = findViewById(R.id.cardWater);
        cardPersonalizedRecommendations = findViewById(R.id.cardPersonalizedRecommendations);
        cardSpotify = findViewById(R.id.cardSpotify);
        cardSteps = findViewById(R.id.cardSteps);
        cardPeriods = findViewById(R.id.cardPeriods);
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_about) {
                Toast.makeText(this, "Health & Fitness Hub v1.0", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * UPDATE NAVIGATION HEADER - NEW METHOD
     * Updates username and email in navigation drawer
     */
    private void updateNavigationHeader() {
        // Get username and full name from SharedPreferences
        String username = prefs.getString("username", "User");
        String fullName = prefs.getString("userName", username);
        String email = prefs.getString("userEmail", "user@example.com");

        // Update navigation header
        if (navHeaderName != null) {
            navHeaderName.setText(username); // Display username
        }

        if (navHeaderEmail != null) {
            navHeaderEmail.setText(email);
        }
    }

    /**
     * LOAD USER DATA - FIXED
     * Displays username correctly
     */
    private void loadUserData() {
        // Get username (for display) and full name
        String username = prefs.getString("username", "User");
        String fullName = prefs.getString("userName", username);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) greeting = "Good Morning";
        else if (hour < 18) greeting = "Good Afternoon";
        else if (hour < 20) greeting = "Good Evening";
        else greeting = "Good Night";

        // Display greeting with username
        tvGreeting.setText(greeting);
        tvUserName.setText(username + " 👋");

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        tvDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupHeaderButtons() {
        imgMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupFeatureCardListeners() {
        cardMealTracker.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, MealTrackerActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Meal Tracker");
            }
        });

        cardRecipeSearch.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, RecipeSearchActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Recipe Search");
            }
        });

        cardCalorieLookup.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, CaloriesActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Calorie Lookup");
            }
        });

        cardTDEECalculator.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, tdeecalculatorMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("TDEE Calculator");
            }
        });

        cardSleepCycle.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, SleepMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Sleep Sense");
            }
        });

        cardSmartAlarms.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, AlarmMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Alarm Manager");
            }
        });

        cardAIAssistant.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, ChatAssistantActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("AI Assistant");
            }
        });

        cardMoodAnalyzer.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WatsoneMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Mood Analyzer");
            }
        });

        cardMyProfile.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, ProfileActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Profile");
            }
        });

        cardWeatherWorkout.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WeatherWorkoutMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Weather Workout");
            }
        });

        cardWater.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WaterMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Water Tracker");
            }
        });

        cardWorkoutPlanner.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WorkoutSearchActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Workout Planner");
            }
        });

        cardPersonalizedRecommendations.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, PersonalizedMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Personalized Recommendations");
            }
        });

        cardSpotify.setOnClickListener(v -> {
            String accessToken = prefs.getString("spotify_access_token", null);
            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    // User is already connected to Spotify, open the playlist view
                    startActivity(new Intent(this, PlaylistActivity.class));
                } catch (Exception e) {
                    showFeatureNotFoundError("Playlist");
                }
            } else {
                try {
                    // User is not connected, start the login flow
                    startActivity(new Intent(this, SpotifyLoginActivity.class));
                } catch (Exception e) {
                    showFeatureNotFoundError("Spotify");
                }
            }
        });

        cardSteps.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, StepMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Steps");
            }
        });

        cardPeriods.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, PeriodsMainActivity.class));
            } catch (Exception e) {
                showFeatureNotFoundError("Periods");
            }
        });
    }

    private void showFeatureNotFoundError(String featureName) {
        Toast.makeText(this, featureName + " - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Logout user and return to login screen
     */
    private void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Login screen not found", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationHeader(); // FIXED: Update nav header when returning
        loadUserData(); // Refresh user data
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
