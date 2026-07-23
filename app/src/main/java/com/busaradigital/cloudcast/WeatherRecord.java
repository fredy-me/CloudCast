package com.busaradigital.cloudcast;

import com.google.gson.annotations.SerializedName;

public class WeatherRecord {
    public String location;
    public String temperature;
    
    @SerializedName("feels_like")
    public String feelsLike;
    
    public String description;
    
    @SerializedName("icon_url")
    public String iconUrl;
    
    public String humidity;
    public String wind;
    public String precipitation;
    
    @SerializedName("date_recorded")
    public String date;
    
    @SerializedName("latitude")
    public double lat;
    
    @SerializedName("longitude")
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