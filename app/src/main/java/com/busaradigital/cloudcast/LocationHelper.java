package com.busaradigital.cloudcast;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    public static final int PERMISSION_REQUEST_CODE = 1001;

    public interface LocationResultListener {
        void onLocationReceived(double latitude, double longitude, String cityName);
        void onLocationError(String error);
    }

    private final Activity activity;
    private final LocationResultListener callback;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallbackReceiver locationCallbackReceiver;

    public LocationHelper(Activity activity, LocationResultListener callback) {
        this.activity = activity;
        this.callback = callback;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    @SuppressWarnings("MissingPermission")
    public void getCurrentLocation() {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
            if (location != null) {
                reverseGeocode(location.getLatitude(), location.getLongitude());
            } else {
                requestFreshLocation();
            }
        }).addOnFailureListener(e -> callback.onLocationError("Failed to get location: " + e.getMessage()));
    }

    @SuppressWarnings("MissingPermission")
    private void requestFreshLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        locationCallbackReceiver = new LocationCallbackReceiver();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallbackReceiver, Looper.getMainLooper());
    }

    private void reverseGeocode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                if (cityName == null) cityName = address.getSubAdminArea();
                if (cityName == null) cityName = address.getAdminArea();
                if (cityName == null) cityName = String.format(Locale.US, "%.4f, %.4f", latitude, longitude);
                callback.onLocationReceived(latitude, longitude, cityName);
            } else {
                callback.onLocationReceived(latitude, longitude,
                        String.format(Locale.US, "%.4f, %.4f", latitude, longitude));
            }
        } catch (IOException e) {
            callback.onLocationReceived(latitude, longitude,
                    String.format(Locale.US, "%.4f, %.4f", latitude, longitude));
        }
    }

    public void removeUpdates() {
        if (locationCallbackReceiver != null) {
            fusedLocationClient.removeLocationUpdates(locationCallbackReceiver);
        }
    }

    private class LocationCallbackReceiver extends com.google.android.gms.location.LocationCallback {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                reverseGeocode(location.getLatitude(), location.getLongitude());
            } else {
                callback.onLocationError("Unable to determine location");
            }
        }
    }
}
