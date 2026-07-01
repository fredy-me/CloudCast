package com.busaradigital.cloudcast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    Button btnRegister;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            // Handle button click here
            Intent openLogin = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(openLogin);
            finish();

        });

        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            // Handle button click here
            Intent openRegister = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(openRegister);
            finish();

        });
    }
}