package com.busaradigital.cloudcast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class WeatherDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather_details);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ImageView ivIcon = findViewById(R.id.iv_details_icon);
        TextView tvDesc = findViewById(R.id.tv_details_desc);
        TextView tvDayName = findViewById(R.id.tv_details_day_name);
        TextView tvLocation = findViewById(R.id.tv_details_location);
        TextView tvDate = findViewById(R.id.tv_details_date);
        TextView tvTemp = findViewById(R.id.tv_details_temp);
        TextView tvFeelsLike = findViewById(R.id.tv_details_feels_like);
        TextView tvHumidity = findViewById(R.id.tv_details_humidity);
        TextView tvWind = findViewById(R.id.tv_details_wind);
        TextView tvPrecipitation = findViewById(R.id.tv_details_precipitation);
        TextView tvCoordinates = findViewById(R.id.tv_details_coordinates);

        Intent intent = getIntent();
        if (intent != null) {
            String iconUrl = intent.getStringExtra(MainActivity.EXTRA_ICON_URL);
            String desc = intent.getStringExtra(MainActivity.EXTRA_WEATHER_DESC);
            String location = intent.getStringExtra(MainActivity.EXTRA_LOCATION);
            String date = intent.getStringExtra(MainActivity.EXTRA_DATE);
            String temp = intent.getStringExtra(MainActivity.EXTRA_TEMPERATURE);
            String feelsLike = intent.getStringExtra(MainActivity.EXTRA_FEELS_LIKE);
            String humidity = intent.getStringExtra(MainActivity.EXTRA_HUMIDITY);
            String wind = intent.getStringExtra(MainActivity.EXTRA_WIND);
            String precipitation = intent.getStringExtra(MainActivity.EXTRA_PRECIPITATION);
            double lat = intent.getDoubleExtra(MainActivity.EXTRA_LAT, 0);
            double lon = intent.getDoubleExtra(MainActivity.EXTRA_LON, 0);

            if (iconUrl != null) {
                ImageLoader.load(iconUrl, ivIcon);
            }
            if (desc != null) tvDesc.setText(desc);
            if (location != null) tvLocation.setText(location);
            if (date != null) {
                tvDate.setText(date);
                try {
                    // Try to parse the date to get the day name
                    // Dates can be "2024-10-21" or "21 October 2024 . 03:27 PM"
                    java.util.Date parsedDate;
                    if (date.contains(".")) {
                        parsedDate = new java.text.SimpleDateFormat("d MMMM yyyy . hh:mm a", Locale.US).parse(date);
                    } else {
                        parsedDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
                    }
                    if (parsedDate != null) {
                        String dayName = new java.text.SimpleDateFormat("EEEE", Locale.US).format(parsedDate);
                        tvDayName.setText(dayName);
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

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}
