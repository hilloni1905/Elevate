package com.unified.healthfitness;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatAssistantActivity extends AppCompatActivity {
    private EditText promptEditText;
    private ImageButton sendButton, micButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;

    private GeminiApiService apiService;

    private static final int REQ_CODE_SPEECH_INPUT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme(); // ✅ apply theme before inflating UI
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_assistant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chatAssistantLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        promptEditText = findViewById(R.id.promptEditText);
        sendButton = findViewById(R.id.sendButton);
        micButton = findViewById(R.id.micButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // Chat list setup
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Show welcome message
        if (messages.isEmpty()) {
            addBotMessage("👋 Welcome To AI Assistant !");
        }

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(GeminiApiService.class);

        // Debug log API key
        Log.d("API_KEY_CHECK", "Loaded API Key: " + BuildConfig.API_KEY);

        // Send button
        sendButton.setOnClickListener(v -> {
            String prompt = promptEditText.getText().toString().trim();
            if (prompt.isEmpty()) {
                promptEditText.setError(getString(R.string.field_cannot_be_empty));
                return;
            }

            addUserMessage(prompt);
            promptEditText.setText("");

            if (isImageRequest(prompt)) {
                requestImageGeneration(prompt);
            } else {
                requestTextGeneration(prompt);
            }
        });

        // Mic button
        micButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 2000);
            } else {
                startSpeechInput();
            }
        });
    }

    // --- Theme methods ---
    private void saveTheme(int mode) {
        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putInt("theme_mode", mode)
                .apply();
    }

    private void applySavedTheme() {
        int savedMode = getSharedPreferences("settings", MODE_PRIVATE)
                .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System default"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Theme")
                .setItems(themes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            saveTheme(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case 1:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            saveTheme(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case 2:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            saveTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }
                });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            showThemeDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Chat helpers ---
    private boolean isImageRequest(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        return lower.contains("image") || lower.contains("picture")
                || lower.contains("generate image") || lower.contains("draw");
    }

    private void addUserMessage(String text) {
        ChatMessage cm = new ChatMessage(text, true);
        messages.add(cm);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        ChatMessage cm = new ChatMessage(text, false);
        messages.add(cm);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addLoading() {
        ChatMessage load = new ChatMessage("", false, true);
        messages.add(load);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }

    private void removeLoadingAt(int index) {
        if (index >= 0 && index < messages.size()) {
            messages.remove(index);
            chatAdapter.notifyItemRemoved(index);
        }
    }

    // --- API Request ---
    private void requestTextGeneration(String prompt) {
        addLoading();
        final int loadingIndex = messages.size() - 1;

        List<GeminiRequest.Part> parts = new ArrayList<>();
        parts.add(new GeminiRequest.Part(prompt));

        List<GeminiRequest.Content> contents = new ArrayList<>();
        contents.add(new GeminiRequest.Content(parts));

        GeminiRequest request = new GeminiRequest(contents);

        Call<GeminiResponse> call = apiService.generateContent(
                BuildConfig.API_KEY,
                request
        );

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                removeLoadingAt(loadingIndex);

                if (response.isSuccessful() && response.body() != null) {
                    String responseText = response.body().getFirstText();
                    addBotMessage(responseText != null ? responseText : "⚠️ Empty response from Gemini");
                } else {
                    String errBody = null;
                    try {
                        if (response.errorBody() != null) {
                            errBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}

                    Log.e("GeminiAPI", "API error: " + response.code() + " body: " + errBody);
                    addBotMessage("❌ Error " + response.code() +
                            (errBody != null ? ": " + errBody : ""));
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                removeLoadingAt(loadingIndex);
                addBotMessage("⚠️ Request failed: " + t.getMessage());
                Log.e("MainActivity", "Request error", t);
            }
        });
    }

    private void requestImageGeneration(String prompt) {
        addLoading();
        final int loadingIndex = messages.size() - 1;

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            final String fakeImageUrl = "https://via.placeholder.com/512.png?text=Generated+Image";

            runOnUiThread(() -> {
                removeLoadingAt(loadingIndex);
                ChatMessage imgMsg = new ChatMessage("", false, false, true, fakeImageUrl);
                messages.add(imgMsg);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                chatRecyclerView.scrollToPosition(messages.size() - 1);
            });
        }).start();
    }

    // --- Speech-to-text ---
    private void startSpeechInput() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                promptEditText.setText(result.get(0));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 2000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechInput();
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
