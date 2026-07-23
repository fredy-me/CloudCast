package com.busaradigital.cloudcast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherDetailsActivity extends AppCompatActivity {

    private ImageView ivIcon;
    private TextView tvDesc, tvDayName, tvLocation, tvDate, tvTemp, tvFeelsLike, tvHumidity, tvWind, tvPrecipitation, tvCoordinates;
    private double currentLat, currentLon;
    private String currentDate;
    private WeatherApiService weatherApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ivIcon = findViewById(R.id.iv_details_icon);
        tvDesc = findViewById(R.id.tv_details_desc);
        tvDayName = findViewById(R.id.tv_details_day_name);
        tvLocation = findViewById(R.id.tv_details_location);
        tvDate = findViewById(R.id.tv_details_date);
        tvTemp = findViewById(R.id.tv_details_temp);
        tvFeelsLike = findViewById(R.id.tv_details_feels_like);
        tvHumidity = findViewById(R.id.tv_details_humidity);
        tvWind = findViewById(R.id.tv_details_wind);
        tvPrecipitation = findViewById(R.id.tv_details_precipitation);
        tvCoordinates = findViewById(R.id.tv_details_coordinates);

        weatherApiService = new WeatherApiService();

        Intent intent = getIntent();
        if (intent != null) {
            updateUIFromIntent(intent);
        }

        findViewById(R.id.btn_refresh).setOnClickListener(v -> refreshWeather());
        
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void updateUIFromIntent(Intent intent) {
        String iconUrl = intent.getStringExtra(MainActivity.EXTRA_ICON_URL);
        String desc = intent.getStringExtra(MainActivity.EXTRA_WEATHER_DESC);
        String location = intent.getStringExtra(MainActivity.EXTRA_LOCATION);
        currentDate = intent.getStringExtra(MainActivity.EXTRA_DATE);
        String temp = intent.getStringExtra(MainActivity.EXTRA_TEMPERATURE);
        String feelsLike = intent.getStringExtra(MainActivity.EXTRA_FEELS_LIKE);
        String humidity = intent.getStringExtra(MainActivity.EXTRA_HUMIDITY);
        String wind = intent.getStringExtra(MainActivity.EXTRA_WIND);
        String precipitation = intent.getStringExtra(MainActivity.EXTRA_PRECIPITATION);
        currentLat = intent.getDoubleExtra(MainActivity.EXTRA_LAT, 0);
        currentLon = intent.getDoubleExtra(MainActivity.EXTRA_LON, 0);

        displayWeatherData(iconUrl, desc, location, currentDate, temp, feelsLike, humidity, wind, precipitation, currentLat, currentLon);
    }

    private void displayWeatherData(String iconUrl, String desc, String location, String date, 
                                    String temp, String feelsLike, String humidity, String wind, 
                                    String precipitation, double lat, double lon) {
        if (iconUrl != null) ImageLoader.load(iconUrl, ivIcon);
        if (desc != null) tvDesc.setText(desc);
        if (location != null) tvLocation.setText(location);
        if (date != null) {
            tvDate.setText(date);
            try {
                Date parsedDate;
                if (date.contains(".")) {
                    parsedDate = new SimpleDateFormat("d MMMM yyyy . hh:mm a", Locale.US).parse(date);
                } else {
                    parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
                }
                if (parsedDate != null) {
                    tvDayName.setText(new SimpleDateFormat("EEEE", Locale.US).format(parsedDate));
                    tvDayName.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                tvDayName.setVisibility(View.GONE);
            }
        }
        if (temp != null) tvTemp.setText(temp);
        if (feelsLike != null) tvFeelsLike.setText(feelsLike);
        if (humidity != null) tvHumidity.setText(humidity);
        if (wind != null) tvWind.setText(wind);
        if (precipitation != null) tvPrecipitation.setText(precipitation);

        if (lat != 0 || lon != 0) {
            tvCoordinates.setText(String.format(Locale.US, "%.4f, %.4f", lat, lon));
        }
    }

    private void refreshWeather() {
        if (currentLat == 0 && currentLon == 0) {
            Toast.makeText(this, "Coordinates unavailable for refresh", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Refreshing details...", Toast.LENGTH_SHORT).show();
        
        WeatherApiService.WeatherCallback callback = new WeatherApiService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                // If we were looking at a specific date, we look for that date in forecast
                if (currentDate != null && !currentDate.contains(".")) {
                    if (response.getForecast() != null && !response.getForecast().forecastDay.isEmpty()) {
                        for (WeatherResponse.ForecastDay day : response.getForecast().forecastDay) {
                            if (day.date.equals(currentDate)) {
                                displayWeatherData(
                                    day.day.condition.icon,
                                    day.day.condition.text,
                                    tvLocation.getText().toString(),
                                    day.date,
                                    String.format(Locale.US, "%.0f°C", day.day.avgTempC),
                                    "--",
                                    String.format(Locale.US, "%.0f%%", day.day.avgHumidity),
                                    String.format(Locale.US, "%.1f km/h", day.day.maxWindKph),
                                    day.day.totalPrecipMm + " mm",
                                    currentLat, currentLon
                                );
                                return;
                            }
                        }
                    }
                }
                
                // Otherwise refresh with current weather
                displayWeatherData(
                    response.getWeatherIconUrl(),
                    response.getWeatherDescription(),
                    response.getCityWithCountry(),
                    new SimpleDateFormat("d MMMM yyyy . hh:mm a", Locale.US).format(new Date()),
                    response.getTemperature(),
                    "Feels like " + response.getFeelsLike(),
                    response.getHumidity(),
                    response.getWindSpeed(),
                    response.getPrecipitation(),
                    currentLat, currentLon
                );
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WeatherDetailsActivity.this, "Refresh failed: " + error, Toast.LENGTH_SHORT).show();
            }
        };

        if (currentDate != null && !currentDate.contains(".")) {
            weatherApiService.getWeatherByDate(currentLat, currentLon, currentDate, callback);
        } else {
            weatherApiService.getWeatherByCoordinates(currentLat, currentLon, callback);
        }
    }
}
