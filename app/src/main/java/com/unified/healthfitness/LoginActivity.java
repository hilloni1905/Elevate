// ========================================
// COMPLETE LoginActivity.java - FIXED
// Properly updates username in SharedPreferences
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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etUsername, etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvRegisterLink, tvForgotPassword;
    private ProgressBar progressBar;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Room Database (Local SQLite Database)
    private UserAppdatabase roomDb;
    private Executor executor;

    // SharedPreferences (Key-Value Cache)
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize Room Database (Local Database)
        roomDb = UserAppdatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        // Initialize SharedPreferences (Session Cache)
        prefs = getSharedPreferences("UnifiedHealthPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && prefs.getBoolean("isLoggedIn", false)) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        btnGoogleSignIn.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign-In - Coming Soon", Toast.LENGTH_SHORT).show()
        );

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    /**
     * LOGIN USER - FIXED VERSION
     * Properly saves username to SharedPreferences
     */
    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
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

        showProgress(true);

        // Step 1: Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Step 2: Load from Room Database with entered username
                            loadUserDataFromRoom(firebaseUser.getUid(), username);
                        }
                    } else {
                        showProgress(false);
                        String errorMessage = "Login failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * LOAD USER DATA FROM ROOM DATABASE - FIXED
     * Properly handles username from login form
     */
    private void loadUserDataFromRoom(String userId, String enteredUsername) {
        executor.execute(() -> {
            try {
                // Query Room Database
                UserEntity user = roomDb.userDao().getUserByFirebaseId(userId);

                if (user != null) {
                    // User exists in Room Database
                    // Save ALL data to SharedPreferences (cache)
                    SharedPreferences.Editor editor = prefs.edit();

                    // Basic user info
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userId", userId);

                    // FIXED: Use entered username from login form
                    editor.putString("username", enteredUsername);

                    // Use full name from database
                    editor.putString("userName", user.getFullName());
                    editor.putString("userEmail", user.getEmail());

                    // Personal info
                    editor.putInt("userAge", user.getAge());
                    editor.putFloat("userHeight", user.getHeight());
                    editor.putFloat("userWeight", user.getWeight());
                    editor.putString("userGender", user.getGender());
                    editor.putString("userGoal", user.getFitnessGoal());
                    editor.putString("userActivityLevel", user.getActivityLevel());
                    editor.putString("userBodyType", user.getBodyType());

                    // Nutrition goals
                    editor.putInt("calorieGoal", user.getDailyCalories());
                    editor.putInt("proteinGoal", user.getProteinGoal());
                    editor.putInt("carbsGoal", user.getCarbsGoal());
                    editor.putInt("fatGoal", user.getFatGoal());

                    // Other data
                    editor.putFloat("userBMI", user.getBmi());
                    editor.putString("dateOfBirth", user.getDateOfBirth());
                    editor.putString("phoneNumber", user.getPhoneNumber());

                    editor.apply();

                    // Sync to other app preferences for compatibility
                    syncToOtherApps(user, enteredUsername);

                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this,
                                "Welcome back, " + enteredUsername + "!",
                                Toast.LENGTH_SHORT).show();
                        goToHome();
                    });
                } else {
                    // User not in Room database - first login or new user
                    // Save temporary data and redirect to profile setup
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userId", userId);
                    editor.putString("username", enteredUsername);
                    editor.putString("userEmail", etEmail.getText().toString().trim());
                    editor.putString("userName", enteredUsername); // Fallback
                    editor.apply();

                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(this, "Please complete your profile", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, PersonalInfoActivity.class);
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("USERNAME", enteredUsername);
                        startActivity(intent);
                        finish();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this,
                            "Error loading user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * SYNC TO OTHER APPS - FIXED
     * Includes username in sync
     */
    private void syncToOtherApps(UserEntity user, String username) {
        // Sync to FitLife AI preferences
        SharedPreferences fitLifePrefs = getSharedPreferences("FitLifePrefs", MODE_PRIVATE);
        fitLifePrefs.edit()
                .putString("userName", user.getFullName())
                .putString("username", username)
                .putInt("caloriesGoal", user.getDailyCalories())
                .apply();

        // Sync to Meal Tracker preferences
        SharedPreferences mealPrefs = getSharedPreferences("MealTrackerPrefs", MODE_PRIVATE);
        mealPrefs.edit()
                .putString("userName", user.getFullName())
                .putString("username", username)
                .putFloat("caloriesGoal", (float) user.getDailyCalories())
                .putFloat("proteinGoal", (float) user.getProteinGoal())
                .putFloat("carbsGoal", (float) user.getCarbsGoal())
                .putFloat("fatGoal", (float) user.getFatGoal())
                .apply();
    }

    /**
     * RESET PASSWORD
     */
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        showProgress(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to send reset email: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Navigate to Home Activity
     */
    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Show/Hide Loading Progress
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnGoogleSignIn.setEnabled(!show);
        etUsername.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }
}