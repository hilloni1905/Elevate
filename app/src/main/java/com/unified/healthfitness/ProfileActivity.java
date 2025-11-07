package com.unified.healthfitness;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Profile Activity - FIXED VERSION
 * Properly displays and updates username
 */
public class ProfileActivity extends AppCompatActivity {

    // Display Views
    private ImageView imgProfile;
    private TextView tvName, tvEmail;
    private Button btnEditProfile;

    // Editable Fields Container
    private LinearLayout editableFieldsLayout;

    // Input Fields
    private TextInputEditText nameInput, emailInput, ageInput, heightInput, weightInput,
            genderInput, goalInput, activityLevelInput, bodyTypeInput;
    private Button saveBtn;

    private SharedPreferences prefs;

    // Edit mode flag
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("UnifiedHealthPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Load profile data
        loadProfile();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        // Display views
        imgProfile = findViewById(R.id.imgProfile);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Editable fields container
        editableFieldsLayout = findViewById(R.id.editableFieldsLayout);

        // Input fields
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        ageInput = findViewById(R.id.ageInput);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        genderInput = findViewById(R.id.genderInput);
        goalInput = findViewById(R.id.goalInput);
        activityLevelInput = findViewById(R.id.activityLevelInput);
        bodyTypeInput = findViewById(R.id.bodyTypeInput);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void setupClickListeners() {
        // Edit Profile Button
        btnEditProfile.setOnClickListener(v -> {
            if (isEditMode) {
                // Cancel edit mode
                isEditMode = false;
                editableFieldsLayout.setVisibility(View.GONE);
                btnEditProfile.setText("Edit Profile");
                btnEditProfile.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
                loadProfile(); // Reload original data
            } else {
                // Enter edit mode
                isEditMode = true;
                editableFieldsLayout.setVisibility(View.VISIBLE);
                btnEditProfile.setText("Cancel");
                btnEditProfile.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
            }
        });

        // Save Button
        saveBtn.setOnClickListener(v -> saveProfile());
    }

    /**
     * LOAD PROFILE - FIXED
     * Properly displays username
     */
    private void loadProfile() {
        // Load from SharedPreferences
        String username = prefs.getString("username", "User");
        String fullName = prefs.getString("userName", username);
        String email = prefs.getString("userEmail", "user@example.com");
        int age = prefs.getInt("userAge", 25);
        float height = prefs.getFloat("userHeight", 170.0f);
        float weight = prefs.getFloat("userWeight", 70.0f);
        String gender = prefs.getString("userGender", "Not specified");
        String goal = prefs.getString("userGoal", "Maintain fitness");
        String activityLevel = prefs.getString("userActivityLevel", "Moderate");
        String bodyType = prefs.getString("userBodyType", "Average");

        // Display username in TextView (use username, not full name)
        tvName.setText(username + " 💙");
        tvEmail.setText(email);

        // Fill input fields with full name
        nameInput.setText(fullName);
        emailInput.setText(email);
        ageInput.setText(String.valueOf(age));
        heightInput.setText(String.valueOf(height));
        weightInput.setText(String.valueOf(weight));
        genderInput.setText(gender);
        goalInput.setText(goal);
        activityLevelInput.setText(activityLevel);
        bodyTypeInput.setText(bodyType);
    }

    /**
     * SAVE PROFILE - No changes needed
     * Username is not editable in profile
     */
    private void saveProfile() {
        try {
            // Get values from input fields
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String ageStr = ageInput.getText().toString().trim();
            String heightStr = heightInput.getText().toString().trim();
            String weightStr = weightInput.getText().toString().trim();
            String gender = genderInput.getText().toString().trim();
            String goal = goalInput.getText().toString().trim();
            String activityLevel = activityLevelInput.getText().toString().trim();
            String bodyType = bodyTypeInput.getText().toString().trim();

            // Validate required fields
            if (name.isEmpty()) {
                nameInput.setError("Name is required");
                return;
            }

            // Parse numeric values with defaults
            int age = ageStr.isEmpty() ? 25 : Integer.parseInt(ageStr);
            float height = heightStr.isEmpty() ? 170.0f : Float.parseFloat(heightStr);
            float weight = weightStr.isEmpty() ? 70.0f : Float.parseFloat(weightStr);

            // Use defaults if fields are empty
            if (email.isEmpty()) email = "user@example.com";
            if (gender.isEmpty()) gender = "Not specified";
            if (goal.isEmpty()) goal = "Maintain fitness";
            if (activityLevel.isEmpty()) activityLevel = "Moderate";
            if (bodyType.isEmpty()) bodyType = "Average";

            // Save to SharedPreferences
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
            editor.apply();

            // Also save to other app preferences for compatibility
            syncToOtherApps(name, email);

            // Show success message
            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

            // Exit edit mode
            isEditMode = false;
            editableFieldsLayout.setVisibility(View.GONE);
            btnEditProfile.setText("Edit Profile");
            btnEditProfile.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));

            // Reload to display updated data
            loadProfile();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for numeric fields", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sync profile data to other apps' SharedPreferences
     */
    private void syncToOtherApps(String name, String email) {
        // Sync to FitLife AI preferences
        SharedPreferences fitLifePrefs = getSharedPreferences("FitLifePrefs", MODE_PRIVATE);
        fitLifePrefs.edit()
                .putString("userName", name)
                .apply();

        // Sync to Meal Tracker preferences
        SharedPreferences mealPrefs = getSharedPreferences("MealTrackerPrefs", MODE_PRIVATE);
        mealPrefs.edit()
                .putString("userName", name)
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile(); // Refresh profile when returning to this activity
    }
}