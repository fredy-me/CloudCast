package com.busaradigital.cloudcast;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final String PREF_NAME = "weather_history";
    private static final String KEY_HISTORY = "history_list";
    private final SharedPreferences prefs;
    private final Gson gson;

    public HistoryManager(Context context, String userEmail) {
        String name = PREF_NAME;
        if (userEmail != null && !userEmail.isEmpty()) {
            name += "_" + userEmail.replace(".", "_");
        }
        prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveRecord(WeatherRecord record) {
        List<WeatherRecord> history = getHistory();
        // Check if we already saved this location for this hour/day to avoid duplicates
        if (!history.isEmpty()) {
            WeatherRecord last = history.get(0);
            if (last.location.equals(record.location) && last.date.equals(record.date)) {
                return;
            }
        }
        
        history.add(0, record); // Add to beginning
        if (history.size() > 20) {
            history = history.subList(0, 20); // Keep last 20
        }
        
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply();
    }

    public List<WeatherRecord> getHistory() {
        String json = prefs.getString(KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<WeatherRecord>>(){}.getType());
    }
}