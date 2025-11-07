package com.unified.healthfitness;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiAIHelper {

    private static final String TAG = "GeminiAIHelper";
    private static final String API_KEY = ""; // Replace with your actual API key
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";

    private final Executor executor;
    private final Handler mainHandler;

    public GeminiAIHelper() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface AICallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Generate a personalized meal plan
     */

    /**
     * Generate a personalized workout plan
     */
    public void generateWorkoutPlan(int fitnessLevel, String goals, AICallback callback) {
        String prompt = String.format(
                "Create a workout plan for fitness level %d/10 with goals: %s.\n\n" +
                        "Format your response EXACTLY as a table with these columns separated by | (pipe):\n" +
                        "Category | Workout Name | Duration | Calories | Type\n\n" +
                        "Example format:\n" +
                        "Cardio | Running | 30 | 300 | Cardio\n" +
                        "Strength | Push-ups | 15 | 100 | Strength\n" +
                        "Strength | Squats | 20 | 150 | Strength\n" +
                        "Flexibility | Yoga | 25 | 80 | Flexibility\n\n" +
                        "Include 5-6 exercises covering cardio, strength, and flexibility.",
                fitnessLevel, goals
        );

        generateContent(prompt, callback);
    }

    /**
     * Get AI health advice
     */
    public void getHealthAdvice(String question, AICallback callback) {
        String prompt = "As a fitness and nutrition expert, answer this question: " + question +
                "\n\nProvide a clear, concise, and actionable response.";

        generateContent(prompt, callback);
    }

    /**
     * Core method to make API call to Gemini
     */
    private void generateContent(String prompt, AICallback callback) {
        executor.execute(() -> {
            try {
                // Build the API URL
                URL url = new URL(API_URL + API_KEY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000); // 30 seconds
                connection.setReadTimeout(30000);

                // Create JSON request body
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();

                part.put("text", prompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Read response
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse response
                    String generatedText = parseGeminiResponse(response.toString());

                    // Return on main thread
                    mainHandler.post(() -> callback.onSuccess(generatedText));

                } else {
                    // Error response
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)
                    );
                    StringBuilder errorResponse = new StringBuilder();
                    String line;

                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    Log.e(TAG, "Error Response: " + errorResponse.toString());
                    mainHandler.post(() -> callback.onError("API Error: " + responseCode));
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Exception in API call", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Parse the JSON response from Gemini API
     */
    private String parseGeminiResponse(String jsonResponse) {
        try {
            JSONObject responseObj = new JSONObject(jsonResponse);
            JSONArray candidates = responseObj.getJSONArray("candidates");

            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");

                if (parts.length() > 0) {
                    JSONObject part = parts.getJSONObject(0);
                    return part.getString("text");
                }
            }

            return "No response generated";

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
            return "Error parsing response";
        }
    }

    /**
     * Check if API key is configured
     */
    public boolean isApiKeyConfigured() {
        return !API_KEY.equals("") && !API_KEY.isEmpty();
    }
}