package com.busaradigital.cloudcast;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("location")
    private Location location;

    @SerializedName("current")
    private Current current;

    @SerializedName("forecast")
    private Forecast forecast;

    public Forecast getForecast() { return forecast; }

    public String getTemperature() {
        if (current == null) return "--";
        return String.format("%.0f\u00B0C", current.tempC);
    }

    public String getFeelsLike() {
        if (current == null) return "--";
        return String.format("%.0f\u00B0C", current.feelsLikeC);
    }

    public String getHumidity() {
        if (current == null) return "--";
        return current.humidity + "%";
    }

    public String getWeatherDescription() {
        if (current == null || current.condition == null) return "Unknown";
        return current.condition.text;
    }

    public String getWeatherIconUrl() {
        if (current == null || current.condition == null) return "";
        String icon = current.condition.icon;
        if (icon.startsWith("//")) {
            return "https:" + icon;
        }
        return icon;
    }

    public String getCityWithCountry() {
        if (location == null) return "Unknown";
        String city = (location.name != null) ? location.name : "Unknown";
        String country = (location.country != null) ? location.country : "";
        if (!country.isEmpty()) {
            return city + ", " + country;
        }
        return city;
    }

    public String getWindSpeed() {
        if (current == null) return "--";
        return String.format(java.util.Locale.US, "%.0f km/h", current.windKph);
    }

    public String getPrecipitation() {
        if (current == null) return "0 mm";
        return current.precipMm + " mm";
    }

    public static class Location {
        @SerializedName("name")
        public String name;
        @SerializedName("country")
        public String country;
        @SerializedName("lat")
        public double lat;
        @SerializedName("lon")
        public double lon;
    }

    public static class Current {
        @SerializedName("temp_c")
        public double tempC;
        @SerializedName("condition")
        public Condition condition;
        @SerializedName("wind_kph")
        public double windKph;
        @SerializedName("humidity")
        public int humidity;
        @SerializedName("feelslike_c")
        public double feelsLikeC;
        @SerializedName("precip_mm")
        public double precipMm;
    }

    public static class Condition {
        @SerializedName("text")
        public String text;
        @SerializedName("icon")
        public String icon;
        @SerializedName("code")
        public int code;
    }

    public static class Forecast {
        @SerializedName("forecastday")
        public List<ForecastDay> forecastDay;
    }

    public static class ForecastDay {
        @SerializedName("date")
        public String date;
        @SerializedName("day")
        public Day day;
    }

    public static class Day {
        @SerializedName("avgtemp_c")
        public double avgTempC;
        @SerializedName("avghumidity")
        public double avgHumidity;
        @SerializedName("maxwind_kph")
        public double maxWindKph;
        @SerializedName("condition")
        public Condition condition;
        @SerializedName("totalprecip_mm")
        public double totalPrecipMm;
    }
}
