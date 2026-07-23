package com.busaradigital.cloudcast;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationHelper.LocationResultListener {

    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_TEMPERATURE = "extra_temperature";
    public static final String EXTRA_FEELS_LIKE = "extra_feels_like";
    public static final String EXTRA_WEATHER_DESC = "extra_weather_desc";
    public static final String EXTRA_ICON_URL = "extra_icon_url";
    public static final String EXTRA_HUMIDITY = "extra_humidity";
    public static final String EXTRA_WIND = "extra_wind";
    public static final String EXTRA_PRECIPITATION = "extra_precipitation";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LON = "extra_lon";

    private LocationHelper locationHelper;
    private WeatherApiService weatherApiService;
    private HistoryManager historyManager;
    private UserManager userManager;
    private TextView tvLocationValue;
    private TextView tvTemperature;
    private TextView tvFeelsLike;
    private TextView tvWeatherDesc;
    private TextView tvDateValue;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvPrecipitation;
    private TextView tvWeatherSummary;
    private ImageView ivWeatherIcon;
    private android.widget.LinearLayout llForecastContainer;

    private String lastIconUrl;
    private String lastLocation;
    private String lastTemp;
    private String lastFeelsLike;
    private String lastDesc;
    private String lastHumidity;
    private String lastWind;
    private String lastPrecipitation;
    private String lastDate;
    private double lastLat;
    private double lastLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvLocationValue = findViewById(R.id.tv_location_value);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvFeelsLike = findViewById(R.id.tv_feels_like);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvDateValue = findViewById(R.id.tv_date_value);
        tvHumidity = findViewById(R.id.tv_humidity);
        tvWind = findViewById(R.id.tv_wind);
        tvPrecipitation = findViewById(R.id.tv_precipitation);
        tvWeatherSummary = findViewById(R.id.tv_weather_summary);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);
        llForecastContainer = findViewById(R.id.ll_forecast_container);

        userManager = new UserManager(this);
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText(String.format("Welcome, %s!", userManager.getUsername()));

        lastDate = new SimpleDateFormat("d MMMM yyyy . hh:mm a", Locale.US).format(new Date());
        tvDateValue.setText(lastDate);

        findViewById(R.id.btn_view_history).setOnClickListener(v -> {
            Intent intent = new Intent(this, WeatherHistoryActivity.class);
            startActivity(intent);
        });

        weatherApiService = new WeatherApiService();
        historyManager = new HistoryManager(this, userManager.getEmail());
        locationHelper = new LocationHelper(this, this);

        if (locationHelper.hasLocationPermission()) {
            locationHelper.getCurrentLocation();
        } else {
            locationHelper.requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.getCurrentLocation();
            } else {
                tvLocationValue.setText("Location unavailable");
                Toast.makeText(this, "Location permission denied. You can enable it in Settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationReceived(double latitude, double longitude, String cityName) {
        tvLocationValue.setText(cityName);
        lastLat = latitude;
        lastLon = longitude;
        fetchWeather(latitude, longitude);
    }

    @Override
    public void onLocationError(String error) {
        tvLocationValue.setText("Location unavailable");
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private void fetchWeather(double lat, double lon) {
        tvWeatherDesc.setText(getString(R.string.loading_weather));
        tvTemperature.setText("--");
        tvFeelsLike.setText("");
        tvHumidity.setText("--");
        tvWind.setText("--");
        tvPrecipitation.setText("--");

        weatherApiService.getWeatherByCoordinates(lat, lon, new WeatherApiService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                lastLocation = response.getCityWithCountry();
                lastTemp = response.getTemperature();
                lastFeelsLike = "Feels like " + response.getFeelsLike();
                lastDesc = response.getWeatherDescription();
                lastIconUrl = response.getWeatherIconUrl();
                lastHumidity = response.getHumidity();
                lastWind = response.getWindSpeed();
                lastPrecipitation = response.getPrecipitation();
                lastDate = new SimpleDateFormat("d MMMM yyyy . hh:mm a", Locale.US).format(new Date());

                tvLocationValue.setText(lastLocation);
                tvWeatherDesc.setText(lastDesc);
                tvTemperature.setText(lastTemp);
                tvFeelsLike.setText(lastFeelsLike);
                tvHumidity.setText(lastHumidity);
                tvWind.setText(lastWind);
                tvPrecipitation.setText(lastPrecipitation);
                tvDateValue.setText(lastDate);
                tvWeatherSummary.setText(String.format("%s today", lastDesc));

                ImageLoader.load(lastIconUrl, ivWeatherIcon);

                historyManager.saveRecord(new WeatherRecord(
                        lastLocation, lastTemp, lastFeelsLike, lastDesc, 
                        lastIconUrl, lastHumidity, lastWind, lastPrecipitation, lastDate, lastLat, lastLon
                ), userManager.getUserId());

                updateForecastUI(response.getForecast());
            }

            @Override
            public void onError(String error) {
                tvWeatherDesc.setText("Error loading weather");
                Toast.makeText(MainActivity.this, "Weather error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateForecastUI(WeatherResponse.Forecast forecast) {
        if (llForecastContainer == null || forecast == null || forecast.forecastDay == null) return;
        llForecastContainer.removeAllViews();

        for (int i = 1; i < forecast.forecastDay.size() && i <= 4; i++) {
            WeatherResponse.ForecastDay dayData = forecast.forecastDay.get(i);
            addForecastCard(dayData);
        }
    }

    private void addForecastCard(WeatherResponse.ForecastDay dayData) {
        android.view.View cardView = getLayoutInflater().inflate(R.layout.item_forecast_card, llForecastContainer, false);

        TextView tvDate = cardView.findViewById(R.id.tv_forecast_date);
        ImageView ivIcon = cardView.findViewById(R.id.iv_forecast_icon);
        TextView tvTemp = cardView.findViewById(R.id.tv_forecast_temp);

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayData.date);
            tvDate.setText(new SimpleDateFormat("EEE, d MMM", Locale.US).format(date));
        } catch (Exception e) {
            tvDate.setText(dayData.date);
        }

        String iconUrl = "";
        if (dayData.day != null && dayData.day.condition != null) {
            iconUrl = dayData.day.condition.icon;
            if (iconUrl.startsWith("//")) iconUrl = "https:" + iconUrl;
            ImageLoader.load(iconUrl, ivIcon);
        }

        String tempStr = String.format(Locale.US, "%.0f°C", dayData.day.avgTempC);
        tvTemp.setText(tempStr);

        final String finalIconUrl = iconUrl;
        cardView.setOnClickListener(v -> {
            String forecastHumidity = String.format(Locale.US, "%.0f%%", dayData.day.avgHumidity);
            String forecastWind = String.format(Locale.US, "%.1f km/h", dayData.day.maxWindKph);
            String forecastPrecip = dayData.day.totalPrecipMm + " mm";
            String forecastDateStr = dayData.date;
            
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayData.date);
                if (d != null) forecastDateStr = new SimpleDateFormat("d MMMM yyyy", Locale.US).format(d);
            } catch (Exception ignored) {}

            // Save this forecast view to history too
            historyManager.saveRecord(new WeatherRecord(
                    lastLocation, tempStr, "Avg Temp", dayData.day.condition.text,
                    finalIconUrl, forecastHumidity, forecastWind, forecastPrecip, forecastDateStr, lastLat, lastLon
            ), userManager.getUserId());

            Intent intent = new Intent(this, WeatherDetailsActivity.class);
            intent.putExtra(EXTRA_LOCATION, lastLocation);
            intent.putExtra(EXTRA_DATE, dayData.date);
            intent.putExtra(EXTRA_TEMPERATURE, tempStr);
            intent.putExtra(EXTRA_WEATHER_DESC, dayData.day.condition.text);
            intent.putExtra(EXTRA_ICON_URL, finalIconUrl);
            intent.putExtra(EXTRA_HUMIDITY, forecastHumidity);
            intent.putExtra(EXTRA_WIND, forecastWind);
            intent.putExtra(EXTRA_PRECIPITATION, forecastPrecip);
            intent.putExtra(EXTRA_FEELS_LIKE, "Avg Temp " + tempStr);
            intent.putExtra(EXTRA_LAT, lastLat);
            intent.putExtra(EXTRA_LON, lastLon);
            startActivity(intent);
        });

        llForecastContainer.addView(cardView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.removeUpdates();
        }
    }
}
