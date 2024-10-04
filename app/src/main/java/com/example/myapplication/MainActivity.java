package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;  // Added for logging
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If the user is logged in, disable the register button
        buttonRegister = findViewById(R.id.button_register);

        if (currentUser != null) {
            // User is logged in, disable the register button
            Log.d("MainActivity", "User is logged in: " + currentUser.getEmail());  // Log for debugging
            buttonRegister.setEnabled(false);  // Disables the button
            // Alternatively, you can hide the button: buttonRegister.setVisibility(View.GONE);
        } else {
            Log.d("MainActivity", "No user is logged in");
        }

        // Open login activity
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Open register activity
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
