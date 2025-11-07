package com.unified.healthfitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.unified.healthfitness.models.SpotifyModels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Playlist activity with integrated API client and RecyclerView adapter
 * Shows workout playlists from Spotify
 */
public class PlaylistActivity extends AppCompatActivity {

    private static final String TAG = "PlaylistActivity";
    private static final String API_BASE_URL = "https://api.spotify.com/v1/";

    // UI Components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvError;
    private Toolbar toolbar;

    // Data
    private PlaylistAdapter adapter;
    private List<SpotifyModels.Playlist> playlists;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get access token from Intent first
        if (getIntent().hasExtra("ACCESS_TOKEN")) {
            accessToken = getIntent().getStringExtra("ACCESS_TOKEN");
        } else {
            // Fallback to SharedPreferences
            SharedPreferences prefs = getSharedPreferences(
                    SpotifyLoginActivity.getPrefsName(), MODE_PRIVATE);
            accessToken = SpotifyLoginActivity.getAccessToken(prefs);
        }

        if (accessToken == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewPlaylists);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvError = findViewById(R.id.tvError);

        // Setup RecyclerView
        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(playlists);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load playlists
        loadPlaylists();
    }

    /**
     * Loads workout playlists from Spotify API
     */
    private void loadPlaylists() {
        showLoading(true);

        // Create Retrofit API service
        SpotifyApiService apiService = createApiService();

        // Simpler search query that's less likely to be restricted
        String query = "workout";
        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "Searching playlists with query: " + query);

        apiService.searchPlaylists(authHeader, query, "playlist", 20)
                .enqueue(new Callback<SpotifyModels.SearchResponse>() {
                    @Override
                    public void onResponse(Call<SpotifyModels.SearchResponse> call,
                                           Response<SpotifyModels.SearchResponse> response) {
                        showLoading(false);

                        Log.d(TAG, "Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            SpotifyModels.SearchResponse searchResponse = response.body();

                            if (searchResponse.playlists != null &&
                                    searchResponse.playlists.items != null) {

                                List<SpotifyModels.Playlist> results =
                                        searchResponse.playlists.items;

                                Log.d(TAG, "Found " + results.size() + " playlists");

                                if (results.isEmpty()) {
                                    showEmpty();
                                } else {
                                    playlists.clear();
                                    playlists.addAll(results);
                                    adapter.notifyDataSetChanged();
                                    showContent();
                                }
                            } else {
                                showEmpty();
                            }
                        } else {
                            // Log the error response body for debugging
                            try {
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "No error body";
                                Log.e(TAG, "Error response: " + errorBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to read error body", e);
                            }
                            handleError(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SpotifyModels.SearchResponse> call, Throwable t) {
                        showLoading(false);
                        showError("Network error: " + t.getMessage());
                        Log.e(TAG, "API call failed", t);
                    }
                });
    }

    /**
     * Creates Retrofit API service
     */
    private SpotifyApiService createApiService() {
        // Logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(SpotifyApiService.class);
    }

    /**
     * Handles API errors
     */
    private void handleError(int statusCode) {
        Log.e(TAG, "API Error - Status Code: " + statusCode);

        if (statusCode == 401) {
            // Unauthorized - token invalid or expired
            Toast.makeText(this, "Session expired. Please log in again.",
                    Toast.LENGTH_LONG).show();
            logout();
        } else if (statusCode == 403) {
            // Forbidden - permissions issue
            String message = "Access denied. Possible reasons:\n" +
                    "1. App not properly configured in Spotify Dashboard\n" +
                    "2. User not whitelisted in Development Mode\n" +
                    "3. Missing required scopes";
            Log.e(TAG, message);
            showError("Access Denied (403)\n\nPlease check:\n" +
                    "• Spotify Dashboard settings\n" +
                    "• Redirect URI is saved\n" +
                    "• User email is whitelisted");
        } else {
            showError("Error loading playlists (Code: " + statusCode + ")");
        }
    }

    /**
     * UI State Management
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
    }

    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }

    /**
     * Logout and clear token
     */
    private void logout() {
        SharedPreferences prefs = getSharedPreferences(
                SpotifyLoginActivity.getPrefsName(), MODE_PRIVATE);
        SpotifyLoginActivity.clearToken(prefs);
        goToLogin();
    }

    /**
     * Navigate to login
     */
    private void goToLogin() {
        Intent intent = new Intent(this, SpotifyLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_refresh) {
            loadPlaylists();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ===== RETROFIT API INTERFACE =====
    interface SpotifyApiService {
        @GET("search")
        Call<SpotifyModels.SearchResponse> searchPlaylists(
                @Header("Authorization") String authorization,
                @Query("q") String query,
                @Query("type") String type,
                @Query("limit") int limit
        );

        // Alternative: Get user's own playlists (more reliable in dev mode)
        @GET("me/playlists")
        Call<SpotifyModels.PlaylistsWrapper> getUserPlaylists(
                @Header("Authorization") String authorization,
                @Query("limit") int limit
        );
    }

    // ===== RECYCLERVIEW ADAPTER =====
    class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

        private final List<SpotifyModels.Playlist> items;

        PlaylistAdapter(List<SpotifyModels.Playlist> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_playlist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            ImageView ivCover;
            TextView tvName, tvDescription, tvTrackCount;

            ViewHolder(View view) {
                super(view);
                cardView = view.findViewById(R.id.cardView);
                ivCover = view.findViewById(R.id.ivPlaylistCover);
                tvName = view.findViewById(R.id.tvPlaylistName);
                tvDescription = view.findViewById(R.id.tvPlaylistDescription);
                tvTrackCount = view.findViewById(R.id.tvTrackCount);
            }

            void bind(SpotifyModels.Playlist playlist) {
                if (playlist == null) {
                    tvName.setText("Unknown Playlist");
                    tvDescription.setText("");
                    return;
                }

                if (playlist.name != null)
                    tvName.setText(playlist.name);
                else
                    tvName.setText("Untitled Playlist");

                if (playlist.description != null && !playlist.description.isEmpty()) {
                    tvDescription.setText(playlist.description);
                    tvDescription.setVisibility(View.VISIBLE);
                } else {
                    tvDescription.setVisibility(View.GONE);
                }

                // Set track count
                tvTrackCount.setText(playlist.getTotalTracks() + " tracks");

                // Load image
                String imageUrl = playlist.getImageUrl();
                if (imageUrl != null) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(R.drawable.ic_music_placeholder)
                            .error(R.drawable.ic_music_placeholder)
                            .into(ivCover);
                } else {
                    ivCover.setImageResource(R.drawable.ic_music_placeholder);
                }

                // Click listener - open in Spotify
                cardView.setOnClickListener(v -> {
                    String url = playlist.getSpotifyUrl();
                    if (url != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setPackage("com.spotify.music");

                        try {
                            itemView.getContext().startActivity(intent);
                        } catch (Exception e) {
                            // Spotify app not installed, open in browser
                            intent.setPackage(null);
                            itemView.getContext().startActivity(intent);
                        }
                    }
                });
            }
        }
    }
}
