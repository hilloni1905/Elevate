package com.unified.healthfitness;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.unified.healthfitness.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.*;

public class RecipeActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchBtn;
    private ImageView recipeImage;
    private TextView recipeTitle, recipeAdditionalInfo;

    // Your Spoonacular API Key
    private static final String API_KEY = ""; //api key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
        recipeImage = findViewById(R.id.recipeImage);
        recipeTitle = findViewById(R.id.recipeTitle);
        recipeAdditionalInfo = findViewById(R.id.recipeAdditionalInfo);

        searchBtn.setOnClickListener(v -> searchRecipe());
    }

    private void searchRecipe() {
        String query = searchInput.getText().toString().trim();
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spoonacular.com/recipes/complexSearch?query=" + query + "&number=1&addRecipeInformation=true&apiKey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(RecipeActivity.this, "API Error!", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray results = jsonObject.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject firstRecipe = results.getJSONObject(0);
                        String title = firstRecipe.getString("title");
                        String imageUrl = firstRecipe.getString("image");
                        String summary = firstRecipe.optString("summary", "No summary available.");
                        runOnUiThread(() -> {
                            recipeTitle.setText(title);
                            recipeAdditionalInfo.setText(android.text.Html.fromHtml(summary));
                            Glide.with(RecipeActivity.this).load(imageUrl).into(recipeImage);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(RecipeActivity.this, "No recipe found", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(RecipeActivity.this, "Parse Error!", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
