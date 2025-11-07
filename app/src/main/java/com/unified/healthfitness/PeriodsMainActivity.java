package com.unified.healthfitness;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PeriodsMainActivity extends AppCompatActivity {

    private static final String TAG = "PeriodsMainActivity";

    private SharedPreferences prefs;
    private OkHttpClient httpClient;

    // Views
    private ScrollView homeLayout, musicLayout;
    private TextView phaseText, dayText, foodText, statusText, musicTitle;
    private EditText lastPeriodInput, cycleLengthInput;
    private Button saveButton, connectButton;
    private LinearLayout playlistContainer;

    private PhaseViewModel mPhaseViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_periods);

        prefs = getSharedPreferences("PeriodPrefs", MODE_PRIVATE);
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        mPhaseViewModel = new ViewModelProvider(this).get(PhaseViewModel.class);

        // Initialize views
        homeLayout = findViewById(R.id.homeLayout);
        musicLayout = findViewById(R.id.musicLayout);
        phaseText = findViewById(R.id.phaseText);
        dayText = findViewById(R.id.dayText);
        foodText = findViewById(R.id.foodText);
        lastPeriodInput = findViewById(R.id.lastPeriodInput);
        cycleLengthInput = findViewById(R.id.cycleLengthInput);
        saveButton = findViewById(R.id.saveButton);
        statusText = findViewById(R.id.statusText);
        connectButton = findViewById(R.id.connectButton);
        playlistContainer = findViewById(R.id.playlistContainer);
        musicTitle = findViewById(R.id.musicTitle);

        // Load saved data
        lastPeriodInput.setText(prefs.getString("lastPeriod", ""));
        cycleLengthInput.setText(prefs.getString("cycleLength", "28"));

        lastPeriodInput.setOnClickListener(v -> showDatePicker());

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                showHome();
                return true;
            } else if (item.getItemId() == R.id.nav_music) {
                showMusic();
                return true;
            }
            return false;
        });

        saveButton.setOnClickListener(v -> saveAndCalculate());
        connectButton.setOnClickListener(v -> {
            SharedPreferences spotifyPrefs = getSharedPreferences(SpotifyLoginActivity.getPrefsName(), MODE_PRIVATE);
            String token = SpotifyLoginActivity.getAccessToken(spotifyPrefs);
            if (token == null) {
                startActivity(new Intent(this, SpotifyLoginActivity.class));
            } else {
                loadPlaylists(token);
            }
        });

        mPhaseViewModel.getPhase().observe(this, phase -> {
            if (phase != null) {
                prefs.edit().putString("currentPhase", phase.name).apply();
                phaseText.setText(phase.name + " Phase");
                if (phase.recommendedFoods != null) {
                    foodText.setText(Html.fromHtml(phase.recommendedFoods, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    foodText.setText("");
                }
            }
        });

        // Initial calculation
        if (!lastPeriodInput.getText().toString().isEmpty()) {
            calculatePhase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the music layout is visible, refresh its content
        if (musicLayout.getVisibility() == View.VISIBLE) {
            showMusic();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
            lastPeriodInput.setText(selectedDate);
        }, year, month, day).show();
    }

    private void showHome() {
        homeLayout.setVisibility(View.VISIBLE);
        musicLayout.setVisibility(View.GONE);
    }

    private void showMusic() {
        homeLayout.setVisibility(View.GONE);
        musicLayout.setVisibility(View.VISIBLE);

        String phase = prefs.getString("currentPhase", "");
        if (!phase.isEmpty()) {
            musicTitle.setText("Music for Your " + phase + " Phase");
        }

        SharedPreferences spotifyPrefs = getSharedPreferences(SpotifyLoginActivity.getPrefsName(), MODE_PRIVATE);
        String token = SpotifyLoginActivity.getAccessToken(spotifyPrefs);
        if (token != null) {
            statusText.setText("Connected to Spotify ✓");
            connectButton.setText("Reconnect Spotify");
            loadPlaylists(token);
        } else {
            statusText.setText("Connect Spotify to view music suggestions");
            connectButton.setText("Connect Spotify");
        }
    }

    private void saveAndCalculate() {
        String lastPeriod = lastPeriodInput.getText().toString().trim();
        String cycleLength = cycleLengthInput.getText().toString().trim();

        if (lastPeriod.isEmpty() || cycleLength.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString("lastPeriod", lastPeriod)
                .putString("cycleLength", cycleLength)
                .apply();

        calculatePhase();
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
    }

    private void calculatePhase() {
        try {
            String lastPeriodStr = prefs.getString("lastPeriod", "");
            int cycleLength = Integer.parseInt(prefs.getString("cycleLength", "28"));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date lastPeriod = sdf.parse(lastPeriodStr);
            Date today = new Date();

            long diff = today.getTime() - lastPeriod.getTime();
            int daysSince = (int) (diff / (1000 * 60 * 60 * 24));
            int dayInCycle = (daysSince % cycleLength) + 1;

            mPhaseViewModel.findPhaseByDay(dayInCycle);

            dayText.setText("Day " + dayInCycle + " of " + cycleLength);

        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format. Use DD/MM/YYYY", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPlaylists(String token) {
        String phase = prefs.getString("currentPhase", "Menstruation");
        if (!phase.isEmpty()) {
            musicTitle.setText("Music for Your " + phase + " Phase");
        }
        String query = getPlaylistQuery(phase);

        String urlStr = "https://api.spotify.com/v1/search?q=" +
                Uri.encode(query) + "&type=playlist&limit=5";
        Request request = new Request.Builder()
                .url(urlStr)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error loading playlists", e);
                runOnUiThread(() -> {
                    Toast.makeText(PeriodsMainActivity.this, "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
                    statusText.setText("Failed to load playlists.");
                    playlistContainer.removeAllViews();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Error loading playlists: " + response.code() + " " + response.message());
                        if (response.code() == 401) {
                            // Token expired, clear token
                            SharedPreferences spotifyPrefs = getSharedPreferences(SpotifyLoginActivity.getPrefsName(), MODE_PRIVATE);
                            SpotifyLoginActivity.clearToken(spotifyPrefs);
                            runOnUiThread(() -> {
                                Toast.makeText(PeriodsMainActivity.this, "Spotify session expired. Please connect again.", Toast.LENGTH_SHORT).show();
                                statusText.setText("Connect Spotify to view music suggestions");
                                connectButton.setText("Connect Spotify");
                                playlistContainer.removeAllViews();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(PeriodsMainActivity.this, "Failed to load playlists.", Toast.LENGTH_SHORT).show();
                                statusText.setText("Error: Could not load playlists.");
                                playlistContainer.removeAllViews();
                            });
                        }
                        return;
                    }

                    if (responseBody == null) {
                        return;
                    }

                    String responseString = responseBody.string();
                    JSONObject json = new JSONObject(responseString);
                    JSONArray playlists = json.getJSONObject("playlists").getJSONArray("items");

                    runOnUiThread(() -> displayPlaylists(playlists));

                } catch (Exception e) {
                    Log.e(TAG, "Error processing playlist response", e);
                    runOnUiThread(() -> Toast.makeText(PeriodsMainActivity.this, "Error processing playlist data.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getPlaylistQuery(String phase) {
        switch (phase) {
            case "Menstruation":
                return "calm soothing relaxing";
            case "Follicular":
                return "energetic uplifting happy";
            case "Ovulation":
                return "confident vibrant empowering";
            case "Luteal":
                return "mellow cozy chill";
            default:
                return "relaxing";
        }
    }

    private void displayPlaylists(JSONArray playlists) {
        playlistContainer.removeAllViews();

        try {
            for (int i = 0; i < playlists.length(); i++) {
                JSONObject playlist = playlists.getJSONObject(i);
                String name = playlist.getString("name");
                String spotifyUrl = playlist.getJSONObject("external_urls").getString("spotify");
                String imageUrl = "";

                if (playlist.has("images") && playlist.getJSONArray("images").length() > 0) {
                    imageUrl = playlist.getJSONArray("images").getJSONObject(0).getString("url");
                }

                addPlaylistView(name, spotifyUrl, imageUrl);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying playlists", e);
        }
    }

    private void addPlaylistView(String name, String url, String imageUrl) {
        View playlistView = getLayoutInflater().inflate(R.layout.playlist_item, playlistContainer, false);

        TextView titleView = playlistView.findViewById(R.id.playlistTitle);
        Button playButton = playlistView.findViewById(R.id.playButton);

        titleView.setText(name);
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        playlistContainer.addView(playlistView);
    }
}
