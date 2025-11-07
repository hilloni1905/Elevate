package com.unified.healthfitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.unified.healthfitness.db.McqAppDatabase;
import com.unified.healthfitness.db.UserAnswers;
import com.unified.healthfitness.db.McqAppDatabase;

import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "DATABASE_DEBUG"; // Unique tag for logging

    TextView tvResultTitle, tvDietRecommendation, tvWorkoutRecommendation, tvTips;
    Button btnRestart, btnSavePlan;
    EditText etPlanName;
    private McqAppDatabase db;
    private HashMap<Integer, Integer> userAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        db = ((MyApplication) getApplication()).getDatabase();

        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvDietRecommendation = findViewById(R.id.tvDietRecommendation);
        tvWorkoutRecommendation = findViewById(R.id.tvWorkoutRecommendation);
        tvTips = findViewById(R.id.tvTips);
        btnRestart = findViewById(R.id.btnRestart);
        etPlanName = findViewById(R.id.etPlanName);
        btnSavePlan = findViewById(R.id.btnSavePlan);

        String dominantCategory = getIntent().getStringExtra("dominant_category");
        userAnswers = (HashMap<Integer, Integer>) getIntent().getSerializableExtra("user_answers");

        if (userAnswers == null || userAnswers.isEmpty()) {
            Log.e(TAG, "CRITICAL ERROR: userAnswers HashMap is null or empty when ResultActivity was created.");
            Toast.makeText(this, "Critical Error: No answers found!", Toast.LENGTH_LONG).show();
            userAnswers = new HashMap<>(); // Initialize to prevent further crashes
        } else {
            Log.d(TAG, "SUCCESS: userAnswers HashMap received successfully. Size: " + userAnswers.size());
            Log.d(TAG, "Answers content: " + userAnswers.toString());
        }

        displayResults(dominantCategory, userAnswers);

        btnRestart.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, PersonalizedMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnSavePlan.setOnClickListener(v -> saveAndLaunchDetailedPlan());
    }

    private void saveAndLaunchDetailedPlan() {
        String planName = etPlanName.getText().toString().trim();

        if (TextUtils.isEmpty(planName)) {
            Toast.makeText(this, "Please enter a name for your plan", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to save plan with name: '" + planName + "'");
        if(userAnswers == null || userAnswers.isEmpty()) {
            Log.e(TAG, "CRITICAL ERROR: Cannot save plan because userAnswers is null or empty.");
            return;
        }

        UserAnswers planToSave = new UserAnswers(planName, userAnswers);

        try {
            long newPlanId = db.userAnswersDao().insert(planToSave);
            Log.d(TAG, "Database insert operation returned new ID: " + newPlanId);

            if (newPlanId <= 0) {
                 Log.e(TAG, "CRITICAL ERROR: Insert operation failed. Returned ID is " + newPlanId);
                 Toast.makeText(this, "Error: Failed to save plan to database.", Toast.LENGTH_LONG).show();
                 return; // Stop execution if save failed
            }
            
            Toast.makeText(ResultActivity.this, "Plan '" + planName + "' saved!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ResultActivity.this, DetailedPlanActivity.class);
            intent.putExtra("plan_id", newPlanId);
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR: An exception occurred while inserting the plan into the database.", e);
            Toast.makeText(this, "Error: Could not save plan. Check logs.", Toast.LENGTH_LONG).show();
        }
    }

    private void displayResults(String category, HashMap<Integer, Integer> userAnswers) {
        tvResultTitle.setText("Your Profile: " + category + " Type");

        StringBuilder diet = new StringBuilder("Diet Recommendation:\n\n");
        StringBuilder workout = new StringBuilder("Workout Recommendation:\n\n");
        StringBuilder tips = new StringBuilder("Tips for Success:\n\n");

        try {
            // Use getOrDefault to prevent NullPointerException if a key is missing
            int activityLevel = userAnswers.getOrDefault(0, 0);
            int dietaryPreference = userAnswers.getOrDefault(1, 0);
            int fitnessGoal = userAnswers.getOrDefault(2, 0);
            int eatingHabits = userAnswers.getOrDefault(3, 0);
            int workoutPreference = userAnswers.getOrDefault(4, 0);
            int stressManagement = userAnswers.getOrDefault(5, 0);
            int mealPlanning = userAnswers.getOrDefault(6, 0);
            int energyLevels = userAnswers.getOrDefault(7, 0);
            int motivation = userAnswers.getOrDefault(8, 0);
            int challenges = userAnswers.getOrDefault(9, 0);

            // Logic for recommendations remains the same...
            if (activityLevel == 1) {
                diet.append("• Since you have a sedentary lifestyle, start with small changes.\n");
                workout.append("• Start with light activities like walking or stretching for 15-20 minutes daily.\n");
                tips.append("✓ Focus on consistency, not intensity at the beginning.\n");
            } else if (activityLevel == 2) {
                diet.append("• You are lightly active, which is a great start!\n");
                workout.append("• Incorporate 2-3 structured workouts per week.\n");
                tips.append("✓ Find activities you enjoy to stay motivated.\n");
            } else if (activityLevel == 3) {
                diet.append("• You are moderately active, so you can focus on optimizing your nutrition.\n");
                workout.append("• You can handle 3-5 challenging workouts per week.\n");
                tips.append("✓ Ensure you are getting enough rest and recovery.\n");
            } else {
                diet.append("• You are very active, so your nutritional needs are high.\n");
                workout.append("• Your workout schedule is packed, so focus on quality and recovery.\n");
                tips.append("✓ Listen to your body and avoid overtraining.\n");
            }

            if (dietaryPreference == 1) {
                diet.append("• As a vegetarian/vegan, ensure you get enough plant-based protein.\n");
            } else if (dietaryPreference == 2) {
                diet.append("• A balanced diet is versatile. Focus on whole foods.\n");
            } else if (dietaryPreference == 3) {
                diet.append("• A high-protein diet is great for muscle building and satiety.\n");
            } else {
                diet.append("• A low-carb/keto diet can be effective for weight loss, but ensure you get enough fiber.\n");
            }

            if (fitnessGoal == 1) {
                diet.append("• For weight loss, a calorie deficit is key. Focus on nutrient-dense foods.\n");
                workout.append("• Combine strength training with cardio for optimal fat loss.\n");
            } else if (fitnessGoal == 2) {
                diet.append("• To gain muscle, a calorie surplus with high protein intake is important.\n");
                workout.append("• Focus on progressive overload in your strength training routine.\n");
            } else if (fitnessGoal == 3) {
                diet.append("• To maintain your weight, focus on a balanced diet and consistent exercise.\n");
                workout.append("• Continue with a mix of activities you enjoy.\n");
            } else {
                diet.append("• To improve overall health, focus on a variety of whole foods and regular movement.\n");
                workout.append("• A mix of cardio, strength, and flexibility work is ideal.\n");
            }

            if (eatingHabits == 1) {
                tips.append("✓ Instead of eating when stressed, try other coping mechanisms like walking or journaling.\n");
            } else if (eatingHabits == 2) {
                tips.append("✓ A structured meal plan is great for consistency.\n");
            } else if (eatingHabits == 3) {
                tips.append("✓ Eating based on hunger cues is intuitive, but ensure you are eating balanced meals.\n");
            } else {
                tips.append("✓ Social eating is important. Learn to make healthy choices in social settings.\n");
            }

            if (workoutPreference == 1) {
                workout.append("• At-home workouts can be very effective. Invest in some basic equipment.\n");
            } else if (workoutPreference == 2) {
                workout.append("• A gym with a structured routine provides a great environment for focused workouts.\n");
            } else if (workoutPreference == 3) {
                workout.append("• Group fitness classes are a fun way to stay motivated and consistent.\n");
            } else {
                workout.append("• Outdoor activities are a great way to get fresh air and exercise.\n");
            }

             if (stressManagement == 1) {
                tips.append("✓ Comfort eating is a common response to stress. Find alternative coping strategies.\n");
            } else if (stressManagement == 2) {
                workout.append("• Exercise is a fantastic way to manage stress. Keep it up!\n");
            } else if (stressManagement == 3) {
                tips.append("✓ Meditation and relaxation are powerful tools for stress management.\n");
            } else {
                tips.append("✓ Socializing with friends can be a great way to de-stress.\n");
            }

            if (mealPlanning == 1) {
                tips.append("✓ Spontaneous eating can lead to unhealthy choices. Try planning a few meals a week.\n");
            } else if (mealPlanning == 2) {
                tips.append("✓ Meal prepping is an excellent way to stay on track with your diet.\n");
            } else if (mealPlanning == 3) {
                tips.append("✓ Cooking fresh daily is great. Ensure you are making healthy choices.\n");
            } else {
                tips.append("✓ Dining out is convenient, but be mindful of hidden calories and sodium.\n");
            }

            if (energyLevels == 1) {
                workout.append("• You have the most energy in the morning, so schedule your workouts then.\n");
            } else if (energyLevels == 2) {
                workout.append("• Afternoon workouts can be a great way to break up the day.\n");
            } else if (energyLevels == 3) {
                workout.append("• Evening workouts can help you de-stress after a long day.\n");
            } else {
                tips.append("✓ Your energy levels vary, so listen to your body and exercise when you feel most energetic.\n");
            }

            if (motivation == 1) {
                tips.append("✓ Health concerns are a powerful motivator. Stay in touch with your doctor.\n");
            } else if (motivation == 2) {
                tips.append("✓ Aesthetic goals are valid. Take progress pictures to stay motivated.\n");
            } else if (motivation == 3) {
                tips.append("✓ Performance goals are a great way to stay focused and driven.\n");
            } else {
                tips.append("✓ Social acceptance and confidence are great reasons to stay fit.\n");
            }

            if (challenges == 1) {
                tips.append("✓ Emotional eating is a common challenge. Identify your triggers and find alternatives.\n");
            } else if (challenges == 2) {
                tips.append("✓ Lack of time is a major hurdle. Try shorter, more intense workouts.\n");
            } else if (challenges == 3) {
                tips.append("✓ Following a routine can be tough. Start with small, manageable habits.\n");
            } else {
                tips.append("✓ Lack of social support is a real challenge. Find online communities or a workout buddy.\n");
            }

        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR: An exception occurred in displayResults. This is likely due to an invalid userAnswers map.", e);
            Toast.makeText(this, "Error generating results. Please restart the assessment.", Toast.LENGTH_LONG).show();
        }

        tvDietRecommendation.setText(diet.toString());
        tvWorkoutRecommendation.setText(workout.toString());
        tvTips.setText(tips.toString());
    }
}
