package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Activity for signing up as a new user
 */
public class SignUpActivity extends AppCompatActivity {
    UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_signup);

        final AppCompatButton signupButton = findViewById(R.id.signup_btn);
        final AppCompatTextView loginLink = findViewById(R.id.login_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);
        final AppCompatEditText veri_password = findViewById(R.id.password_veri);

        uc = new UserController(SignUpActivity.this);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String passwordText = password.getText().toString().trim();
                final String usernameText = username.getText().toString().trim();
                String veriPasswordText = veri_password.getText().toString().trim();
                if (usernameText.length() > 0 && passwordText.length() > 0 && veriPasswordText.length() > 0) {
                    if (passwordText.equals(veriPasswordText)) {
                        uc.checkUserExists(new User(usernameText, passwordText));
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Please enter a matching password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this,
                            "Please enter a username, a password, and a password verification.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent loginScreen = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(loginScreen);
                finish();
            }
        });
    }
}