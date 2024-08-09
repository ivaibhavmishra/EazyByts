package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginSignupActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, signupButton, guestButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Create UI elements programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        emailEditText = new EditText(this);
        emailEditText.setHint("Email");
        emailEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(emailEditText);

        passwordEditText = new EditText(this);
        passwordEditText.setHint("Password");
        passwordEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordEditText);

        loginButton = new Button(this);
        loginButton.setText("Login");
        layout.addView(loginButton);

        signupButton = new Button(this);
        signupButton.setText("Sign Up");
        layout.addView(signupButton);

        guestButton = new Button(this);
        guestButton.setText("Continue as Guest");
        layout.addView(guestButton);

        setContentView(layout);

        loginButton.setOnClickListener(v -> loginUser());
        signupButton.setOnClickListener(v -> registerUser());
        guestButton.setOnClickListener(v -> goToSeenRoom());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginSignupActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        startActivity(new Intent(LoginSignupActivity.this, ChatRoomActivity.class));
                        finish();
                    } else {
                        // If sign in fails
                        Toast.makeText(LoginSignupActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginSignupActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        startActivity(new Intent(LoginSignupActivity.this, ChatRoomActivity.class));
                        finish();
                    } else {
                        // If registration fails
                        Toast.makeText(LoginSignupActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToSeenRoom() {
        // Navigate to SeenRoom without authentication
        startActivity(new Intent(LoginSignupActivity.this, SeenRoom.class));
        finish();
    }
}
