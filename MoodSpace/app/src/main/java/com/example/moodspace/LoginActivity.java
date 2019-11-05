package com.example.moodspace;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    AppCompatButton loginButton;
    AppCompatTextView signUpLink;
    AppCompatEditText username;
    AppCompatEditText password;

    UserController uc;

    boolean inLoginState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        // actionBar.hide();

        signUpLink = findViewById(R.id.signup_link);
        loginButton = findViewById(R.id.login_btn);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        uc = new UserController(LoginActivity.this);

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inLoginState) {
                    loginButton.setText("Sign Up");
                    signUpLink.setText("Already registered? LOGIN");
                    inLoginState = false;
                } else {
                    loginButton.setText("Login");
                    signUpLink.setText("New user? SIGN UP");
                    inLoginState = true;
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwordText = password.getText().toString().trim();
                String usernameText = username.getText().toString().trim();

                if (usernameText.length() > 0 && passwordText.length() > 0) {
                    if (inLoginState) {
                        uc.loginUser(new User(usernameText, passwordText));
                    } else {
                        uc.checkUserExists(new User(usernameText, passwordText));
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a username and a password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
