package com.busaradigital.cloudcast;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherApiService {

    private static final String BASE_URL = "https://api.weatherapi.com/v1/forecast.json";
    private static final String API_KEY = "d82e1ecc3310487091991447261907";

    private final Gson gson;
    private final Handler mainHandler;

    public interface WeatherCallback {
        void onSuccess(WeatherResponse response);
        void onError(String error);
    }

    public WeatherApiService() {
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getWeatherByCoordinates(double lat, double lon, WeatherCallback callback) {
        String urlStr = BASE_URL + "?key=" + API_KEY + "&q=" + lat + "," + lon + "&days=5&aqi=no&alerts=no";
        makeRequest(urlStr, callback);
    }

    public void getWeatherByDate(double lat, double lon, String date, WeatherCallback callback) {
        String urlStr = BASE_URL + "?key=" + API_KEY + "&q=" + lat + "," + lon + "&dt=" + date + "&aqi=no";
        makeRequest(urlStr, callback);
    }

    private void makeRequest(String urlStr, WeatherCallback callback) {
        makeRequestWithRetry(urlStr, callback, 3);
    }

    private void makeRequestWithRetry(String urlStr, WeatherCallback callback, int retriesLeft) {
        Log.d("CloudCast", "Fetching weather (attempt " + (4 - retriesLeft) + "/3)");
        new Thread(() -> {
            try {
                String json = fetchJson(urlStr);
                WeatherResponse weatherResponse = gson.fromJson(json, WeatherResponse.class);
                mainHandler.post(() -> callback.onSuccess(weatherResponse));
            } catch (Exception e) {
                Log.e("CloudCast", "Request failed: " + e.getMessage());
                if (retriesLeft > 1) {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    makeRequestWithRetry(urlStr, callback, retriesLeft - 1);
                    return;
                }
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        }).start();
    }

    private String fetchJson(String urlStr) throws Exception {
        try {
            return directFetch(urlStr);
        } catch (Exception e) {
            Log.w("CloudCast", "Direct fetch failed: " + e.getMessage() + ", trying DoH fallback");
            return dohFetch(urlStr);
        }
    }

    private String directFetch(String urlStr) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");

            int code = connection.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code);

            return readStream(connection);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String dohFetch(String urlStr) throws Exception {
        String host = "api.weatherapi.com";
        String ip = resolveIpViaDoH(host);
        if (ip == null) throw new Exception("DoH resolution failed for " + host);

        Log.d("CloudCast", "DoH resolved " + host + " -> " + ip);
        String dohUrl = urlStr.replace("https://" + host, "http://" + ip);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(dohUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Host", host);
            connection.setRequestProperty("Accept", "application/json");

            int code = connection.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code);

            return readStream(connection);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String resolveIpViaDoH(String hostname) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://dns.google/resolve?name=" + hostname + "&type=A");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/dns-json");

            if (connection.getResponseCode() != 200) return null;

            String body = readStream(connection);
            Matcher matcher = Pattern.compile("\"data\":\"(\\d+\\.\\d+\\.\\d+\\.\\d+)\"").matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            Log.e("CloudCast", "DoH resolution error: " + e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    private String readStream(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
