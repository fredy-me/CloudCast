package com.busaradigital.cloudcast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    Button btnRegister;
    TextView tvGoLogin;
    EditText etUsername, etEmail, etPassword, etConfirmPassword;
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userManager = new UserManager(this);

        tvGoLogin = findViewById(R.id.tv_go_login);
        etUsername = findViewById(R.id.register_username);
        etEmail = findViewById(R.id.register_email);
        etPassword = findViewById(R.id.enter_password);
        etConfirmPassword = findViewById(R.id.re_enter_password);

        tvGoLogin.setOnClickListener(v -> {
            Intent openLogin = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(openLogin);
            finish();
        });

        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            userManager.registerUser(username, email, password);
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

            Intent openLogin = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(openLogin);
            finish();
        });
    }
}
