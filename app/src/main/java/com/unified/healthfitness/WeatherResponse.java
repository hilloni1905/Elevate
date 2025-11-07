package com.unified.healthfitness;

public class WeatherResponse {
    public Main main;
    public Weather[] weather;

    public static class Main {
        public float temp;
    }

    public static class Weather {
        public String main;       // e.g., "Rain", "Clear", "Clouds"
        public String description;
    }
}
