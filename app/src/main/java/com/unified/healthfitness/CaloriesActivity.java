package com.unified.healthfitness;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.unified.healthfitness.R;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.*;

public class CaloriesActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchBtn;
    private ImageView recipeImage;
    private TextView caloriesInfo;

    // Your CalorieNinjas API Key
    private static final String API_KEY = "";//api key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);

        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
        recipeImage = findViewById(R.id.recipeImage);
        caloriesInfo = findViewById(R.id.caloriesInfo);

        searchBtn.setOnClickListener(v -> searchCalories());
    }

    private void searchCalories() {
        String query = searchInput.getText().toString().trim();
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.calorieninjas.com/v1/nutrition?query=" + query;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Api-Key", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CaloriesActivity.this, "API Error!", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject json = new JSONObject(result);
                    JSONArray items = json.getJSONArray("items");
                    if (items.length() > 0) {
                        JSONObject item = items.getJSONObject(0);
                        String name = item.getString("name");
                        String calories = String.valueOf(item.getDouble("calories"));
                        String protein = String.valueOf(item.getDouble("protein_g"));
                        String fat = String.valueOf(item.getDouble("fat_total_g"));
                        String carbs = String.valueOf(item.getDouble("carbohydrates_total_g"));

                        final String info = "Food: " + name +
                                "\nCalories: " + calories +
                                "\nProtein: " + protein + "g" +
                                "\nFat: " + fat + "g" +
                                "\nCarbs: " + carbs + "g";

                        runOnUiThread(() -> {
                            caloriesInfo.setText(info);
                            Glide.with(CaloriesActivity.this)
                                    .load("https://img.icons8.com/flat-round/64/000000/caloric-energy.png")
                                    .into(recipeImage);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(CaloriesActivity.this, "No info found", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(CaloriesActivity.this, "Parse Error!", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
