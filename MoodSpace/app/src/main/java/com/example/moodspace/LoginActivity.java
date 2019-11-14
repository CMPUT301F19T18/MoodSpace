package com.example.moodspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Activity for logging in and signing up
 */
public class LoginActivity extends AppCompatActivity {
    UserController uc;

    private boolean inLoginState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();

        final AppCompatButton loginButton = findViewById(R.id.login_btn);
        final AppCompatTextView signUpLink = findViewById(R.id.signup_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);
        final AppCompatEditText veri_password = findViewById(R.id.password_veri);

        uc = new UserController(LoginActivity.this);

        veri_password.setVisibility(View.GONE);
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inLoginState) {
                    loginButton.setText("Sign Up");
                    signUpLink.setText("Already registered? LOGIN");
                    veri_password.setVisibility(View.VISIBLE);
                    inLoginState = false;
                } else {
                    loginButton.setText("Login");
                    signUpLink.setText("New user? SIGN UP");
                    veri_password.setVisibility(View.GONE);
                    inLoginState = true;
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginAttempt();
            }
        });

        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                if (keycode == KeyEvent.KEYCODE_ENTER) {
                    closekeyboard();
                    loginButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loginAttempt();
                        }
                    });
                }
                return false;
            }
        });
    }

    private void loginAttempt() {
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);

        String passwordText = password.getText().toString().trim();
        String usernameText = username.getText().toString().trim();
        closekeyboard();

        if (LoginActivity.this.inLoginState) {
            if (usernameText.length() > 0 && passwordText.length() > 0) {
                uc.loginUser(new User(usernameText, passwordText));
            } else {
                Toast.makeText(LoginActivity.this,
                        "Please enter a username and a password", Toast.LENGTH_SHORT).show();
            }
        } else {
            final AppCompatEditText veri_password = findViewById(R.id.password_veri);
            String veriPasswordText = veri_password.getText().toString().trim();
            if (usernameText.length() > 0 && passwordText.length() > 0 && veriPasswordText.length() > 0) {
                if (passwordText.equals(veriPasswordText)) {
                    uc.checkUserExists(new User(usernameText, passwordText));
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Please enter a matching password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this,
                        "Please enter a username, a password, and a password verification.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void closekeyboard() {
        View view = this.getCurrentFocus();
        if (view!= null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }
}


