package com.unified.healthfitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Login activity using PKCE flow (works for all users, no whitelist needed!)
 */
public class SpotifyLoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // ===== SPOTIFY CONFIGURATION =====
    private static final String CLIENT_ID = "";//input your id
    private static final String REDIRECT_URI = "com.unified.healthfitness://callback";
    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    // Scopes
    private static final String SCOPES =
            "user-read-private user-read-email playlist-read-private playlist-read-collaborative streaming";

    // SharedPreferences keys
    private static final String PREFS_NAME = "SpotifyPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    private static final String KEY_CODE_VERIFIER = "code_verifier";

    // UI Components
    private Button btnLogin;
    private ProgressBar progressBar;
    private SharedPreferences prefs;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_spotify);

        // Initialize
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        httpClient = new OkHttpClient.Builder()
                .dns(new ForceIPv4Dns())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize views
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Handle callback
        handleSpotifyCallback(getIntent());

        // Check if already logged in
        if (isTokenValid()) {
            Log.d(TAG, "Valid token found");
            navigateToPlaylists(prefs.getString(KEY_ACCESS_TOKEN, null));
            return;
        }

        // Login button
        btnLogin.setOnClickListener(v -> authenticateWithSpotify());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSpotifyCallback(intent);
    }

    /**
     * Handles callback from Spotify
     */
    private void handleSpotifyCallback(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri uri = intent.getData();

            if (uri.toString().startsWith(REDIRECT_URI)) {
                String code = uri.getQueryParameter("code");
                String error = uri.getQueryParameter("error");

                if (error != null) {
                    Log.e(TAG, "Auth error: " + error);
                    Toast.makeText(this, "Login cancelled or failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (code != null) {
                    Log.d(TAG, "Authorization code received");
                    exchangeCodeForToken(code);
                }
            }
        }
    }

    /**
     * Start authentication with PKCE
     */
    private void authenticateWithSpotify() {
        Log.d(TAG, "Starting PKCE authentication");

        try {
            // Generate code verifier and challenge
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);

            // Save verifier for later
            prefs.edit().putString(KEY_CODE_VERIFIER, codeVerifier).apply();

            // Build authorization URL
            Uri authUri = Uri.parse(AUTH_URL).buildUpon()
                    .appendQueryParameter("client_id", CLIENT_ID)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("redirect_uri", REDIRECT_URI)
                    .appendQueryParameter("scope", SCOPES)
                    .appendQueryParameter("code_challenge_method", "S256")
                    .appendQueryParameter("code_challenge", codeChallenge)
                    .build();

            // Launch Chrome Custom Tab
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build();

            customTabsIntent.launchUrl(this, authUri);
            Log.d(TAG, "Launched authentication");

        } catch (Exception e) {
            Log.e(TAG, "Authentication failed", e);
            Toast.makeText(this, "Failed to start login: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Exchange authorization code for access token
     */
    private void exchangeCodeForToken(String code) {
        showLoading(true);

        String codeVerifier = prefs.getString(KEY_CODE_VERIFIER, null);
        if (codeVerifier == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        // Build request body
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .add("code_verifier", codeVerifier)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "Token exchange failed", e);
                    Toast.makeText(SpotifyLoginActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                runOnUiThread(() -> {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String accessToken = json.getString("access_token");
                            String refreshToken = json.optString("refresh_token", null);
                            int expiresIn = json.getInt("expires_in");

                            saveTokens(accessToken, refreshToken, expiresIn);
                            Log.d(TAG, "Token exchange successful");
                            Toast.makeText(SpotifyLoginActivity.this,
                                    "Login successful!", Toast.LENGTH_SHORT).show();
                            navigateToPlaylists(accessToken);

                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse token response", e);
                            Toast.makeText(SpotifyLoginActivity.this,
                                    "Login failed: Invalid response", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Token exchange failed: " + response.code() + " - " + responseBody);
                        Toast.makeText(SpotifyLoginActivity.this,
                                "Login failed: " + response.message(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * Generate random code verifier
     */
    private String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);

        return android.util.Base64.encodeToString(bytes,
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING);
    }

    /**
     * Generate code challenge from verifier
     */
    private String generateCodeChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));

        return android.util.Base64.encodeToString(hash,
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING);
    }

    /**
     * Save tokens to SharedPreferences
     */
    private void saveTokens(String accessToken, String refreshToken, int expiresIn) {
        long expiryTime = System.currentTimeMillis() + (expiresIn * 1000L);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }
        editor.putLong(KEY_TOKEN_EXPIRY, expiryTime);
        editor.apply();

        Log.d(TAG, "Tokens saved");
    }

    /**
     * Check if token is valid
     */
    private boolean isTokenValid() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);
        long expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0);

        return token != null && System.currentTimeMillis() < expiryTime;
    }

    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        btnLogin.setEnabled(!show);
    }

    /**
     * Navigate to playlists
     */
    private void navigateToPlaylists(String accessToken) {
        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra("ACCESS_TOKEN", accessToken);
        startActivity(intent);
        finish();
    }

    // ===== PUBLIC STATIC METHODS =====

    public static String getAccessToken(SharedPreferences prefs) {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public static void clearToken(SharedPreferences prefs) {
        prefs.edit().clear().apply();
    }

    public static String getPrefsName() {
        return PREFS_NAME;
    }
}
