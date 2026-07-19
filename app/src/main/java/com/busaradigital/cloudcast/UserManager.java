package com.busaradigital.cloudcast;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREF_NAME = "CloudCastUserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public UserManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void registerUser(String username, String email, String password) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public boolean loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) return false;

        String storedEmail = sharedPreferences.getString(KEY_EMAIL, "");
        String storedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

        if (email.equals(storedEmail) && password.equals(storedPassword)) {
            setLoggedIn(true);
            return true;
        }
        return false;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "User");
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }
}
