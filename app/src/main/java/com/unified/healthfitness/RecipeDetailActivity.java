package com.unified.healthfitness;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class RecipeDetailActivity extends AppCompatActivity {
    private TextView recipeTitle, recipeSummary, servingsText, readyTimeText, healthScoreText;
    private ImageView recipeImage;
    private RecyclerView ingredientsRecycler, instructionsRecycler;
    private ProgressBar loadingProgress;
    private View nutritionCard;
    private TextView caloriesText, proteinText, carbsText, fatText;

    private static final String API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail_enhanced);

        initViews();

        int id = getIntent().getIntExtra("RECIPE_ID", -1);
        String title = getIntent().getStringExtra("RECIPE_TITLE");
        String imageUrl = getIntent().getStringExtra("RECIPE_IMAGE_URL");

        recipeTitle.setText(title);
        Glide.with(this).load(imageUrl).into(recipeImage);

        if (id != -1) {
            fetchRecipeDetails(id);
        }
    }

    private void initViews() {
        recipeTitle = findViewById(R.id.recipeTitle);
        recipeSummary = findViewById(R.id.recipeSummary);
        recipeImage = findViewById(R.id.recipeImage);
        servingsText = findViewById(R.id.servingsText);
        readyTimeText = findViewById(R.id.readyTimeText);
        healthScoreText = findViewById(R.id.healthScoreText);
        ingredientsRecycler = findViewById(R.id.ingredientsRecycler);
        instructionsRecycler = findViewById(R.id.instructionsRecycler);
        loadingProgress = findViewById(R.id.loadingProgress);
        nutritionCard = findViewById(R.id.nutritionCard);
        caloriesText = findViewById(R.id.caloriesText);
        proteinText = findViewById(R.id.proteinText);
        carbsText = findViewById(R.id.carbsText);
        fatText = findViewById(R.id.fatText);
    }

    private void fetchRecipeDetails(int id) {
        loadingProgress.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spoonacular.com/recipes/" + id
                + "/information?apiKey=" + API_KEY
                + "&includeNutrition=true";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(RecipeDetailActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject recipe = new JSONObject(result);

                    String summary = recipe.optString("summary", "No description available.");
                    int servings = recipe.optInt("servings", 0);
                    int readyTime = recipe.optInt("readyInMinutes", 0);
                    int healthScore = recipe.optInt("healthScore", 0);

                    // Get ingredients
                    JSONArray ingredientsArray = recipe.optJSONArray("extendedIngredients");
                    List<String> ingredients = new ArrayList<>();
                    if (ingredientsArray != null) {
                        for (int i = 0; i < ingredientsArray.length(); i++) {
                            JSONObject ing = ingredientsArray.getJSONObject(i);
                            String original = ing.getString("original");
                            ingredients.add(original);
                        }
                    }

                    // Get instructions
                    JSONArray instructionsArray = recipe.optJSONArray("analyzedInstructions");
                    List<String> instructions = new ArrayList<>();
                    if (instructionsArray != null && instructionsArray.length() > 0) {
                        JSONObject inst = instructionsArray.getJSONObject(0);
                        JSONArray steps = inst.getJSONArray("steps");
                        for (int i = 0; i < steps.length(); i++) {
                            JSONObject step = steps.getJSONObject(i);
                            int number = step.getInt("number");
                            String stepText = step.getString("step");
                            instructions.add(number + ". " + stepText);
                        }
                    }

                    // Get nutrition
                    JSONObject nutrition = recipe.optJSONObject("nutrition");
                    String calories = "N/A";
                    String protein = "N/A";
                    String carbs = "N/A";
                    String fat = "N/A";

                    if (nutrition != null) {
                        JSONArray nutrients = nutrition.getJSONArray("nutrients");
                        for (int i = 0; i < nutrients.length(); i++) {
                            JSONObject nutrient = nutrients.getJSONObject(i);
                            String name = nutrient.getString("name");
                            String amount = nutrient.getString("amount");
                            String unit = nutrient.getString("unit");

                            if (name.equals("Calories")) {
                                calories = amount + " " + unit;
                            } else if (name.equals("Protein")) {
                                protein = amount + unit;
                            } else if (name.equals("Carbohydrates")) {
                                carbs = amount + unit;
                            } else if (name.equals("Fat")) {
                                fat = amount + unit;
                            }
                        }
                    }

                    final String finalSummary = summary;
                    final String finalCalories = calories;
                    final String finalProtein = protein;
                    final String finalCarbs = carbs;
                    final String finalFat = fat;

                    runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);

                        // Set basic info
                        recipeSummary.setText(android.text.Html.fromHtml(finalSummary, android.text.Html.FROM_HTML_MODE_COMPACT));
                        servingsText.setText(servings + " servings");
                        readyTimeText.setText(readyTime + " min");
                        healthScoreText.setText("Health: " + healthScore + "/100");

                        // Set nutrition
                        caloriesText.setText(finalCalories);
                        proteinText.setText(finalProtein);
                        carbsText.setText(finalCarbs);
                        fatText.setText(finalFat);
                        nutritionCard.setVisibility(View.VISIBLE);

                        // Set ingredients
                        if (!ingredients.isEmpty()) {
                            IngredientsAdapter ingredientsAdapter = new IngredientsAdapter(ingredients);
                            ingredientsRecycler.setLayoutManager(new LinearLayoutManager(RecipeDetailActivity.this));
                            ingredientsRecycler.setAdapter(ingredientsAdapter);
                        }

                        // Set instructions
                        if (!instructions.isEmpty()) {
                            InstructionsAdapter instructionsAdapter = new InstructionsAdapter(instructions);
                            instructionsRecycler.setLayoutManager(new LinearLayoutManager(RecipeDetailActivity.this));
                            instructionsRecycler.setAdapter(instructionsAdapter);
                        } else {
                            // If no structured instructions, show a message
                            instructions.add("No step-by-step instructions available. Please refer to the summary above.");
                            InstructionsAdapter instructionsAdapter = new InstructionsAdapter(instructions);
                            instructionsRecycler.setLayoutManager(new LinearLayoutManager(RecipeDetailActivity.this));
                            instructionsRecycler.setAdapter(instructionsAdapter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        Toast.makeText(RecipeDetailActivity.this, "Error parsing data!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}

// Ingredients Adapter
class IngredientsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
    private List<String> ingredients;

    IngredientsAdapter(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.ingredientText.setText("• " + ingredients.get(position));
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView ingredientText;

        ViewHolder(android.view.View itemView) {
            super(itemView);
            ingredientText = itemView.findViewById(R.id.ingredientText);
        }
    }
}

// Instructions Adapter
class InstructionsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<InstructionsAdapter.ViewHolder> {
    private List<String> instructions;

    InstructionsAdapter(List<String> instructions) {
        this.instructions = instructions;
    }

    @Override
    public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instruction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.instructionText.setText(instructions.get(position));
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView instructionText;

        ViewHolder(android.view.View itemView) {
            super(itemView);
            instructionText = itemView.findViewById(R.id.instructionText);
        }
    }
}