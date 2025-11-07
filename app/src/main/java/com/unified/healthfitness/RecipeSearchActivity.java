package com.unified.healthfitness;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;
import com.unified.healthfitness.R;


public class RecipeSearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private ChipGroup categoryChips;
    private RecyclerView recipesRecyclerView;
    private RecipeAdapter adapter;
    private List<RecipeItem> recipeList = new ArrayList<>();
    private static final String API_KEY = "";
    private String currentCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_search);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        loadRecipes("healthy");
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        categoryChips = findViewById(R.id.categoryChips);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(this, recipeList);
        recipesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipesRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    loadRecipes(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryChips() {
        String[] categories = {"Breakfast", "Lunch", "Dinner", "Snacks", "Smoothies", "Vegan", "Keto"};

        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                currentCategory = category.toLowerCase();
                loadRecipes(currentCategory);
            });
            categoryChips.addView(chip);
        }
    }

    private void loadRecipes(String query) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spoonacular.com/recipes/complexSearch?query=" + query
                + "&number=20&addRecipeInformation=true&apiKey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RecipeSearchActivity.this, "Network Error!", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray results = jsonObject.getJSONArray("results");

                    List<RecipeItem> newRecipes = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject recipe = results.getJSONObject(i);
                        int id = recipe.getInt("id");
                        String title = recipe.getString("title");
                        String imageUrl = recipe.getString("image");

                        newRecipes.add(new RecipeItem(id, title, imageUrl));
                    }

                    runOnUiThread(() -> {
                        recipeList.clear();
                        recipeList.addAll(newRecipes);
                        adapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(RecipeSearchActivity.this, "Parse Error!", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}