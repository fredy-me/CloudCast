package com.busaradigital.cloudcast;

public class WeatherRecord {
    public String location;
    public String temperature;
    public String feelsLike;
    public String description;
    public String iconUrl;
    public String humidity;
    public String wind;
    public String precipitation;
    public String date;
    public double lat;
    public double lon;

    public WeatherRecord(String location, String temperature, String feelsLike, String description, 
                         String iconUrl, String humidity, String wind, String precipitation, String date, double lat, double lon) {
        this.location = location;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.description = description;
        this.iconUrl = iconUrl;
        this.humidity = humidity;
        this.wind = wind;
        this.precipitation = precipitation;
        this.date = date;
        this.lat = lat;
        this.lon = lon;
    }
}