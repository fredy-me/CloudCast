package com.busaradigital.cloudcast;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BackendApiService {
    // Change this to your Ubuntu local IP address
    private static final String BASE_URL = "http://10.28.124.178:8000/api/";
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public BackendApiService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void register(String username, String email, String password, ApiCallback<String> callback) {
        String json = gson.toJson(new RegisterRequest(username, email, password));
        post("register/", json, callback, String.class);
    }

    public void login(String email, String password, ApiCallback<UserResponse> callback) {
        String json = gson.toJson(new LoginRequest(email, password));
        post("login/", json, callback, UserResponse.class);
    }

    public void saveHistory(WeatherRecord record, int userId, ApiCallback<String> callback) {
        WeatherRecordWrapper request = new WeatherRecordWrapper(record, userId);
        String json = gson.toJson(request);
        post("history/", json, callback, String.class);
    }

    public void getHistory(int userId, ApiCallback<List<WeatherRecord>> callback) {
        String url = BASE_URL + "history/?user_id=" + userId;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    mainHandler.post(() -> callback.onError("Empty response"));
                    return;
                }
                String body = response.body().string();
                if (response.isSuccessful()) {
                    List<WeatherRecord> history = gson.fromJson(body, new TypeToken<List<WeatherRecord>>(){}.getType());
                    mainHandler.post(() -> callback.onSuccess(history));
                } else {
                    mainHandler.post(() -> callback.onError("Error: " + response.code()));
                }
            }
        });
    }

    private <T> void post(String endpoint, String json, ApiCallback<T> callback, Class<T> responseType) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    mainHandler.post(() -> callback.onError("Empty response"));
                    return;
                }
                String responseBody = response.body().string();
                android.util.Log.d("BackendAPI", "Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        if (responseType == String.class) {
                            mainHandler.post(() -> callback.onSuccess(responseType.cast("Success")));
                        } else {
                            T result = gson.fromJson(responseBody, responseType);
                            mainHandler.post(() -> callback.onSuccess(result));
                        }
                    } catch (Exception e) {
                        android.util.Log.e("BackendAPI", "Parsing error: " + e.getMessage());
                        mainHandler.post(() -> callback.onError("Data format error"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server Error: " + response.code()));
                }
            }
        });
    }

    // Helper classes for JSON mapping
    private static class RegisterRequest {
        String username, email, password;
        RegisterRequest(String u, String e, String p) { username = u; email = e; password = p; }
    }

    private static class LoginRequest {
        String username, email, password;
        LoginRequest(String e, String p) { username = e; email = e; password = p; }
    }

    public static class UserResponse {
        public int id;
        public String username, email;
    }

    private static class WeatherRecordWrapper {
        public String location, temperature, feels_like, description, icon_url, humidity, wind, precipitation, date_recorded;
        public double latitude, longitude;
        public int user;

        WeatherRecordWrapper(WeatherRecord r, int userId) {
            this.location = r.location;
            this.temperature = r.temperature;
            this.feels_like = r.feelsLike;
            this.description = r.description;
            this.icon_url = r.iconUrl;
            this.humidity = r.humidity;
            this.wind = r.wind;
            this.precipitation = r.precipitation;
            this.date_recorded = r.date;
            this.latitude = r.lat;
            this.longitude = r.lon;
            this.user = userId;
        }
    }
}
