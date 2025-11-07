package com.unified.healthfitness;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MealTrackerActivity extends AppCompatActivity {
    private TextView dateText, totalCalories;
    private LinearProgressIndicator proteinProgress, carbsProgress, fatProgress;
    private TextView proteinText, carbsText, fatText;
    private RecyclerView breakfastRecycler, lunchRecycler, dinnerRecycler, snacksRecycler;
    private FloatingActionButton fabAddMeal;
    private ImageButton btnPrevDay, btnNextDay;
    private SharedPreferences prefs;
    private MealDatabaseHelper dbHelper;

    private List<MealItem> breakfastList = new ArrayList<>();
    private List<MealItem> lunchList = new ArrayList<>();
    private List<MealItem> dinnerList = new ArrayList<>();
    private List<MealItem> snacksList = new ArrayList<>();

    private Calendar currentDate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateKeyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_tracker);

        prefs = getSharedPreferences("MealTrackerPrefs", MODE_PRIVATE);

        // Initialize database helper
        dbHelper = MealDatabaseHelper.getInstance(this);

        // Initialize date formats
        dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Start with today's date
        currentDate = Calendar.getInstance();

        initViews();
        updateDateDisplay();
        loadMealsForCurrentDate();
        updateStats();
        setupFab();
        setupDateNavigation();
        setupDateClickListener();
    }

    private void initViews() {
        dateText = findViewById(R.id.dateText);
        totalCalories = findViewById(R.id.totalCalories);
        proteinProgress = findViewById(R.id.proteinProgress);
        carbsProgress = findViewById(R.id.carbsProgress);
        fatProgress = findViewById(R.id.fatProgress);
        proteinText = findViewById(R.id.proteinText);
        carbsText = findViewById(R.id.carbsText);
        fatText = findViewById(R.id.fatText);

        breakfastRecycler = findViewById(R.id.breakfastRecycler);
        lunchRecycler = findViewById(R.id.lunchRecycler);
        dinnerRecycler = findViewById(R.id.dinnerRecycler);
        snacksRecycler = findViewById(R.id.snacksRecycler);
        fabAddMeal = findViewById(R.id.fabAddMeal);

        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);

        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        MealItemAdapter breakfastAdapter = new MealItemAdapter(this, breakfastList, this::onMealDeleted);
        breakfastRecycler.setLayoutManager(new LinearLayoutManager(this));
        breakfastRecycler.setAdapter(breakfastAdapter);
        breakfastRecycler.setNestedScrollingEnabled(false);

        MealItemAdapter lunchAdapter = new MealItemAdapter(this, lunchList, this::onMealDeleted);
        lunchRecycler.setLayoutManager(new LinearLayoutManager(this));
        lunchRecycler.setAdapter(lunchAdapter);
        lunchRecycler.setNestedScrollingEnabled(false);

        MealItemAdapter dinnerAdapter = new MealItemAdapter(this, dinnerList, this::onMealDeleted);
        dinnerRecycler.setLayoutManager(new LinearLayoutManager(this));
        dinnerRecycler.setAdapter(dinnerAdapter);
        dinnerRecycler.setNestedScrollingEnabled(false);

        MealItemAdapter snacksAdapter = new MealItemAdapter(this, snacksList, this::onMealDeleted);
        snacksRecycler.setLayoutManager(new LinearLayoutManager(this));
        snacksRecycler.setAdapter(snacksAdapter);
        snacksRecycler.setNestedScrollingEnabled(false);
    }

    private void setupDateNavigation() {
        btnPrevDay.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadMealsForCurrentDate();
            updateStats();
        });

        btnNextDay.setOnClickListener(v -> {
            // Don't allow future dates
            Calendar today = Calendar.getInstance();
            if (currentDate.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                    currentDate.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)) {
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadMealsForCurrentDate();
                updateStats();
            }
        });
    }

    private void setupDateClickListener() {
        dateText.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = (Calendar) currentDate.clone();

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentDate.set(Calendar.YEAR, year);
                    currentDate.set(Calendar.MONTH, month);
                    currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    updateDateDisplay();
                    loadMealsForCurrentDate();
                    updateStats();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Don't allow future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        String formattedDate = dateFormat.format(currentDate.getTime());
        dateText.setText(formattedDate);

        // Disable next button if viewing today
        Calendar today = Calendar.getInstance();
        boolean isToday = currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        btnNextDay.setEnabled(!isToday);
        btnNextDay.setAlpha(isToday ? 0.5f : 1.0f);
    }

    private String getCurrentDateKey() {
        return dateKeyFormat.format(currentDate.getTime());
    }

    private void loadMealsForCurrentDate() {
        String dateKey = getCurrentDateKey();

        // Clear existing lists
        breakfastList.clear();
        lunchList.clear();
        dinnerList.clear();
        snacksList.clear();

        // Load from database
        breakfastList.addAll(dbHelper.getMealsByDateAndType(dateKey, "Breakfast"));
        lunchList.addAll(dbHelper.getMealsByDateAndType(dateKey, "Lunch"));
        dinnerList.addAll(dbHelper.getMealsByDateAndType(dateKey, "Dinner"));
        snacksList.addAll(dbHelper.getMealsByDateAndType(dateKey, "Snacks"));

        notifyAdapters();
    }

    private void updateStats() {
        String dateKey = getCurrentDateKey();

        // Get nutrition totals from database
        MealDatabaseHelper.NutritionTotals totals = dbHelper.getNutritionTotals(dateKey);

        totalCalories.setText(String.format("%.0f cal", totals.calories));

        float proteinGoal = prefs.getFloat("proteinGoal", 150);
        float carbsGoal = prefs.getFloat("carbsGoal", 250);
        float fatGoal = prefs.getFloat("fatGoal", 70);

        int proteinPercent = (int) ((totals.protein / proteinGoal) * 100);
        int carbsPercent = (int) ((totals.carbs / carbsGoal) * 100);
        int fatPercent = (int) ((totals.fat / fatGoal) * 100);

        proteinProgress.setProgress(Math.min(proteinPercent, 100));
        carbsProgress.setProgress(Math.min(carbsPercent, 100));
        fatProgress.setProgress(Math.min(fatPercent, 100));

        proteinText.setText(String.format("%.1f / %.0fg", totals.protein, proteinGoal));
        carbsText.setText(String.format("%.1f / %.0fg", totals.carbs, carbsGoal));
        fatText.setText(String.format("%.1f / %.0fg", totals.fat, fatGoal));

        // Only save to dashboard if viewing today
        Calendar today = Calendar.getInstance();
        boolean isToday = currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

        if (isToday) {
            prefs.edit()
                    .putFloat("todayCalories", (float) totals.calories)
                    .putFloat("todayProtein", (float) totals.protein)
                    .putFloat("todayCarbs", (float) totals.carbs)
                    .putFloat("todayFat", (float) totals.fat)
                    .apply();
        }
    }

    private void setupFab() {
        fabAddMeal.setOnClickListener(v -> {
            AddMealDialog dialog = new AddMealDialog(this, (mealType, mealItem) -> {
                String dateKey = getCurrentDateKey();

                // Save to database
                long id = dbHelper.insertMeal(dateKey, mealType, mealItem);
                mealItem.id = id;
                mealItem.mealType = mealType;

                // Add to appropriate list
                switch (mealType) {
                    case "Breakfast":
                        breakfastList.add(mealItem);
                        break;
                    case "Lunch":
                        lunchList.add(mealItem);
                        break;
                    case "Dinner":
                        dinnerList.add(mealItem);
                        break;
                    case "Snacks":
                        snacksList.add(mealItem);
                        break;
                }

                notifyAdapters();
                updateStats();
            });
            dialog.show();
        });
    }

    private void onMealDeleted(MealItem item) {
        String dateKey = getCurrentDateKey();

        // Delete from database
        if (item.id != -1) {
            dbHelper.deleteMeal(item.id);
        } else {
            // Fallback if ID not set
            dbHelper.deleteMeal(dateKey, item.mealType, item);
        }

        // Remove from lists
        breakfastList.remove(item);
        lunchList.remove(item);
        dinnerList.remove(item);
        snacksList.remove(item);

        notifyAdapters();
        updateStats();
    }

    private void notifyAdapters() {
        if (breakfastRecycler.getAdapter() != null) {
            breakfastRecycler.getAdapter().notifyDataSetChanged();
        }
        if (lunchRecycler.getAdapter() != null) {
            lunchRecycler.getAdapter().notifyDataSetChanged();
        }
        if (dinnerRecycler.getAdapter() != null) {
            dinnerRecycler.getAdapter().notifyDataSetChanged();
        }
        if (snacksRecycler.getAdapter() != null) {
            snacksRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Optional: Clean up old data (e.g., keep only last 90 days)
        // dbHelper.deleteOldMeals(90);
    }
}