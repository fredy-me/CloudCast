package com.busaradigital.cloudcast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WeatherHistoryActivity extends AppCompatActivity {

    private LinearLayout llHistoryContainer;
    private HistoryManager historyManager;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather_history);
        EdgeToEdge.enable(this);

        llHistoryContainer = findViewById(R.id.ll_history_container);
        android.widget.CalendarView calendarView = findViewById(R.id.calendarView);
        
        userManager = new UserManager(this);
        historyManager = new HistoryManager(this, userManager.getEmail());

        loadHistory();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(java.util.Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            fetchHistoricalWeather(selectedDate);
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void fetchHistoricalWeather(String date) {
        // We need the last known location from SharedPreferences or similar
        // For simplicity, let's use the first record in history as a reference for location
        List<WeatherRecord> history = historyManager.getHistory();
        String city = "Dar es Salaam"; // Default fallback
        double latitude = 0, longitude = 0;

        if (!history.isEmpty()) {
            latitude = history.get(0).lat;
            longitude = history.get(0).lon;
            city = history.get(0).location;
        }

        final String finalCity = city;
        final double finalLat = latitude;
        final double finalLon = longitude;

        android.widget.Toast.makeText(this, "Checking weather for " + date, android.widget.Toast.LENGTH_SHORT).show();

        new WeatherApiService().getWeatherByDate(latitude, longitude, date, new WeatherApiService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                // If the response contains the date we asked for in forecastday[0]
                if (response.getForecast() != null && !response.getForecast().forecastDay.isEmpty()) {
                    WeatherResponse.ForecastDay dayData = response.getForecast().forecastDay.get(0);
                    
                    Intent intent = new Intent(WeatherHistoryActivity.this, WeatherDetailsActivity.class);
                    intent.putExtra(MainActivity.EXTRA_LOCATION, finalCity);
                    intent.putExtra(MainActivity.EXTRA_DATE, dayData.date);
                    intent.putExtra(MainActivity.EXTRA_WEATHER_DESC, dayData.day.condition.text);
                    intent.putExtra(MainActivity.EXTRA_ICON_URL, "https:" + dayData.day.condition.icon);
                    intent.putExtra(MainActivity.EXTRA_TEMPERATURE, String.format(java.util.Locale.US, "%.0f°C", dayData.day.avgTempC));
                    intent.putExtra(MainActivity.EXTRA_FEELS_LIKE, "--");
                    
                    // Forecast data doesn't usually have "feels like" but has humidity
                    intent.putExtra(MainActivity.EXTRA_HUMIDITY, String.format(java.util.Locale.US, "%.0f%%", dayData.day.avgHumidity));
                    intent.putExtra(MainActivity.EXTRA_WIND, String.format(java.util.Locale.US, "%.1f km/h", dayData.day.maxWindKph));
                    intent.putExtra(MainActivity.EXTRA_PRECIPITATION, dayData.day.totalPrecipMm + " mm");

                    intent.putExtra(MainActivity.EXTRA_LAT, finalLat);
                    intent.putExtra(MainActivity.EXTRA_LON, finalLon);
                    startActivity(intent);
                } else {
                    android.widget.Toast.makeText(WeatherHistoryActivity.this, "No data available for this date", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                android.widget.Toast.makeText(WeatherHistoryActivity.this, "Error: " + error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHistory() {
        llHistoryContainer.removeAllViews();
        List<WeatherRecord> history = historyManager.getHistory();

        if (history.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText(R.string.empty_history);
            emptyText.setTextColor(getColor(R.color.text_secondary));
            emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            emptyText.setPadding(0, 50, 0, 0);
            llHistoryContainer.addView(emptyText);
            return;
        }

        for (WeatherRecord record : history) {
            addHistoryCard(record);
        }
    }

    private void addHistoryCard(WeatherRecord record) {
        View cardView = getLayoutInflater().inflate(R.layout.item_history_card, llHistoryContainer, false);

        ImageView ivIcon = cardView.findViewById(R.id.iv_history_icon);
        TextView tvDesc = cardView.findViewById(R.id.tv_history_desc);
        TextView tvLocation = cardView.findViewById(R.id.tv_history_location);
        TextView tvTime = cardView.findViewById(R.id.tv_history_time);

        ImageLoader.load(record.iconUrl, ivIcon);
        tvDesc.setText(record.description);
        tvLocation.setText(record.location);
        tvTime.setText(record.date);

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(WeatherHistoryActivity.this, WeatherDetailsActivity.class);
            intent.putExtra(MainActivity.EXTRA_ICON_URL, record.iconUrl);
            intent.putExtra(MainActivity.EXTRA_WEATHER_DESC, record.description);
            intent.putExtra(MainActivity.EXTRA_LOCATION, record.location);
            intent.putExtra(MainActivity.EXTRA_TEMPERATURE, record.temperature);
            intent.putExtra(MainActivity.EXTRA_FEELS_LIKE, record.feelsLike);
            intent.putExtra(MainActivity.EXTRA_HUMIDITY, record.humidity);
            intent.putExtra(MainActivity.EXTRA_WIND, record.wind);
            intent.putExtra(MainActivity.EXTRA_PRECIPITATION, record.precipitation);
            intent.putExtra(MainActivity.EXTRA_DATE, record.date);
            intent.putExtra(MainActivity.EXTRA_LAT, record.lat);
            intent.putExtra(MainActivity.EXTRA_LON, record.lon);
            startActivity(intent);
        });

        llHistoryContainer.addView(cardView);
    }
}