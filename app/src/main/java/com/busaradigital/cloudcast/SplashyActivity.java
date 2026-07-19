package com.busaradigital.cloudcast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Transition to LoginActivity after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            UserManager userManager = new UserManager(this);
            Intent intent;
            if (userManager.isLoggedIn()) {
                intent = new Intent(SplashyActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashyActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}