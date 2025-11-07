package com.unified.healthfitness;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import com.unified.healthfitness.R;
import com.unified.healthfitness.MealItem;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

public class AddMealDialog extends Dialog {

    private OnMealAddedListener listener;

    // UI Components
    private TextInputEditText foodInput;
    private AutoCompleteTextView mealTypeSpinner;
    private Button searchBtn;
    private Button addBtn;

    // Meal types
    private final String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snacks"};

    // Store search results
    private MealItem currentMealItem = null;

    // Callback interface matching your MealTrackerActivity
    public interface OnMealAddedListener {
        void onMealAdded(String mealType, MealItem mealItem);
    }

    public AddMealDialog(@NonNull Context context, OnMealAddedListener listener) {
        // Use Material Dialog theme from themes.xml
        super(context, R.style.AppTheme_Dialog);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request feature before setting content view
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_add_meal);

        // Initialize views
        initViews();

        // Setup meal type dropdown
        setupMealTypeDropdown();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        foodInput = findViewById(R.id.foodInput);
        mealTypeSpinner = findViewById(R.id.mealTypeSpinner);
        searchBtn = findViewById(R.id.searchBtn);
        addBtn = findViewById(R.id.addBtn);

        // Initially disable add button until search is performed
        addBtn.setEnabled(false);
        addBtn.setAlpha(0.5f);
    }

    private void setupMealTypeDropdown() {
        // Create adapter for the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                mealTypes
        );

        // Set adapter to AutoCompleteTextView
        mealTypeSpinner.setAdapter(adapter);

        // Set default selection based on time of day
        mealTypeSpinner.setText(getCurrentMealType(), false);
    }

    private void setupListeners() {
        // Search button click listener
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFood();
            }
        });

        // Add button click listener
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMeal();
            }
        });
    }

    private void searchFood() {
        String foodName = foodInput.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(foodName)) {
            Toast.makeText(getContext(), "Please enter a food name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for nutrition data
        NutritionData data = getNutritionData(foodName);

        // Create MealItem with the nutrition data
        currentMealItem = new MealItem(
                data.name,
                data.calories,
                data.protein,
                data.carbs,
                data.fat
        );

        // Show result to user
        Toast.makeText(getContext(),
                String.format("Found: %s - %.0f cal, %.1fg protein",
                        data.name, data.calories, data.protein),
                Toast.LENGTH_LONG).show();

        // Enable add button
        addBtn.setEnabled(true);
        addBtn.setAlpha(1.0f);
    }

    private void addMeal() {
        String mealType = mealTypeSpinner.getText().toString();

        // Validate that search was performed
        if (currentMealItem == null) {
            Toast.makeText(getContext(), "Please search for a food first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call listener with mealType and MealItem (matching your interface)
        if (listener != null) {
            listener.onMealAdded(mealType, currentMealItem);
        }

        // Show success message
        Toast.makeText(getContext(), "Meal added successfully!", Toast.LENGTH_SHORT).show();

        // Close dialog
        dismiss();
    }

    // Get nutrition data for the food
    private NutritionData getNutritionData(String foodName) {
        String lowerFood = foodName.toLowerCase();
        NutritionData data = new NutritionData();
        data.name = foodName;

        // Common foods with nutrition info
        // Format: calories, protein, carbs, fat

        // Fruits
        if (lowerFood.contains("apple")) {
            data.calories = 95; data.protein = 0.5; data.carbs = 25; data.fat = 0.3;
        } else if (lowerFood.contains("banana")) {
            data.calories = 105; data.protein = 1.3; data.carbs = 27; data.fat = 0.4;
        } else if (lowerFood.contains("orange")) {
            data.calories = 62; data.protein = 1.2; data.carbs = 15; data.fat = 0.2;
        }
        // Proteins
        else if (lowerFood.contains("chicken breast")) {
            data.calories = 165; data.protein = 31; data.carbs = 0; data.fat = 3.6;
        } else if (lowerFood.contains("chicken")) {
            data.calories = 239; data.protein = 27; data.carbs = 0; data.fat = 14;
        } else if (lowerFood.contains("egg")) {
            data.calories = 78; data.protein = 6; data.carbs = 0.6; data.fat = 5;
        } else if (lowerFood.contains("fish")) {
            data.calories = 206; data.protein = 22; data.carbs = 0; data.fat = 12;
        } else if (lowerFood.contains("steak") || lowerFood.contains("beef")) {
            data.calories = 271; data.protein = 26; data.carbs = 0; data.fat = 19;
        }
        // Carbs
        else if (lowerFood.contains("rice")) {
            data.calories = 206; data.protein = 4.3; data.carbs = 45; data.fat = 0.4;
        } else if (lowerFood.contains("pasta")) {
            data.calories = 220; data.protein = 8; data.carbs = 43; data.fat = 1.3;
        } else if (lowerFood.contains("bread")) {
            data.calories = 79; data.protein = 2.7; data.carbs = 15; data.fat = 1;
        } else if (lowerFood.contains("potato")) {
            data.calories = 163; data.protein = 4.3; data.carbs = 37; data.fat = 0.2;
        }
        // Dairy
        else if (lowerFood.contains("milk")) {
            data.calories = 149; data.protein = 8; data.carbs = 12; data.fat = 8;
        } else if (lowerFood.contains("yogurt")) {
            data.calories = 100; data.protein = 10; data.carbs = 13; data.fat = 2.5;
        } else if (lowerFood.contains("cheese")) {
            data.calories = 113; data.protein = 7; data.carbs = 1; data.fat = 9;
        }
        // Fast Food
        else if (lowerFood.contains("burger")) {
            data.calories = 354; data.protein = 20; data.carbs = 30; data.fat = 17;
        } else if (lowerFood.contains("pizza")) {
            data.calories = 285; data.protein = 12; data.carbs = 36; data.fat = 10;
        } else if (lowerFood.contains("sandwich")) {
            data.calories = 300; data.protein = 15; data.carbs = 35; data.fat = 10;
        }
        // Vegetables
        else if (lowerFood.contains("salad")) {
            data.calories = 150; data.protein = 3; data.carbs = 10; data.fat = 10;
        } else if (lowerFood.contains("broccoli")) {
            data.calories = 55; data.protein = 3.7; data.carbs = 11; data.fat = 0.6;
        } else if (lowerFood.contains("carrot")) {
            data.calories = 41; data.protein = 0.9; data.carbs = 10; data.fat = 0.2;
        }
        // Default values if food not found
        else {
            data.calories = 200; data.protein = 8; data.carbs = 25; data.fat = 8;
        }

        return data;
    }

    // Helper method to determine meal type based on current time
    private String getCurrentMealType() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 11) {
            return "Breakfast";
        } else if (hour >= 11 && hour < 16) {
            return "Lunch";
        } else if (hour >= 16 && hour < 21) {
            return "Dinner";
        } else {
            return "Snacks";
        }
    }

    // Inner class for nutrition data
    private static class NutritionData {
        String name;
        double calories;
        double protein;
        double carbs;
        double fat;
    }
}