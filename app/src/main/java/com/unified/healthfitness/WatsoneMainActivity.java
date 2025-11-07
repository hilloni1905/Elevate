package com.unified.healthfitness;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class WatsoneMainActivity extends AppCompatActivity {

    private static final String HUGGINGFACE_API_URL =
            "https://router.huggingface.co/hf-inference/models/SamLowe/roberta-base-go_emotions";

    private static final String API_KEY = "Bearer ";//api key

    private EditText inputText;
    private TextView resultText;
    private Button analyzeButton, clearButton;
    private final OkHttpClient client = new OkHttpClient();

    // ✅ Food recommendation map
    private final Map<String, String> foodSuggestions = new HashMap<String, String>() {{
        put("anger", "Try calming foods like chamomile tea, green tea, or oatmeal to relax.");
        put("joy", "Celebrate with something light and healthy, like a fruit smoothie or a salad.");
        put("sadness", "Boost mood with dark chocolate, bananas, or warm soup.");
        put("fear", "Try comfort foods like herbal tea, nuts, or yogurt for relaxation.");
        put("surprise", "Enjoy a fun snack like popcorn or trail mix.");
        put("love", "Romantic foods like strawberries, dark chocolate, or a nice pasta dish.");
        put("neutral", "Stay balanced with a healthy meal like grilled chicken and vegetables.");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watsone_main);

        inputText = findViewById(R.id.inputText);
        resultText = findViewById(R.id.resultText);
        analyzeButton = findViewById(R.id.analyzeButton);
        clearButton = findViewById(R.id.clearButton);

        // ✅ Allow network calls on main thread (for demo project only)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        analyzeButton.setOnClickListener(v -> analyzeMood());
        clearButton.setOnClickListener(v -> {
            inputText.setText("");
            resultText.setText("");
        });
    }

    private void analyzeMood() {
        String text = inputText.getText().toString().trim();
        if (text.isEmpty()) {
            resultText.setText("Please enter some text first.");
            return;
        }

        try {
            String jsonResponse = callHuggingFaceAPI(text);
            String emotion = parseEmotion(jsonResponse);

            String suggestion = foodSuggestions.getOrDefault(emotion.toLowerCase(),
                    "Try a balanced meal to maintain your mood.");

            resultText.setText("Detected Emotion: " + emotion + "\n\nRecommended Food: " + suggestion);

        } catch (Exception e) {
            e.printStackTrace();
            resultText.setText("Error: " + e.getMessage());
        }
    }

    private String callHuggingFaceAPI(String text) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonBody = "{\"inputs\": \"" + text + "\"}";

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(HUGGINGFACE_API_URL)
                .addHeader("Authorization", API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed: " + response.code() + " " + response.message());
            }
            return response.body().string();
        }
    }

    private String parseEmotion(String jsonResponse) {
        try {
            JSONArray array = new JSONArray(jsonResponse);
            JSONArray emotions = array.getJSONArray(0);

            JSONObject topEmotion = emotions.getJSONObject(0);
            String label = topEmotion.getString("label");
            double score = topEmotion.getDouble("score") * 100;

            return label; // Return just emotion name for mapping
        } catch (Exception e) {
            e.printStackTrace();
            return "neutral";
        }
    }
}
