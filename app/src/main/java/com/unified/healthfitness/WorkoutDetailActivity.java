package com.unified.healthfitness;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WorkoutDetailActivity extends AppCompatActivity {

    private TextView workoutName, workoutType, workoutMuscle, workoutEquipment;
    private TextView workoutDifficulty, workoutInstructions, workoutSecondaryMuscles;
    private ImageView workoutGif;
    private ProgressBar gifLoadingProgress;
    private CardView infoCard, instructionsCard;
    private Button btnLogWorkout;

    private String currentWorkoutName;
    private String currentBodyPart;
    private String currentMuscle;
    private String currentEquipment;
    private String currentDifficulty;

    private WorkoutRepository repository;
    private String selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        repository = new WorkoutRepository(this);

        initViews();
        loadWorkoutData();
        setupLogButton();
    }

    private void initViews() {
        workoutName = findViewById(R.id.workoutName);
        workoutType = findViewById(R.id.workoutType);
        workoutMuscle = findViewById(R.id.workoutMuscle);
        workoutEquipment = findViewById(R.id.workoutEquipment);
        workoutDifficulty = findViewById(R.id.workoutDifficulty);
        workoutInstructions = findViewById(R.id.workoutInstructions);
        workoutGif = findViewById(R.id.workoutGif);
        gifLoadingProgress = findViewById(R.id.gifLoadingProgress);
        workoutSecondaryMuscles = findViewById(R.id.workoutSecondaryMuscles);
        infoCard = findViewById(R.id.infoCard);
        instructionsCard = findViewById(R.id.instructionsCard);
        btnLogWorkout = findViewById(R.id.btnLogWorkout);
    }

    private void loadWorkoutData() {
        String name = safeGetExtra("WORKOUT_NAME");
        String type = safeGetExtra("WORKOUT_TYPE");
        String muscle = safeGetExtra("WORKOUT_MUSCLE");
        String equipment = safeGetExtra("WORKOUT_EQUIPMENT");
        String difficulty = safeGetExtra("WORKOUT_DIFFICULTY");
        String instructions = safeGetExtra("WORKOUT_INSTRUCTIONS");
        String gifUrl = safeGetExtra("WORKOUT_GIF_URL");
        String secondaryMuscles = safeGetExtra("WORKOUT_SECONDARY_MUSCLES");

        currentWorkoutName = name;
        currentBodyPart = type;
        currentMuscle = muscle;
        currentEquipment = equipment;
        currentDifficulty = difficulty;

        workoutName.setText(capitalizeWords(name));
        workoutType.setText(capitalizeWords(type));
        workoutMuscle.setText(capitalizeWords(muscle));
        workoutEquipment.setText(capitalizeWords(equipment));
        workoutDifficulty.setText("Difficulty: " + capitalizeWords(difficulty));
        workoutInstructions.setText(instructions);

        if (!secondaryMuscles.isEmpty()) {
            workoutSecondaryMuscles.setVisibility(View.VISIBLE);
            workoutSecondaryMuscles.setText(capitalizeWords(secondaryMuscles));
        } else {
            workoutSecondaryMuscles.setVisibility(View.GONE);
        }

        if (!gifUrl.isEmpty()) {
            gifLoadingProgress.setVisibility(View.VISIBLE);
            workoutGif.setVisibility(View.VISIBLE);

            Log.d("WorkoutDetail", "Loading image from: " + gifUrl);

            Glide.with(this)
                    .asDrawable()
                    .load(gifUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    @NonNull Target<Drawable> target,
                                                    boolean isFirstResource) {
                            Log.e("WorkoutDetail", "Failed to load image: " +
                                    (e != null ? e.getMessage() : "Unknown error"));
                            gifLoadingProgress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, Object model,
                                                       @NonNull Target<Drawable> target,
                                                       @NonNull DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d("WorkoutDetail", "Image loaded successfully");
                            gifLoadingProgress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(workoutGif);
        } else {
            Log.d("WorkoutDetail", "No image URL provided");
            gifLoadingProgress.setVisibility(View.GONE);
            workoutGif.setImageResource(android.R.drawable.ic_menu_gallery);
            workoutGif.setVisibility(View.VISIBLE);
        }

        int difficultyColor = Color.GRAY;
        if (!difficulty.isEmpty()) {
            switch (difficulty.toLowerCase()) {
                case "beginner":
                    difficultyColor = Color.parseColor("#4CAF50");
                    break;
                case "intermediate":
                    difficultyColor = Color.parseColor("#FF9800");
                    break;
                case "expert":
                    difficultyColor = Color.parseColor("#F44336");
                    break;
            }
        }
        workoutDifficulty.setTextColor(difficultyColor);
    }

    private void setupLogButton() {
        btnLogWorkout.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Create selected date
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

                    // Format date as yyyy-MM-dd
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = dateFormat.format(selectedCalendar.getTime());

                    // Show workout input dialog
                    showLogWorkoutDialog();
                },
                year, month, day
        );

        // Set max date to today (cannot select future dates)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Optional: Set min date (e.g., 1 year ago)
        Calendar minCalendar = Calendar.getInstance();
        minCalendar.add(Calendar.YEAR, -1);
        datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());

        datePickerDialog.setTitle("Select Workout Date");
        datePickerDialog.show();
    }

    private void showLogWorkoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_workout, null);

        EditText setsInput = dialogView.findViewById(R.id.setsInput);
        EditText repsInput = dialogView.findViewById(R.id.repsInput);
        EditText notesInput = dialogView.findViewById(R.id.notesInput);

        // Show selected date in dialog
        String displayDate = WorkoutRepository.formatDisplayDate(selectedDate);

        builder.setView(dialogView)
                .setTitle("Log: " + currentWorkoutName + "\n📅 " + displayDate)
                .setPositiveButton("Save", (dialog, which) -> {
                    String setsStr = setsInput.getText().toString();
                    String repsStr = repsInput.getText().toString();
                    String notes = notesInput.getText().toString();

                    if (setsStr.isEmpty() || repsStr.isEmpty()) {
                        Toast.makeText(this, "Please enter sets and reps", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int sets = Integer.parseInt(setsStr);
                    int reps = Integer.parseInt(repsStr);

                    // Create datetime string for display
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    String dateTime = displayFormat.format(new Date());

                    // Create workout log entity
                    WorkoutLogEntity logEntity = new WorkoutLogEntity(
                            currentWorkoutName,
                            currentBodyPart,
                            currentMuscle,
                            currentEquipment,
                            currentDifficulty,
                            sets,
                            reps,
                            selectedDate,
                            dateTime,
                            System.currentTimeMillis(),
                            notes
                    );

                    // Insert into database
                    repository.insertLog(logEntity, new WorkoutRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long id) {
                            showSuccessDialog();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(WorkoutDetailActivity.this,
                                    "Error saving workout: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Workout Logged! 💪")
                .setMessage("Your workout has been saved for " +
                        WorkoutRepository.formatDisplayDate(selectedDate) + ".")
                .setPositiveButton("View My Logs", (dialog, which) -> {
                    Intent intent = new Intent(WorkoutDetailActivity.this, WorkoutLoggerActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    private String safeGetExtra(String key) {
        String v = getIntent().getStringExtra(key);
        return v == null ? "" : v;
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return "";
        String[] words = text.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.shutdown();
    }
}