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
    private static final String HOST = "api.weatherapi.com";

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

    public void getWeatherByCity(String city, WeatherCallback callback) {
        String urlStr = BASE_URL + "?key=" + API_KEY + "&q=" + city + "&days=5&aqi=no&alerts=no";
        makeRequest(urlStr, callback);
    }

    public void getWeatherByDate(double lat, double lon, String date, WeatherCallback callback) {
        String urlStr = BASE_URL + "?key=" + API_KEY + "&q=" + lat + "," + lon + "&dt=" + date + "&aqi=no";
        makeRequest(urlStr, callback);
    }

    private void makeRequest(String urlStr, WeatherCallback callback) {
        new Thread(() -> {
            try {
                String json = fetchJsonWithRetry(urlStr, 3);
                WeatherResponse weatherResponse = gson.fromJson(json, WeatherResponse.class);
                mainHandler.post(() -> callback.onSuccess(weatherResponse));
            } catch (Exception e) {
                Log.e("CloudCast", "Request failed: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        }).start();
    }

    private String fetchJsonWithRetry(String urlStr, int retries) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < retries; i++) {
            try {
                return fetchJson(urlStr);
            } catch (Exception e) {
                lastException = e;
                Log.w("CloudCast", "Attempt " + (i + 1) + " failed: " + e.getMessage());
                if (i < retries - 1) Thread.sleep(2000);
            }
        }
        throw (lastException != null) ? lastException : new Exception("Unknown error");
    }

    private String fetchJson(String urlStr) throws Exception {
        try {
            return directFetch(urlStr);
        } catch (Exception e) {
            Log.w("CloudCast", "Direct fetch failed, trying DoH fallback: " + e.getMessage());
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
        String ip = resolveIpViaDoH(HOST);
        if (ip == null) {
            // Try secondary DoH provider (Cloudflare)
            Log.w("CloudCast", "Primary DoH failed, trying Cloudflare...");
            ip = resolveIpViaCloudflare(HOST);
        }
        
        if (ip == null) throw new Exception("DoH resolution failed for both providers");

        String dohUrl = urlStr.replace(HOST, ip);
        // Fallback to http to avoid SNI/Hostname verification issues with raw IP
        dohUrl = dohUrl.replace("https://", "http://");

        HttpURLConnection connection = null;
        try {
            URL url = new URL(dohUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Host", HOST);
            connection.setRequestProperty("Accept", "application/json");

            int code = connection.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code);

            return readStream(connection);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String resolveIpViaDoH(String hostname) {
        return fetchIpFromDnsUrl("https://dns.google/resolve?name=" + hostname + "&type=A");
    }

    private String resolveIpViaCloudflare(String hostname) {
        return fetchIpFromDnsUrl("https://cloudflare-dns.com/query?name=" + hostname + "&type=A");
    }

    private String fetchIpFromDnsUrl(String dnsUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(dnsUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Accept", "application/dns-json");

            if (connection.getResponseCode() != 200) return null;

            String body = readStream(connection);
            Matcher matcher = Pattern.compile("\"data\":\"(\\d+\\.\\d+\\.\\d+\\.\\d+)\"").matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            Log.e("CloudCast", "DNS resolution error for " + dnsUrl + ": " + e.getMessage());
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
