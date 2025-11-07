package com.unified.healthfitness.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * All Spotify API models in one file for simplicity
 */

// ===== SEARCH RESPONSE =====
public class SpotifyModels {

    public static class SearchResponse {
        @SerializedName("playlists")
        public PlaylistsWrapper playlists;
    }

    public static class PlaylistsWrapper {
        @SerializedName("items")
        public List<Playlist> items;

        @SerializedName("total")
        public int total;
    }

    // ===== PLAYLIST MODEL =====
    public static class Playlist {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("images")
        public List<Image> images;

        @SerializedName("external_urls")
        public ExternalUrls externalUrls;

        @SerializedName("tracks")
        public TracksInfo tracks;

        // Helper methods
        public String getImageUrl() {
            return (images != null && !images.isEmpty()) ? images.get(0).url : null;
        }

        public String getSpotifyUrl() {
            return (externalUrls != null) ? externalUrls.spotify : null;
        }

        public int getTotalTracks() {
            return (tracks != null) ? tracks.total : 0;
        }
    }

    // ===== SUPPORTING MODELS =====
    public static class Image {
        @SerializedName("url")
        public String url;

        @SerializedName("height")
        public Integer height;

        @SerializedName("width")
        public Integer width;
    }

    public static class ExternalUrls {
        @SerializedName("spotify")
        public String spotify;
    }

    public static class TracksInfo {
        @SerializedName("total")
        public int total;
    }
}