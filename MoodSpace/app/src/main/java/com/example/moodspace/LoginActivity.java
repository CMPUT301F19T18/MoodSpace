package com.example.moodspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

/**
 * Activity for logging in and signing up
 */
public class LoginActivity extends AppCompatActivity implements ControllerCallback {
    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final String USERNAME_KEY = "moodspace.UserController.username";

    private UserController uc;

    // so you can't press the login button multiple times
    private boolean canClick = true;
    private boolean inLoginState = true;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final AppCompatButton loginButton = findViewById(R.id.login_btn);
        final AppCompatTextView signUpLink = findViewById(R.id.signup_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);
        final AppCompatEditText veri_password = findViewById(R.id.password_veri);
        final Button logOut = findViewById(R.id.nav_item_log_out);

        uc = new UserController(this);

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
                if (!canClick) {
                    return;
                }

                String passwordText = password.getText().toString().trim();
                String usernameText = username.getText().toString().trim();
                currentUser = new User(usernameText, passwordText);

                if (LoginActivity.this.inLoginState) {
                    if (usernameText.length() > 0 && passwordText.length() > 0) {
                        uc.loginUser(currentUser);
                        canClick = false;
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Please enter a username and a password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String veriPasswordText = veri_password.getText().toString().trim();
                    if (usernameText.length() > 0 && passwordText.length() > 0 && veriPasswordText.length() > 0) {
                        if (passwordText.equals(veriPasswordText)) {
                            uc.checkUserExists(currentUser);
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
        });

    }

    public boolean getCanClick() {
        return this.canClick;
    }

    public void callback(String callbackId) {
        View snackBarView = findViewById(R.id.login_view);
        switch (callbackId) {
            case UserController.USERNAME_NOT_TAKEN:
                uc.signUpUser(currentUser);
                return;

            case UserController.LOGIN:
                Intent i = new Intent(this, ProfileListActivity.class);
                i.putExtra(USERNAME_KEY, currentUser.getUsername());
                startActivity(i);
                finish();
                return;

            case UserController.USERNAME_TAKEN:
                Toast.makeText(this, "This username is taken", Toast.LENGTH_SHORT).show();
                canClick = true;
                return;

            case UserController.LOGIN_READ_FAIL:
                Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show();
                canClick = true;
                return;

            case UserController.INCORRECT_PASSWORD:
                Toast.makeText(this, "Incorrect password, please try again", Toast.LENGTH_SHORT).show();
                canClick = true;
                return;

            case UserController.USERNAME_NONEXISTENT:
                Toast.makeText(this, "This username does not exist", Toast.LENGTH_SHORT).show();
                canClick = true;
                return;

            case UserController.PASSWORD_TASK_NULL:
                Snackbar.make(snackBarView,
                        "Unexpected error: password task result should not be null",
                        Snackbar.LENGTH_LONG).show();
                canClick = true;
                return;

            case UserController.PASSWORD_FETCH_NULL:
                Snackbar.make(snackBarView,
                        "Unexpected error: fetched password should not be null",
                        Snackbar.LENGTH_LONG).show();
                canClick = true;
                return;

            case UserController.USER_ADDITION_FAIL:
                Utils.displayCriticalError(this, "Failed to upload the user to FireStore");
                return;

            case UserController.FILTER_INITIALIZE_FAIL:
                Utils.displayCriticalError(this, "Failed to initialize the filters in FireStore");
                return;

            default:
                Log.w(TAG, "unrecognized callback ID: " + callbackId);
        }
    }
}


