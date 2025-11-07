package com.unified.healthfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.unified.healthfitness.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WeatherWorkoutMainActivity extends AppCompatActivity {

    private EditText editTextCity;
    private Button buttonFetch;
    private ImageView imageViewUnsplash;
    private TextView textViewSuggestion, textViewWeather;
    private LinearLayout workoutButtonsLayout;
    private StyledPlayerView playerView;
    private ExoPlayer player;

    private Handler slideshowHandler = new Handler();
    private int currentImageIndex = 0;
    private String[] currentImageUrls;
    private Runnable slideshowRunnable;

    private Handler videoHandler = new Handler();
    private int currentVideoIndex = 0;
    private String[] currentVideoUrls;
    private boolean isVideoRotating = false;

    private String currentWeatherMain = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_workout);

        // Views
        editTextCity = findViewById(R.id.editTextCity);
        buttonFetch = findViewById(R.id.buttonFetch);
        imageViewUnsplash = findViewById(R.id.imageViewUnsplash);
        textViewSuggestion = findViewById(R.id.textViewSuggestion);
        textViewWeather = findViewById(R.id.textViewWeather);
        workoutButtonsLayout = findViewById(R.id.workoutButtonsLayout);
        playerView = findViewById(R.id.playerView);

        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        Log.d("APP", "WeatherWorkoutActivity started");

        // default sample fetch (optional)
        String defaultCity = "London";
        editTextCity.setText(defaultCity);

        // button triggers weather fetch + suggestions
        buttonFetch.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherAndUpdate(city);
                // hide keyboard action done
                editTextCity.onEditorAction(EditorInfo.IME_ACTION_DONE);
            } else {
                textViewSuggestion.setText("Please enter a city.");
            }
        });

        // initial load
        fetchWeatherAndUpdate(defaultCity);
    }

    private void fetchWeatherAndUpdate(String city) {
        Log.d("WEATHER", "fetchWeather for " + city);

        Retrofit retrofit = ApiClient.getClient("https://api.openweathermap.org/");
        OpenWeatherService service = retrofit.create(OpenWeatherService.class);

        service.getWeather(city, Keys.OPENWEATHER_API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.body() != null && response.body().weather != null && response.body().weather.length > 0) {
                            WeatherResponse.Weather w = response.body().weather[0];
                            float temp = response.body().main != null ? response.body().main.temp : Float.NaN;
                            currentWeatherMain = w.main;

                            textViewWeather.setText(String.format("%s — %.1f°C", w.main, temp));
                            Log.d("WEATHER", "Main: " + w.main + " desc: " + w.description + " temp: " + temp);

                            // Decide workout context and display buttons
                            workoutButtonsLayout.removeAllViews();

                            if (w.main.equalsIgnoreCase("Rain") || w.main.equalsIgnoreCase("Drizzle") || w.main.equalsIgnoreCase("Thunderstorm")) {
                                textViewSuggestion.setText("Weather: Rainy — Indoor exercises recommended");
                                addWorkoutButton("Yoga", "yoga workout");
                                addWorkoutButton("Bodyweight Circuit", "bodyweight workout");
                                addWorkoutButton("Pilates", "pilates workout");
                                loadUnsplashForWeather("rainy weather");
                            } else if (w.main.equalsIgnoreCase("Clear")) {
                                textViewSuggestion.setText("Weather: Clear — Outdoor exercises recommended");
                                addWorkoutButton("Running", "running workout");
                                addWorkoutButton("Park Workout", "park workout");
                                addWorkoutButton("Cycling", "cycling workout");
                                loadUnsplashForWeather("sunny beach");
                            } else if (w.main.equalsIgnoreCase("Snow")) {
                                textViewSuggestion.setText("Weather: Snow — Indoor exercises recommended");
                                addWorkoutButton("Home Cardio", "cardio workout");
                                addWorkoutButton("Mobility", "mobility workout");
                                addWorkoutButton("Stretching", "stretching workout");
                                loadUnsplashForWeather("snow winter");
                            } else {
                                textViewSuggestion.setText("Weather: " + w.main + " — Flexible workouts");
                                addWorkoutButton("General Fitness", "fitness workout");
                                addWorkoutButton("Strength Training", "strength workout");
                                addWorkoutButton("HIIT", "hiit workout");
                                loadUnsplashForWeather("cloudy weather");
                            }
                        } else {
                            Log.d("WEATHER", "Weather response empty or malformed");
                            textViewWeather.setText("Weather: (no data)");
                            textViewSuggestion.setText("Couldn't fetch weather. Try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Log.d("WEATHER", "API failed: " + t.getMessage());
                        textViewWeather.setText("Weather: (error)");
                        textViewSuggestion.setText("Error fetching weather: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

    private void addWorkoutButton(String buttonText, String videoQuery) {
        Button btn = new Button(this);
        btn.setText(buttonText);
        btn.setAllCaps(false);
        btn.setTextColor(0xFFFFFFFF); // White text
        btn.setBackgroundResource(R.drawable.button_rounded);
        btn.setTextSize(16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        btn.setLayoutParams(params);
        btn.setPadding(32, 32, 32, 32);

        btn.setOnClickListener(v -> {
            loadPexelsVideoForQuery(videoQuery);
        });

        workoutButtonsLayout.addView(btn);
    }

    private void loadPexelsVideoForQuery(String query) {
        Log.d("PEXELS", "loadPexelsVideo for query: " + query);

        // Stop any existing video rotation
        isVideoRotating = false;
        if (videoHandler != null) {
            videoHandler.removeCallbacksAndMessages(null);
        }

        // Stop current video
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }

        Retrofit r = ApiClient.getClient("https://api.pexels.com/");
        PexelsService p = r.create(PexelsService.class);

        p.searchVideos(Keys.PEXELS_API_KEY, query, 5)
                .enqueue(new Callback<PexelsResponse>() {
                    @Override
                    public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                        Log.d("PEXELS", "Response received");
                        if (response.body() != null && response.body().videos != null && response.body().videos.length > 0) {
                            Log.d("PEXELS", "Videos found: " + response.body().videos.length);

                            // Collect all valid video URLs
                            java.util.ArrayList<String> videoList = new java.util.ArrayList<>();

                            for (int i = 0; i < response.body().videos.length && videoList.size() < 5; i++) {
                                PexelsResponse.PexelsVideo video = response.body().videos[i];
                                String videoUrl = null;

                                if (video.video_files != null && video.video_files.length > 0) {
                                    // Try to find SD quality first (better loading)
                                    for (PexelsResponse.VideoFile file : video.video_files) {
                                        if (file.quality != null && file.link != null) {
                                            if (file.quality.equals("sd") && file.width <= 640) {
                                                videoUrl = file.link;
                                                break;
                                            }
                                        }
                                    }

                                    // Try HD if SD not found
                                    if (videoUrl == null) {
                                        for (PexelsResponse.VideoFile file : video.video_files) {
                                            if (file.quality != null && file.quality.equals("hd") && file.link != null) {
                                                videoUrl = file.link;
                                                break;
                                            }
                                        }
                                    }

                                    // Fallback to first available
                                    if (videoUrl == null && video.video_files[0].link != null) {
                                        videoUrl = video.video_files[0].link;
                                    }
                                }

                                if (videoUrl != null) {
                                    videoList.add(videoUrl);
                                    Log.d("PEXELS", "Added video URL: " + videoUrl);
                                }
                            }

                            if (videoList.size() > 0) {
                                currentVideoUrls = videoList.toArray(new String[0]);
                                currentVideoIndex = 0;
                                isVideoRotating = true;

                                Log.d("PEXELS", "Starting video playback with " + currentVideoUrls.length + " videos");

                                // Start playing first video
                                playCurrentVideo();
                            } else {
                                Log.d("PEXELS", "No valid video URLs found");
                                playerView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("PEXELS", "No video result in response");
                            playerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<PexelsResponse> call, Throwable t) {
                        Log.e("PEXELS", "API failed: " + t.getMessage());
                        playerView.setVisibility(View.GONE);
                        t.printStackTrace();
                    }
                });
    }

    private void playCurrentVideo() {
        if (!isVideoRotating || currentVideoUrls == null || currentVideoUrls.length == 0) {
            return;
        }

        String videoUrl = currentVideoUrls[currentVideoIndex];
        Log.d("PEXELS", "Playing video " + currentVideoIndex + ": " + videoUrl);

        try {
            playerView.setVisibility(View.VISIBLE);

            // Create MediaItem from URL
            MediaItem mediaItem = MediaItem.fromUri(videoUrl);

            // Set up player
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);

            // Add listener for when video ends
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        Log.d("PEXELS", "Video completed");

                        // Remove this listener to avoid duplicates
                        player.removeListener(this);

                        if (isVideoRotating && currentVideoUrls != null) {
                            currentVideoIndex = (currentVideoIndex + 1) % currentVideoUrls.length;
                            videoHandler.postDelayed(() -> playCurrentVideo(), 500);
                        }
                    } else if (playbackState == Player.STATE_READY) {
                        Log.d("PEXELS", "Video ready to play");
                    }
                }
            });

            Log.d("PEXELS", "ExoPlayer setup complete");

        } catch (Exception e) {
            Log.e("PEXELS", "Exception playing video: " + e.getMessage());
            e.printStackTrace();

            // Try next video on exception
            if (isVideoRotating && currentVideoUrls != null && currentVideoUrls.length > 1) {
                currentVideoIndex = (currentVideoIndex + 1) % currentVideoUrls.length;
                videoHandler.postDelayed(() -> playCurrentVideo(), 1000);
            }
        }
    }

    private void loadUnsplashForWeather(String query) {
        Log.d("UNSPLASH", "loadUnsplash for query: " + query);

        // Stop any existing slideshow
        if (slideshowRunnable != null) {
            slideshowHandler.removeCallbacks(slideshowRunnable);
        }

        Retrofit r = ApiClient.getClient("https://api.unsplash.com/");
        UnsplashService s = r.create(UnsplashService.class);

        s.searchPhotos(Keys.UNSPLASH_API_KEY, query, 10)
                .enqueue(new Callback<UnsplashResponse>() {
                    @Override
                    public void onResponse(Call<UnsplashResponse> call, Response<UnsplashResponse> response) {
                        if (response.body() != null && response.body().results != null && response.body().results.length > 0) {
                            // Store all image URLs
                            currentImageUrls = new String[response.body().results.length];
                            for (int i = 0; i < response.body().results.length; i++) {
                                currentImageUrls[i] = response.body().results[i].urls.regular;
                            }

                            currentImageIndex = 0;

                            // Start slideshow
                            slideshowRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (currentImageUrls != null && currentImageUrls.length > 0) {
                                        String url = currentImageUrls[currentImageIndex];
                                        Log.d("UNSPLASH", "Slideshow image: " + url);
                                        Glide.with(WeatherWorkoutMainActivity.this)
                                                .load(url)
                                                .centerCrop()
                                                .into(imageViewUnsplash);

                                        // Move to next image
                                        currentImageIndex = (currentImageIndex + 1) % currentImageUrls.length;

                                        // Schedule next image change in 5 seconds
                                        slideshowHandler.postDelayed(this, 5000);
                                    }
                                }
                            };

                            // Start immediately
                            slideshowRunnable.run();
                        } else {
                            Log.d("UNSPLASH", "No Unsplash result; clearing view");
                            imageViewUnsplash.setImageDrawable(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<UnsplashResponse> call, Throwable t) {
                        Log.d("UNSPLASH", "API failed: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up the handlers to prevent memory leaks
        if (slideshowRunnable != null) {
            slideshowHandler.removeCallbacks(slideshowRunnable);
        }

        isVideoRotating = false;
        if (videoHandler != null) {
            videoHandler.removeCallbacksAndMessages(null);
        }

        // Release ExoPlayer
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when app goes to background
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video when app comes to foreground
        if (player != null && playerView.getVisibility() == View.VISIBLE) {
            player.play();
        }
    }
}