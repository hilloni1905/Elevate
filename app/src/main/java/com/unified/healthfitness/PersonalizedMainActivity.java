package com.unified.healthfitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.unified.healthfitness.R;
import com.unified.healthfitness.ResultActivity;
import com.unified.healthfitness.Question;

public class PersonalizedMainActivity extends AppCompatActivity {

    TextView tvQuestion, tvProgress, tvQuestionNumber;
    RadioGroup rgOptions;
    RadioButton rb1, rb2, rb3, rb4;
    Button btnNext, btnPrevious;
    ProgressBar progressBar;
    CardView cardQuestion;

    List<Question> questions;
    int current = 0;
    Map<String, Integer> scores = new HashMap<>();
    Map<Integer, Integer> userAnswers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_personalized); //

        initViews();
        initScores();
        loadQuestions();
        displayQuestion();
        setupListeners();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        rgOptions = findViewById(R.id.rgOptions);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        rb4 = findViewById(R.id.rb4);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        progressBar = findViewById(R.id.progressBar);
        cardQuestion = findViewById(R.id.cardQuestion);
    }

    private void initScores() {
        scores.put("Balanced", 0);
        scores.put("Emotional", 0);
        scores.put("Physical", 0);
        scores.put("Social", 0);
    }

    private void loadQuestions() {
        questions = new ArrayList<>();

        // Question 1: Activity Level
        questions.add(new Question(
                "What best describes your daily activity level?",
                "Sedentary (little to no exercise)",
                "Lightly active (exercise 1-3 days/week)",
                "Moderately active (exercise 3-5 days/week)",
                "Very active (exercise 6-7 days/week)",
                "Physical", "Physical", "Physical", "Physical"
        ));

        // Question 2: Dietary Preference
        questions.add(new Question(
                "What is your primary dietary preference?",
                "Vegetarian/Vegan",
                "Balanced (mix of everything)",
                "High protein (meat-focused)",
                "Low carb/Keto",
                "Emotional", "Balanced", "Physical", "Physical"
        ));

        // Question 3: Fitness Goal
        questions.add(new Question(
                "What is your primary fitness goal?",
                "Weight loss",
                "Muscle gain",
                "Maintain current weight",
                "Improve overall health",
                "Physical", "Physical", "Balanced", "Balanced"
        ));

        // Question 4: Eating Habits
        questions.add(new Question(
                "How would you describe your eating habits?",
                "I eat when stressed or emotional",
                "I follow a structured meal plan",
                "I eat based on hunger cues",
                "I often eat with friends/family",
                "Emotional", "Balanced", "Balanced", "Social"
        ));

        // Question 5: Workout Preference
        questions.add(new Question(
                "What type of workout do you prefer?",
                "Solo workouts at home",
                "Gym with structured routine",
                "Group fitness classes",
                "Outdoor activities/sports",
                "Physical", "Physical", "Social", "Social"
        ));

        // Question 6: Stress Management
        questions.add(new Question(
                "How do you typically manage stress?",
                "Comfort eating",
                "Exercise/Physical activity",
                "Meditation or relaxation",
                "Socializing with friends",
                "Emotional", "Physical", "Balanced", "Social"
        ));

        // Question 7: Meal Planning
        questions.add(new Question(
                "How do you approach meal planning?",
                "I don\'t plan, I eat spontaneously",
                "I prep meals for the week",
                "I cook fresh daily",
                "I often dine out or order food",
                "Emotional", "Balanced", "Balanced", "Social"
        ));

        // Question 8: Energy Levels
        questions.add(new Question(
                "When do you have the most energy?",
                "Morning (6 AM - 12 PM)",
                "Afternoon (12 PM - 6 PM)",
                "Evening (6 PM - 12 AM)",
                "Energy levels vary greatly",
                "Physical", "Physical", "Physical", "Emotional"
        ));

        // Question 9: Motivation
        questions.add(new Question(
                "What motivates you most to stay fit?",
                "Health concerns or medical advice",
                "Appearance and aesthetics",
                "Athletic performance",
                "Social acceptance and confidence",
                "Balanced", "Physical", "Physical", "Social"
        ));

        // Question 10: Challenges
        questions.add(new Question(
                "What\'s your biggest challenge with diet/fitness?",
                "Emotional eating patterns",
                "Lack of time for workouts",
                "Difficulty following a routine",
                "Lack of social support",
                "Emotional", "Physical", "Balanced", "Social"
        ));
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (rgOptions.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            saveAnswer();

            if (current < questions.size() - 1) {
                current++;
                displayQuestion();
            } else {
                showResults();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (current > 0) {
                current--;
                displayQuestion();
            }
        });
    }

    private void displayQuestion() {
        Question q = questions.get(current);

        tvQuestion.setText(q.getQuestion());
        tvQuestionNumber.setText("Question " + (current + 1));
        tvProgress.setText((current + 1) + " / " + questions.size());
        progressBar.setMax(questions.size());
        progressBar.setProgress(current + 1);

        rb1.setText(q.getOption1());
        rb2.setText(q.getOption2());
        rb3.setText(q.getOption3());
        rb4.setText(q.getOption4());

        // Restore previous answer if exists
        if (userAnswers.containsKey(current)) {
            int savedAnswer = userAnswers.get(current);
            switch (savedAnswer) {
                case 1: rb1.setChecked(true); break;
                case 2: rb2.setChecked(true); break;
                case 3: rb3.setChecked(true); break;
                case 4: rb4.setChecked(true); break;
            }
        } else {
            rgOptions.clearCheck();
        }

        // Update button states
        btnPrevious.setEnabled(current > 0);
        btnPrevious.setAlpha(current > 0 ? 1.0f : 0.5f);
        btnNext.setText(current == questions.size() - 1 ? "Finish" : "Next");
    }

    private void saveAnswer() {
        Question q = questions.get(current);
        int selectedId = rgOptions.getCheckedRadioButtonId();

        int answerIndex = 0;
        if (selectedId == rb1.getId()) answerIndex = 1;
        else if (selectedId == rb2.getId()) answerIndex = 2;
        else if (selectedId == rb3.getId()) answerIndex = 3;
        else if (selectedId == rb4.getId()) answerIndex = 4;

        // Save user answer
        userAnswers.put(current, answerIndex);

        // Remove previous score for this question if re-answering
        String category = q.getCategoryForOption(answerIndex);
        scores.put(category, scores.get(category) + 1);
    }

    private void showResults() {
        // Calculate dominant category
        String dominantCategory = "";
        int maxScore = 0;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                dominantCategory = entry.getKey();
            }
        }

        // Create intent to ResultActivity
        Intent intent = new Intent(PersonalizedMainActivity.this, ResultActivity.class);
        intent.putExtra("dominant_category", dominantCategory);
        intent.putExtra("balanced_score", scores.get("Balanced"));
        intent.putExtra("emotional_score", scores.get("Emotional"));
        intent.putExtra("physical_score", scores.get("Physical"));
        intent.putExtra("social_score", scores.get("Social"));
        intent.putExtra("user_answers", (Serializable) userAnswers);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }
}