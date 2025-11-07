// ========================================
// FIXED RegisterActivity.java
// Firebase: Email + Password (Auth only)
// Room: All user data stored locally
// Properly syncs user data to SharedPreferences
// ========================================

package com.unified.healthfitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserAppdatabase roomDb;
    private Executor executor;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Room Database
        roomDb = UserAppdatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        // Initialize SharedPreferences
        prefs = getSharedPreferences("UnifiedHealthPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 3) {
            etFullName.setError("Name must be at least 3 characters");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        showProgress(true);

        // Step 1: Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Step 2: Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        // Step 3: Save minimal data to Firestore (only username)
                                        saveToFirestore(firebaseUser.getUid(), email, username, fullName);
                                    });
                        }
                    } else {
                        showProgress(false);
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Save ONLY username to Firestore (email is already in Firebase Auth)
     */
    private void saveToFirestore(String userId, String email, String username, String fullName) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email); // For easy retrieval
        userData.put("fullName", fullName);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Step 4: Save all data to Room Database
                    saveToRoomDatabase(userId, fullName, email, username);
                })
                .addOnFailureListener(e -> {
                    // Even if Firestore fails, continue with Room
                    Toast.makeText(this, "Firestore save failed, continuing with local storage",
                            Toast.LENGTH_SHORT).show();
                    saveToRoomDatabase(userId, fullName, email, username);
                });
    }

    /**
     * Save complete user data to Room Database and sync to SharedPreferences
     */
    private void saveToRoomDatabase(String userId, String fullName, String email, String username) {
        executor.execute(() -> {
            try {
                UserEntity user = new UserEntity();
                user.setUserId(userId);
                user.setFullName(fullName);
                user.setEmail(email);
                user.setUsername(username);
                user.setCreatedAt(System.currentTimeMillis());
                user.setLastUpdated(System.currentTimeMillis());

                // Set default values
                user.setDailyCalories(2000);
                user.setProteinGoal(150);
                user.setCarbsGoal(250);
                user.setFatGoal(70);
                user.setWaterGoal(2.5f);
                user.setNotificationsEnabled(true);

                long rowId = roomDb.userDao().insert(user);

                // Save ALL data to SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();

                // Basic user info
                editor.putBoolean("isLoggedIn", true);
                editor.putString("userId", userId);
                editor.putString("userName", fullName);
                editor.putString("userEmail", email);
                editor.putString("username", username);

                // Nutrition goals
                editor.putInt("calorieGoal", 2000);
                editor.putInt("proteinGoal", 150);
                editor.putInt("carbsGoal", 250);
                editor.putInt("fatGoal", 70);

                // Default values for other fields
                editor.putInt("userAge", 25);
                editor.putFloat("userHeight", 170.0f);
                editor.putFloat("userWeight", 70.0f);
                editor.putString("userGender", "Not specified");
                editor.putString("userGoal", "Maintain fitness");
                editor.putString("userActivityLevel", "Moderate");
                editor.putString("userBodyType", "Average");

                editor.apply();

                // Sync to other app preferences
                syncToOtherApps(fullName, 2000, 150, 250, 70);

                runOnUiThread(() -> {
                    showProgress(false);
                    if (rowId > 0) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to Personal Info Activity
                        Intent intent = new Intent(RegisterActivity.this, PersonalInfoActivity.class);
                        intent.putExtra("USER_ID", userId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Failed to save user data locally",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(RegisterActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Sync user data to other apps' SharedPreferences
     */
    private void syncToOtherApps(String name, int calories, int protein, int carbs, int fat) {
        // Sync to FitLife AI preferences
        SharedPreferences fitLifePrefs = getSharedPreferences("FitLifePrefs", MODE_PRIVATE);
        fitLifePrefs.edit()
                .putString("userName", name)
                .putInt("caloriesGoal", calories)
                .apply();

        // Sync to Meal Tracker preferences
        SharedPreferences mealPrefs = getSharedPreferences("MealTrackerPrefs", MODE_PRIVATE);
        mealPrefs.edit()
                .putString("userName", name)
                .putFloat("caloriesGoal", (float) calories)
                .putFloat("proteinGoal", (float) protein)
                .putFloat("carbsGoal", (float) carbs)
                .putFloat("fatGoal", (float) fat)
                .apply();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        etFullName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }
}