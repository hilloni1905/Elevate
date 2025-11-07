// ========================================
// FIXED PersonalInfoActivity.java
// All data stored in Room Database ONLY
// No Firestore operations
// Calculates and displays BMI
// ========================================

package com.unified.healthfitness;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PersonalInfoActivity extends AppCompatActivity {

    private TextInputEditText etDateOfBirth, etPhoneNumber, etHeight, etWeight;
    private TextInputEditText etTargetWeight, etTargetDate;
    private RadioGroup rgGender;
    private Spinner spBodyType, spFitnessGoal, spActivityLevel;
    private Button btnSaveAndContinue, btnSkip;
    private ProgressBar progressBar;
    private TextView tvBMIResult; // New TextView to display BMI

    private String userId;
    private UserAppdatabase roomDb;
    private Executor executor;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private Calendar selectedBirthDate = Calendar.getInstance();
    private Calendar selectedTargetDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomDb = UserAppdatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        initializeViews();
        setupSpinners();
        setupClickListeners();
        setupRealtimeBMICalculation();
    }

    private void initializeViews() {
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etTargetWeight = findViewById(R.id.etTargetWeight);
        etTargetDate = findViewById(R.id.etTargetDate);
        rgGender = findViewById(R.id.rgGender);
        spBodyType = findViewById(R.id.spBodyType);
        spFitnessGoal = findViewById(R.id.spFitnessGoal);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        btnSaveAndContinue = findViewById(R.id.btnSaveAndContinue);
        btnSkip = findViewById(R.id.btnSkip);
        progressBar = findViewById(R.id.progressBar);
        tvBMIResult = findViewById(R.id.tvBMIResult); // Initialize BMI display TextView
    }

    private void setupSpinners() {
        String[] bodyTypes = {"Ectomorph (Lean)", "Mesomorph (Muscular)", "Endomorph (Curvy)"};
        ArrayAdapter<String> bodyTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bodyTypes);
        bodyTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBodyType.setAdapter(bodyTypeAdapter);

        String[] fitnessGoals = {
                "Lose Weight",
                "Gain Muscle",
                "Maintain Weight",
                "Improve Endurance",
                "Get Fit",
                "Body Recomposition"
        };
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fitnessGoals);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFitnessGoal.setAdapter(goalAdapter);

        String[] activityLevels = {
                "Sedentary (Little or no exercise)",
                "Lightly Active (1-3 days/week)",
                "Moderately Active (3-5 days/week)",
                "Very Active (6-7 days/week)",
                "Extremely Active (Athlete)"
        };
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, activityLevels);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivityLevel.setAdapter(activityAdapter);
    }

    private void setupClickListeners() {
        etDateOfBirth.setOnClickListener(v -> showDateOfBirthPicker());
        etTargetDate.setOnClickListener(v -> showTargetDatePicker());
        btnSaveAndContinue.setOnClickListener(v -> savePersonalInfo());
        btnSkip.setOnClickListener(v -> skipToHome());
    }

    /**
     * Setup real-time BMI calculation as user types
     */
    private void setupRealtimeBMICalculation() {
        // Add text watchers to height and weight fields
        etHeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayBMI();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayBMI();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    /**
     * Calculate and display BMI in real-time
     */
    private void calculateAndDisplayBMI() {
        String heightText = etHeight.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (!heightText.isEmpty() && !weightText.isEmpty()) {
            try {
                float height = Float.parseFloat(heightText);
                float weight = Float.parseFloat(weightText);

                if (height > 0 && weight > 0) {
                    // Calculate BMI: weight(kg) / (height(m) * height(m))
                    float heightInMeters = height / 100.0f; // Convert cm to meters
                    float bmi = weight / (heightInMeters * heightInMeters);

                    // Determine BMI category
                    String category;
                    int color;
                    if (bmi < 18.5) {
                        category = "Underweight";
                        color = getColor(android.R.color.holo_blue_dark);
                    } else if (bmi < 25) {
                        category = "Normal";
                        color = getColor(android.R.color.holo_green_dark);
                    } else if (bmi < 30) {
                        category = "Overweight";
                        color = getColor(android.R.color.holo_orange_dark);
                    } else {
                        category = "Obese";
                        color = getColor(android.R.color.holo_red_dark);
                    }

                    // Display BMI result
                    String bmiText = String.format(Locale.getDefault(),
                            "BMI: %.1f (%s)", bmi, category);
                    tvBMIResult.setText(bmiText);
                    tvBMIResult.setTextColor(color);
                    tvBMIResult.setVisibility(View.VISIBLE);
                } else {
                    tvBMIResult.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                tvBMIResult.setVisibility(View.GONE);
            }
        } else {
            tvBMIResult.setVisibility(View.GONE);
        }
    }

    private void showDateOfBirthPicker() {
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -13);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedBirthDate.set(Calendar.YEAR, year);
                    selectedBirthDate.set(Calendar.MONTH, month);
                    selectedBirthDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etDateOfBirth.setText(displayFormat.format(selectedBirthDate.getTime()));
                },
                selectedBirthDate.get(Calendar.YEAR),
                selectedBirthDate.get(Calendar.MONTH),
                selectedBirthDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTargetDatePicker() {
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 2);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedTargetDate.set(Calendar.YEAR, year);
                    selectedTargetDate.set(Calendar.MONTH, month);
                    selectedTargetDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etTargetDate.setText(displayFormat.format(selectedTargetDate.getTime()));
                },
                selectedTargetDate.get(Calendar.YEAR),
                selectedTargetDate.get(Calendar.MONTH),
                selectedTargetDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void savePersonalInfo() {
        String dobText = etDateOfBirth.getText().toString().trim();
        String heightText = etHeight.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (dobText.isEmpty()) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (heightText.isEmpty()) {
            etHeight.setError("Height is required");
            etHeight.requestFocus();
            return;
        }

        if (weightText.isEmpty()) {
            etWeight.setError("Weight is required");
            etWeight.requestFocus();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        float height, weight;
        try {
            height = Float.parseFloat(heightText);
            weight = Float.parseFloat(weightText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (height < 100 || height > 250) {
            etHeight.setError("Height must be between 100-250 cm");
            etHeight.requestFocus();
            return;
        }

        if (weight < 30 || weight > 300) {
            etWeight.setError("Weight must be between 30-300 kg");
            etWeight.requestFocus();
            return;
        }

        showProgress(true);

        String gender = (selectedGenderId == R.id.rbMale) ? "Male" :
                (selectedGenderId == R.id.rbFemale) ? "Female" : "Other";
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String bodyType = spBodyType.getSelectedItem().toString();
        String fitnessGoal = spFitnessGoal.getSelectedItem().toString();
        String activityLevel = spActivityLevel.getSelectedItem().toString();
        String targetWeightText = etTargetWeight.getText().toString().trim();
        int targetWeight = targetWeightText.isEmpty() ? (int)weight : Integer.parseInt(targetWeightText);
        String targetDate = etTargetDate.getText().toString().isEmpty() ?
                dateFormat.format(selectedTargetDate.getTime()) :
                dateFormat.format(selectedTargetDate.getTime());
        String dateOfBirth = dateFormat.format(selectedBirthDate.getTime());

        NutritionCalculator calculator = new NutritionCalculator(weight, height, gender,
                activityLevel, fitnessGoal);

        saveToRoom(dateOfBirth, gender, phoneNumber, height, weight, bodyType,
                fitnessGoal, activityLevel, targetWeight, targetDate, calculator);
    }

    private void saveToRoom(String dob, String gender, String phone, float height, float weight,
                            String bodyType, String goal, String activity, int targetWeight,
                            String targetDate, NutritionCalculator calculator) {
        executor.execute(() -> {
            try {
                UserEntity user = roomDb.userDao().getUserByFirebaseId(userId);
                if (user == null) {
                    user = new UserEntity();
                    user.setUserId(userId);
                }

                user.setDateOfBirth(dob);
                user.setGender(gender);
                user.setPhoneNumber(phone);
                user.setHeight(height);
                user.setWeight(weight);
                user.setBodyType(bodyType);
                user.setFitnessGoal(goal);
                user.setActivityLevel(activity);
                user.setTargetWeight(targetWeight);
                user.setTargetDate(targetDate);
                user.setDailyCalories(calculator.getDailyCalories());
                user.setProteinGoal(calculator.getProteinGoal());
                user.setCarbsGoal(calculator.getCarbsGoal());
                user.setFatGoal(calculator.getFatGoal());
                user.setWaterGoal(calculator.getWaterGoal());
                user.calculateBMI(); // Calculate BMI
                user.calculateAge(); // Calculate age
                user.setLastUpdated(System.currentTimeMillis());

                roomDb.userDao().update(user);

                // Save ALL data to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UnifiedHealthPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // Update personal info
                editor.putString("userName", user.getFullName());
                editor.putString("userEmail", user.getEmail());
                editor.putString("dateOfBirth", dob);
                editor.putString("userGender", gender);
                editor.putString("phoneNumber", phone);
                editor.putFloat("userHeight", height);
                editor.putFloat("userWeight", weight);
                editor.putString("userBodyType", bodyType);
                editor.putString("userGoal", goal);
                editor.putString("userActivityLevel", activity);
                editor.putInt("userAge", user.getAge());
                editor.putFloat("userBMI", user.getBmi());

                // Update nutrition goals
                editor.putInt("calorieGoal", calculator.getDailyCalories());
                editor.putInt("proteinGoal", calculator.getProteinGoal());
                editor.putInt("carbsGoal", calculator.getCarbsGoal());
                editor.putInt("fatGoal", calculator.getFatGoal());

                editor.apply();

                // Sync to other app preferences
                syncToOtherApps(user.getFullName(), calculator);

                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();
                    goToHome();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Sync user data to other apps' SharedPreferences
     */
    private void syncToOtherApps(String name, NutritionCalculator calculator) {
        // Sync to FitLife AI preferences
        SharedPreferences fitLifePrefs = getSharedPreferences("FitLifePrefs", MODE_PRIVATE);
        fitLifePrefs.edit()
                .putString("userName", name)
                .putInt("caloriesGoal", calculator.getDailyCalories())
                .apply();

        // Sync to Meal Tracker preferences
        SharedPreferences mealPrefs = getSharedPreferences("MealTrackerPrefs", MODE_PRIVATE);
        mealPrefs.edit()
                .putString("userName", name)
                .putFloat("caloriesGoal", (float) calculator.getDailyCalories())
                .putFloat("proteinGoal", (float) calculator.getProteinGoal())
                .putFloat("carbsGoal", (float) calculator.getCarbsGoal())
                .putFloat("fatGoal", (float) calculator.getFatGoal())
                .apply();
    }

    private void skipToHome() {
        SharedPreferences prefs = getSharedPreferences("UnifiedHealthPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("isLoggedIn", true).apply();
        goToHome();
    }

    private void goToHome() {
        Intent intent = new Intent(PersonalInfoActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveAndContinue.setEnabled(!show);
        btnSkip.setEnabled(!show);
    }

    private static class NutritionCalculator {
        private int dailyCalories;
        private int proteinGoal;
        private int carbsGoal;
        private int fatGoal;
        private float waterGoal;

        public NutritionCalculator(float weight, float height, String gender,
                                   String activityLevel, String fitnessGoal) {
            float bmr = 10 * weight + 6.25f * height - 5 * 25;
            if (gender.equals("Male")) bmr += 5;
            else bmr -= 161;

            float multiplier = 1.55f;
            if (activityLevel.contains("Sedentary")) multiplier = 1.2f;
            else if (activityLevel.contains("Lightly")) multiplier = 1.375f;
            else if (activityLevel.contains("Moderately")) multiplier = 1.55f;
            else if (activityLevel.contains("Very")) multiplier = 1.725f;
            else if (activityLevel.contains("Extremely")) multiplier = 1.9f;

            float tdee = bmr * multiplier;

            if (fitnessGoal.contains("Lose")) tdee -= 500;
            else if (fitnessGoal.contains("Gain")) tdee += 300;

            this.dailyCalories = (int) tdee;
            this.proteinGoal = (int) (weight * 2);
            this.carbsGoal = (int) ((tdee * 0.40) / 4);
            this.fatGoal = (int) ((tdee * 0.30) / 9);
            this.waterGoal = weight * 0.033f;
        }

        public int getDailyCalories() { return dailyCalories; }
        public int getProteinGoal() { return proteinGoal; }
        public int getCarbsGoal() { return carbsGoal; }
        public int getFatGoal() { return fatGoal; }
        public float getWaterGoal() { return waterGoal; }
    }
}