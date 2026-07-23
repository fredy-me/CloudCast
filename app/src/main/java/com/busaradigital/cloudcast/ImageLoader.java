package com.busaradigital.cloudcast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ImageLoader {

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final ConcurrentHashMap<String, Bitmap> cache = new ConcurrentHashMap<>();

    public static void load(String url, ImageView imageView) {
        if (url == null || url.isEmpty()) return;

        Bitmap cached = cache.get(url);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        new Thread(() -> {
            try {
                Bitmap bitmap = fetchBitmap(url);
                if (bitmap != null) {
                    cache.put(url, bitmap);
                    mainHandler.post(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                Log.e("CloudCast", "Image load failed: " + e.getMessage());
            }
        }).start();
    }

    private static Bitmap fetchBitmap(String urlStr) {
        try {
            return directFetch(urlStr);
        } catch (Exception e) {
            Log.w("CloudCast", "Direct image fetch failed, trying DoH fallback: " + e.getMessage());
            try {
                return dohFetch(urlStr);
            } catch (Exception e2) {
                Log.e("CloudCast", "DoH image fetch also failed: " + e2.getMessage());
                return null;
            }
        }
    }

    private static Bitmap directFetch(String urlStr) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = connection.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code);

            InputStream inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static Bitmap dohFetch(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        String host = url.getHost();
        String ip = resolveIpViaDoH(host);
        if (ip == null) throw new Exception("DoH resolution failed for " + host);

        String dohUrl = urlStr.replace(host, ip);
        // Fallback to http for IP-based requests
        dohUrl = dohUrl.replace("https://", "http://");

        HttpURLConnection connection = null;
        try {
            URL finalUrl = new URL(dohUrl);
            connection = (HttpURLConnection) finalUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Host", host);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setInstanceFollowRedirects(true);

            int code = connection.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code);

            InputStream inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String resolveIpViaDoH(String hostname) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://dns.google/resolve?name=" + hostname + "&type=A");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/dns-json");

            if (connection.getResponseCode() != 200) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"data\":\"(\\d+\\.\\d+\\.\\d+\\.\\d+)\"").matcher(sb.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            Log.e("CloudCast", "DoH image resolution error: " + e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }
}
