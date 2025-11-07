package com.unified.healthfitness;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.unified.healthfitness.db.McqAppDatabase;
import com.unified.healthfitness.db.UserAnswers;
import java.util.HashMap;

public class DetailedPlanActivity extends AppCompatActivity {

    TextView tvWeeklyPlan, tvMealPlan, tvSupplementInfo;
    Toolbar toolbar;
    private McqAppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_plan);

        db = ((MyApplication) getApplication()).getDatabase();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvWeeklyPlan = findViewById(R.id.tvWeeklyPlan);
        tvMealPlan = findViewById(R.id.tvMealPlan);
        tvSupplementInfo = findViewById(R.id.tvSupplementInfo);

        long planId = getIntent().getLongExtra("plan_id", -1);

        if (planId != -1) {
            loadPlanFromDatabase(planId);
        } else {
            Toast.makeText(this, "Error: Plan ID not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadPlanFromDatabase(long planId) {
        try {
            UserAnswers userAnswers = db.userAnswersDao().findById((int) planId);
            if (userAnswers != null) {
                getSupportActionBar().setTitle(userAnswers.userName);
                displayDetailedPlan(userAnswers.answers);
            } else {
                Toast.makeText(this, "Error: Could not load plan with ID " + planId, Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("DetailedPlanActivity", "Error loading plan from database", e);
            Toast.makeText(this, "ERROR: An exception occurred while loading the plan. Check logs.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void displayDetailedPlan(HashMap<Integer, Integer> userAnswers) {
        int activityLevel = userAnswers.get(0);
        int dietaryPreference = userAnswers.get(1);
        int fitnessGoal = userAnswers.get(2);
        int eatingHabits = userAnswers.get(3);
        int workoutPreference = userAnswers.get(4);
        int mealPlanningStyle = userAnswers.get(6);
        int motivation = userAnswers.get(8);

        tvWeeklyPlan.setText(generateWorkoutPlan(activityLevel, fitnessGoal, workoutPreference));
        tvMealPlan.setText(generateMealPlan(dietaryPreference, fitnessGoal, eatingHabits, mealPlanningStyle));
        tvSupplementInfo.setText(generateSupplementPlan(dietaryPreference, fitnessGoal, motivation));
    }

    private String generateWorkoutPlan(int activityLevel, int fitnessGoal, int workoutPreference) {
        StringBuilder plan = new StringBuilder();

        if (activityLevel == 1) { // Sedentary
            plan.append("This plan is a great starting point. As you get fitter, consider increasing the duration or intensity.\n\n");
        } else if (activityLevel == 2) { // Lightly Active
            plan.append("This plan is balanced for your current activity level. Feel free to increase cardio duration if you feel energetic.\n\n");
        } else { // Active
            plan.append("You're already active, which is great! This plan adds structure. Feel free to add an extra set or increase cardio intensity on days you feel strong.\n\n");
        }
        
        plan.append("7-Day Workout Schedule:\n\n");
        String reps, day1, day2, day3, day4, day5, day6;

        if (fitnessGoal == 1) { // Weight Loss
            reps = "12-15 reps";
            day1 = "Full Body Strength (e.g., Goblet Squats, Dumbbell Rows, Push-ups) 3x" + reps;
            day2 = "HIIT Cardio (e.g., Burpees, Jumping Jacks) 30s on, 30s off for 20 mins";
            day3 = "Full Body Strength (e.g., Lunges, Overhead Press, Glute Bridges) 3x" + reps;
            day4 = "Steady-State Cardio (e.g., Brisk Walking, Cycling) for 30-40 mins";
            day5 = "Full Body Circuit (all exercises, 1 round each) 3x through";
            day6 = "Active Recovery (e.g., Yoga, Stretching, or a long walk)";
        } else if (fitnessGoal == 2) { // Muscle Gain
            reps = "6-10 reps";
            String machinePrefix = workoutPreference == 2 ? "Barbell/" : "Dumbbell/"; // Gym vs Home
            day1 = "Upper Body Push (e.g., " + machinePrefix + "Bench Press, Shoulder Press) 4x" + reps;
            day2 = "Lower Body Squat Focus (e.g., " + machinePrefix + "Squats, Leg Press, Leg Extensions) 4x" + reps;
            day3 = "Upper Body Pull (e.g., Pull-ups or Lat Pulldowns, Bent-Over Rows) 4x" + reps;
            day4 = "Lower Body Hinge Focus (e.g., Deadlifts or Good Mornings, Hamstring Curls) 4x" + reps;
            day5 = "Accessory & Core (e.g., Bicep Curls, Tricep Extensions, Planks) 3x12-15";
            day6 = "Rest or Light Cardio";
        } else { // Maintain/Health
            reps = "10-12 reps";
            day1 = "Full Body Strength A (e.g., Squats, Push-ups, Rows) 3x" + reps;
            day2 = "Cardio & Core (e.g., Jogging for 25 mins, followed by Planks and Crunches)";
            day3 = "Full Body Strength B (e.g., Deadlifts, Overhead Press, Lunges) 3x" + reps;
            day4 = "Active Recovery (e.g., Swimming, a light hike, or Yoga)";
            day5 = "Full Body Circuit (Mix of exercises from Day 1 & 3)";
            day6 = "Recreational Activity (e.g., team sport, dancing, cycling with friends)";
        }

        String[] schedule = {day1, day2, day3, day4, day5, day6, "Complete Rest"};
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) {
            plan.append("• ").append(days[i]).append(": ").append(schedule[i]).append("\n");
        }
        return plan.toString();
    }

    private String generateMealPlan(int dietaryPreference, int fitnessGoal, int eatingHabits, int mealPlanningStyle) {
        StringBuilder plan = new StringBuilder("Nutrition Guide:\n\n");
        
        String[] breakfastOptions = new String[2];
        String[] lunchOptions = new String[2];
        String[] dinnerOptions = new String[2];
        String[] snackOptions = new String[2];

        // === 6 CORE DIET FRAMEWORKS ===
        if (dietaryPreference == 1) { // VEGETARIAN/VEGAN
            snackOptions = new String[]{"An apple with 2 tbsp of almond butter.", "A handful of mixed nuts (almonds, walnuts)."};
            if (fitnessGoal == 1) { // Weight Loss
                breakfastOptions = new String[]{"Tofu Scramble (1/2 block) with spinach and black salt.", "1/2 cup (dry) oatmeal, cooked with water, with 1/2 cup berries."};
                lunchOptions = new String[]{"Large lentil salad (1 cup cooked lentils) with mixed greens.", "2 large lettuce cups with a mix of black beans, corn, and salsa."};
                dinnerOptions = new String[]{"2 black bean burgers (no bun) with a large side salad.", "1.5 cups of lentil soup with a side of steamed green beans."};
            } else if (fitnessGoal == 2) { // Muscle Gain
                breakfastOptions = new String[]{"Large oatmeal (1 cup dry) made with soy milk and plant-based protein.", "3 slices of whole-wheat toast with a full can of baked beans."};
                lunchOptions = new String[]{"Large quinoa bowl (1.5 cups cooked) with a full block of firm tofu.", "A wrap containing a full can of chickpeas, hummus, and spinach."};
                dinnerOptions = new String[]{"Seitan (150-200g) stir-fry with peanut sauce and brown rice.", "Large portion of lentil pasta with a rich tomato and vegetable sauce."};
                snackOptions = new String[]{"A smoothie with soy milk, plant protein, a banana, and spinach.", "A pot of high-protein soy yogurt with nuts."};
            } else { // Maintain/Health
                breakfastOptions = new String[]{"2 slices of avocado toast on whole-grain bread, topped with seeds.", "Soy yogurt pot with granola and mixed berries."};
                lunchOptions = new String[]{"Hummus (4 tbsp) and vegetable wrap with a side of carrot sticks.", "A hearty mixed bean salad (1.5 cups) with a light dressing."};
                dinnerOptions = new String[]{"Lentil Shepherd's Pie with a sweet potato topping.", "Vegetable curry (using coconut milk) with 1 cup of brown rice."};
            }
        } else { // NON-VEGETARIAN
            snackOptions = new String[]{"An apple with a handful of walnuts.", "1 cup of Greek yogurt."};
            if (fitnessGoal == 1) { // Weight Loss
                breakfastOptions = new String[]{"Omelette (3 eggs) with spinach and feta cheese.", "1 cup Greek yogurt with a handful of berries."};
                lunchOptions = new String[]{"Large salad with 150g grilled chicken breast and light vinaigrette.", "A can of tuna in springwater, mixed with greens and a tbsp of mayo."};
                dinnerOptions = new String[]{"150g baked salmon with a large serving of roasted asparagus.", "150g lean turkey mince stir-fry with mixed peppers (no rice)."};
            } else if (fitnessGoal == 2) { // Muscle Gain
                breakfastOptions = new String[]{"4-5 scrambled eggs, 2 slices whole-wheat toast, and 1 cup oatmeal.", "A high-calorie protein shake (2 scoops protein, milk, banana, peanut butter)."};
                lunchOptions = new String[]{"200g lean beef mince with 1.5 cups of white rice and vegetables.", "200g grilled chicken breast in a large wrap with salad and cheese." };
                dinnerOptions = new String[]{"200g turkey meatballs with whole-wheat pasta and marinara.", "200g steak with a large sweet potato and a side of broccoli."};
                snackOptions = new String[]{"1.5 cups of cottage cheese with pineapple.", "A quality protein bar and a piece of fruit."};
            } else { // Maintain/Health
                breakfastOptions = new String[]{"Oatmeal (1 cup) with a scoop of whey protein and a banana.", "3 scrambled eggs on 2 slices of whole-grain toast."};
                lunchOptions = new String[]{"Turkey & Swiss cheese wrap with a side of carrots.", "A balanced meal prep box (150g chicken, 1 cup quinoa, mixed veg)."};
                dinnerOptions = new String[]{"150g salmon, 1 cup of quinoa, and roasted brussels sprouts.", "A healthy chicken and vegetable stir-fry with a moderate portion of noodles."};
            }
        }

        plan.append("• Breakfast Options:\n");
        plan.append("  - ").append(breakfastOptions[0]).append("\n");
        plan.append("  - ").append(breakfastOptions[1]).append("\n\n");
        plan.append("• Lunch Options:\n");
        plan.append("  - ").append(lunchOptions[0]).append("\n");
        plan.append("  - ").append(lunchOptions[1]).append("\n\n");
        plan.append("• Dinner Options:\n");
        plan.append("  - ").append(dinnerOptions[0]).append("\n");
        plan.append("  - ").append(dinnerOptions[1]).append("\n\n");
        plan.append("• Snack Options:\n");
        plan.append("  - ").append(snackOptions[0]).append("\n");
        plan.append("  - ").append(snackOptions[1]).append("\n\n");

        // Actionable Lifestyle Tips
        if (eatingHabits == 1) {
            plan.append("• Emotional Eating Tip:\n  - Instead of snacking when stressed, try a 5-minute walk or a cup of herbal tea first. Identify the trigger.\n\n");
        } else if (eatingHabits == 2) {
            plan.append("• Boredom Eating Tip:\n  - If you feel like snacking out of boredom, have a glass of water and wait 10 minutes. Often, we mistake thirst for hunger.\n\n");
        } else if (eatingHabits == 3) {
            plan.append("• Late-Night Snacking Tip:\n  - To curb late-night cravings, establish a relaxing pre-sleep routine like reading. Ensure your dinner was satisfying with enough protein and fiber.\n\n");
        } else if (eatingHabits == 4) {
            plan.append("• Social Eater Tip:\n  - Before going out, look at the menu online and choose a healthier option in advance. This removes in-the-moment decisions.\n\n");
        }
        if (mealPlanningStyle == 1) {
            plan.append("• Quick Meal Tip:\n  - Keep staples on hand for fast meals: canned beans/fish, bagged salads, frozen vegetables, and quick-cook grains like quinoa or couscous.\n\n");
        } else if (mealPlanningStyle == 2) {
            plan.append("• Meal Prep Tip:\n  - Cook a large batch of a versatile protein (e.g., lentils, chicken breast) and a carb (e.g., rice, quinoa) on Sunday to use in different meals all week.\n\n");
        }

        return plan.toString();
    }
    
    private String generateSupplementPlan(int dietaryPreference, int fitnessGoal, int motivation) {
        StringBuilder plan = new StringBuilder("Recommended Supplements:\n\n");
        plan.append("• Multivitamin: A good foundation to ensure you are not missing any key micronutrients from your diet.\n");
        plan.append("• Vitamin D3: Essential for bone health, immune function, and mood. Most people are deficient.\n");

        if (dietaryPreference == 1) {
            plan.append("• Vitamin B12: Non-negotiable. This is not found in plant foods and is critical for nerve function and producing red blood cells.\n");
            plan.append("• Plant-Based Protein Powder: A highly effective and convenient tool to help you meet your daily protein targets for recovery and growth.\n");
        } else {
            plan.append("• Whey/Casein Protein Powder: A convenient and bioavailable protein source to aid muscle repair and stimulate growth after workouts.\n");
        }

        if (fitnessGoal == 1) { // Weight Loss
            plan.append("• Green Tea Extract: Contains EGCG, an antioxidant that may help support a healthy metabolism alongside your diet and exercise plan.\n");
        } else if (fitnessGoal == 2) { // Muscle Gain
            plan.append("• Creatine Monohydrate (5g daily): The most scientifically-backed supplement for increasing strength, power output, and muscle mass.\n");
        } else { // Maintain/Health
            plan.append("• Omega-3 Fish Oil (or Algal Oil for Vegans): Supports heart health, brain function, and can help manage inflammation.\n");
        }

        if (motivation == 1) {
            plan.append("\nIMPORTANT: Given your motivation is health-related, you must consult with a healthcare professional before starting any supplement regimen.");
        }
        return plan.toString();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
