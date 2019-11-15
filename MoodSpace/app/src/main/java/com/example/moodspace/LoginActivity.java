package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import io.paperdb.Paper;

/**
 * Activity for logging in as an existing user
 */
public class LoginActivity extends AppCompatActivity {
    UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();

        final AppCompatButton loginButton = findViewById(R.id.login_btn);
        final AppCompatTextView signUpLink = findViewById(R.id.signup_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);

        Paper.init(this);
        final CheckBox chkBoxRememberMe = findViewById(R.id.rememberMe);
        final String userNameKey = Paper.book().read(SavedUser.userNameKey);
        final String passwordKey = Paper.book().read(SavedUser.passWordKey);

        uc = new UserController(LoginActivity.this);

        if (userNameKey != null && passwordKey != null) {
            uc.loginUser(true, new User(userNameKey, passwordKey));
        }

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent signUpScreen = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(signUpScreen);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String passwordText = password.getText().toString().trim();
            String usernameText = username.getText().toString().trim();

            if (usernameText.length() > 0 && passwordText.length() > 0) {
                uc.loginUser(chkBoxRememberMe.isChecked(), new User(usernameText, passwordText));
            } else {
                Toast.makeText(LoginActivity.this,
                        "Please enter a username and a password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}