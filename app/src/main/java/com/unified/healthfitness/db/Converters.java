package com.unified.healthfitness.db;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Converters {
    @TypeConverter
    public static HashMap<Integer, Integer> fromString(String value) {
        Type mapType = new TypeToken<HashMap<Integer, Integer>>() {}.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromHashMap(HashMap<Integer, Integer> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }
}
