package com.unified.healthfitness;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class WorkoutSearchActivity extends AppCompatActivity {
    private static final String TAG = "WorkoutSearchActivity";
    private EditText searchInput;
    private ChipGroup muscleChips, equipmentChips;
    private RecyclerView workoutsRecyclerView;
    private WorkoutAdapter adapter;
    private List<WorkoutItem> workoutList = new ArrayList<>();
    private List<WorkoutItem> allExercises = new ArrayList<>();
    private FloatingActionButton fabViewLogs;
    private ProgressBar progressBar;
    private static final String BASE_URL = "https://exercisedb-api.vercel.app/api/v1";
    private String currentBodyPart = "";
    private String currentEquipment = "";
    private String currentSearchName = "";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_search);
        initViews();
        setupRecyclerView();
        setupSearch();
        setupMuscleChips();
        setupEquipmentChips();
        loadExercisesInBatches();
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        muscleChips = findViewById(R.id.muscleChips);
        equipmentChips = findViewById(R.id.difficultyChips);
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView);
        fabViewLogs = findViewById(R.id.fabViewLogs);
        progressBar = findViewById(R.id.progressBar);
        fabViewLogs.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutSearchActivity.this, WorkoutLoggerActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new WorkoutAdapter(this, workoutList);
        workoutsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        workoutsRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchName = s.toString().trim().toLowerCase();
                applyCurrentFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupMuscleChips() {
        // Use lowercase to match API body parts
        String[] bodyParts = {
                "back", "cardio", "chest", "lower arms", "lower legs",
                "neck", "shoulders", "upper arms", "upper legs", "waist"
        };

        for (String bodyPart : bodyParts) {
            Chip chip = new Chip(this);
            chip.setText(capitalizeWords(bodyPart)); // Display: "Upper Arms"
            chip.setCheckable(true);
            chip.setTag(bodyPart); // Store lowercase for filtering
            chip.setOnClickListener(v -> {
                for (int i = 0; i < muscleChips.getChildCount(); i++) {
                    Chip c = (Chip) muscleChips.getChildAt(i);
                    if (c != chip) {
                        c.setChecked(false);
                    }
                }
                if (chip.isChecked()) {
                    currentBodyPart = (String) chip.getTag(); // Use lowercase tag
                    currentEquipment = "";
                    clearEquipmentChips();
                } else {
                    currentBodyPart = "";
                }
                applyCurrentFilters();
            });
            muscleChips.addView(chip);
        }
    }

    private void setupEquipmentChips() {
        // Use lowercase to match API equipment types
        String[] equipmentTypes = {
                "barbell", "dumbbell", "kettlebell", "cable", "machine",
                "body weight", "resistance band", "medicine ball",
                "exercise ball", "ez curl bar", "smith machine"
        };

        for (String equipment : equipmentTypes) {
            Chip chip = new Chip(this);
            chip.setText(capitalizeWords(equipment)); // Display: "Body Weight"
            chip.setCheckable(true);
            chip.setTag(equipment); // Store lowercase for filtering
            chip.setOnClickListener(v -> {
                for (int i = 0; i < equipmentChips.getChildCount(); i++) {
                    Chip c = (Chip) equipmentChips.getChildAt(i);
                    if (c != chip) {
                        c.setChecked(false);
                    }
                }
                if (chip.isChecked()) {
                    currentEquipment = (String) chip.getTag(); // Use lowercase tag
                    currentBodyPart = "";
                    clearMuscleChips();
                } else {
                    currentEquipment = "";
                }
                applyCurrentFilters();
            });
            equipmentChips.addView(chip);
        }
    }

    private String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1));
        }
        return result.toString();
    }

    private void clearMuscleChips() {
        for (int i = 0; i < muscleChips.getChildCount(); i++) {
            Chip c = (Chip) muscleChips.getChildAt(i);
            c.setChecked(false);
        }
    }

    private void clearEquipmentChips() {
        for (int i = 0; i < equipmentChips.getChildCount(); i++) {
            Chip c = (Chip) equipmentChips.getChildAt(i);
            c.setChecked(false);
        }
    }

    private void loadExercisesInBatches() {
        if (isLoading) return;
        isLoading = true;
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Loading exercises...", Toast.LENGTH_SHORT).show();
        });
        loadBatch(0, 100, new ArrayList<>());
    }

    private void loadBatch(int offset, int limit, List<WorkoutItem> accumulatedExercises) {
        String url = BASE_URL + "/exercises?limit=" + limit + "&offset=" + offset;
        Log.d(TAG, "Loading batch: offset=" + offset + ", limit=" + limit);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network Error: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    Toast.makeText(WorkoutSearchActivity.this,
                            "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e(TAG, "API Error " + response.code() + ": " + errorBody);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                        if (!accumulatedExercises.isEmpty()) {
                            allExercises.addAll(accumulatedExercises);
                            applyCurrentFilters();
                            Toast.makeText(WorkoutSearchActivity.this,
                                    "Loaded " + allExercises.size() + " exercises ✓", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WorkoutSearchActivity.this,
                                    "API Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                try {
                    String result = response.body().string();
                    JSONObject responseObj = new JSONObject(result);
                    JSONArray jsonArray;
                    if (responseObj.has("data")) {
                        jsonArray = responseObj.getJSONArray("data");
                    } else if (responseObj.has("exercises")) {
                        jsonArray = responseObj.getJSONArray("exercises");
                    } else {
                        jsonArray = new JSONArray(result);
                    }

                    Log.d(TAG, "Batch loaded: " + jsonArray.length() + " exercises");

                    // Debug first exercise
                    if (offset == 0 && jsonArray.length() > 0) {
                        JSONObject first = jsonArray.getJSONObject(0);
                        Log.d(TAG, "Sample exercise structure: " + first.toString());
                    }

                    List<WorkoutItem> batchExercises = parseExercises(jsonArray);
                    accumulatedExercises.addAll(batchExercises);

                    // Load more batches (up to 1400 exercises - covers full database)
                    if (jsonArray.length() == limit && offset < 1300) {
                        loadBatch(offset + limit, limit, accumulatedExercises);
                    } else {
                        runOnUiThread(() -> {
                            allExercises.clear();
                            allExercises.addAll(accumulatedExercises);
                            progressBar.setVisibility(View.GONE);
                            isLoading = false;
                            applyCurrentFilters();
                            Log.d(TAG, "Total exercises loaded: " + allExercises.size());
                            Toast.makeText(WorkoutSearchActivity.this,
                                    "Loaded " + allExercises.size() + " exercises ✓", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse Error: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                        if (!accumulatedExercises.isEmpty()) {
                            allExercises.addAll(accumulatedExercises);
                            applyCurrentFilters();
                        }
                        Toast.makeText(WorkoutSearchActivity.this,
                                "Parse Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private List<WorkoutItem> parseExercises(JSONArray jsonArray) throws Exception {
        List<WorkoutItem> exercises = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject exercise = jsonArray.getJSONObject(i);
            String name = exercise.optString("name", "Unknown Exercise");

            // Parse bodyParts array - take first one (lowercase)
            String bodyPart = "general";
            if (exercise.has("bodyParts")) {
                JSONArray bodyParts = exercise.optJSONArray("bodyParts");
                if (bodyParts != null && bodyParts.length() > 0) {
                    bodyPart = bodyParts.optString(0, "general").toLowerCase();
                }
            } else if (exercise.has("bodyPart")) {
                bodyPart = exercise.optString("bodyPart", "general").toLowerCase();
            }

            // Parse equipments array - take first one (lowercase)
            String equipment = "body weight";
            if (exercise.has("equipments")) {
                JSONArray equipments = exercise.optJSONArray("equipments");
                if (equipments != null && equipments.length() > 0) {
                    equipment = equipments.optString(0, "body weight").toLowerCase();
                }
            } else if (exercise.has("equipment")) {
                equipment = exercise.optString("equipment", "body weight").toLowerCase();
            }

            // Get target muscles
            String target = "multiple";
            if (exercise.has("targetMuscles")) {
                JSONArray targets = exercise.optJSONArray("targetMuscles");
                if (targets != null && targets.length() > 0) {
                    target = targets.optString(0, "multiple");
                }
            } else if (exercise.has("target")) {
                target = exercise.optString("target", "multiple");
            }

            // Get GIF/image URL
            String gifUrl = "";
            if (exercise.has("gifUrl")) {
                gifUrl = exercise.optString("gifUrl", "");
            } else if (exercise.has("imageUrl")) {
                gifUrl = exercise.optString("imageUrl", "");
            }

            // Get secondary muscles
            String secondaryMuscles = "";
            if (exercise.has("secondaryMuscles")) {
                JSONArray secMuscles = exercise.optJSONArray("secondaryMuscles");
                if (secMuscles != null) {
                    StringBuilder muscles = new StringBuilder();
                    for (int j = 0; j < secMuscles.length() && j < 2; j++) {
                        if (j > 0) muscles.append(", ");
                        muscles.append(secMuscles.optString(j, ""));
                    }
                    secondaryMuscles = muscles.toString();
                }
            }

            // Get instructions
            String instructions = "";
            if (exercise.has("instructions")) {
                JSONArray instrArray = exercise.optJSONArray("instructions");
                if (instrArray != null && instrArray.length() > 0) {
                    StringBuilder instr = new StringBuilder();
                    for (int j = 0; j < instrArray.length(); j++) {
                        instr.append((j + 1)).append(". ")
                                .append(instrArray.optString(j, "")).append("\n\n");
                    }
                    instructions = instr.toString();
                }
            }

            if (instructions.isEmpty()) {
                instructions = "1. Position yourself correctly\n\n2. Perform the movement with proper form\n\n3. Control the movement throughout\n\n4. Breathe properly during execution";
            }

            String difficulty = getDifficultyFromEquipment(equipment);
            exercises.add(new WorkoutItem(
                    name,
                    bodyPart,
                    target,
                    equipment,
                    difficulty,
                    instructions,
                    gifUrl,
                    secondaryMuscles
            ));
        }
        return exercises;
    }

    private void applyCurrentFilters() {
        if (allExercises.isEmpty()) {
            return;
        }

        List<WorkoutItem> filtered = new ArrayList<>();
        for (WorkoutItem exercise : allExercises) {
            // All comparisons now use lowercase
            String exerciseBodyPart = exercise.type.toLowerCase().trim();
            String exerciseEquipment = exercise.equipment.toLowerCase().trim();
            String exerciseName = exercise.name.toLowerCase().trim();
            String filterBodyPart = currentBodyPart.toLowerCase().trim();
            String filterEquipment = currentEquipment.toLowerCase().trim();
            String filterName = currentSearchName.toLowerCase().trim();

            // Check if exercise matches filters
            boolean matchesBodyPart = filterBodyPart.isEmpty() ||
                    exerciseBodyPart.equals(filterBodyPart) ||
                    exerciseBodyPart.contains(filterBodyPart);

            boolean matchesEquipment = filterEquipment.isEmpty() ||
                    exerciseEquipment.equals(filterEquipment) ||
                    exerciseEquipment.contains(filterEquipment);

            boolean matchesName = filterName.isEmpty() ||
                    exerciseName.contains(filterName);

            if (matchesBodyPart && matchesEquipment && matchesName) {
                filtered.add(exercise);
            }
        }

        workoutList.clear();
        workoutList.addAll(filtered);
        adapter.notifyDataSetChanged();

        // Build filter info message
        String filterInfo = "";
        if (!currentBodyPart.isEmpty()) {
            filterInfo += capitalizeWords(currentBodyPart);
        }
        if (!currentEquipment.isEmpty()) {
            if (!filterInfo.isEmpty()) filterInfo += " • ";
            filterInfo += capitalizeWords(currentEquipment);
        }
        if (!currentSearchName.isEmpty()) {
            if (!filterInfo.isEmpty()) filterInfo += " • ";
            filterInfo += "\"" + currentSearchName + "\"";
        }

        String message = filtered.size() + " exercise" + (filtered.size() != 1 ? "s" : "");
        if (!filterInfo.isEmpty()) {
            message += " (" + filterInfo + ")";
        }

        Log.d(TAG, "Filter applied: " + message);
        Log.d(TAG, "  Body Part filter: '" + currentBodyPart + "'");
        Log.d(TAG, "  Equipment filter: '" + currentEquipment + "'");
        Log.d(TAG, "  Name filter: '" + currentSearchName + "'");
        Log.d(TAG, "  Total in cache: " + allExercises.size());
        Log.d(TAG, "  Filtered results: " + filtered.size());
    }

    private String getDifficultyFromEquipment(String equipment) {
        String eq = equipment.toLowerCase();
        if (eq.contains("body") || eq.contains("weight") && !eq.contains("dumbbell") && !eq.contains("barbell")) {
            return "Beginner";
        } else if (eq.contains("dumbbell") || eq.contains("kettlebell") || eq.contains("band")) {
            return "Intermediate";
        } else if (eq.contains("barbell") || eq.contains("cable") || eq.contains("machine")) {
            return "Expert";
        }
        return "Intermediate";
    }
}