package com.unified.healthfitness;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface PexelsService {
    @GET("videos/search")
    Call<PexelsResponse> searchVideos(
            @Header("Authorization") String apiKey,
            @Query("query") String query,
            @Query("per_page") int perPage
    );
}