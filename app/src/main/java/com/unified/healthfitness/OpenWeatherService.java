package com.unified.healthfitness;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherService {
    // We'll request metric units for human-friendly Celsius
    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeather(@Query("q") String city,
                                     @Query("appid") String apiKey,
                                     @Query("units") String units);
}
