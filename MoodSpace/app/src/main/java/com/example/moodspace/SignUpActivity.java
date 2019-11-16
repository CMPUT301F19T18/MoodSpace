package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.snackbar.Snackbar;

/**
 * Activity for signing up as a new user
 */
public class SignUpActivity extends AppCompatActivity
        implements ControllerCallback {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    public static final String SIGN_UP_USER_KEY = "moodspace.SignUpActivity.signUpKey";

    private UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_signup);

        final AppCompatButton signUpButton = findViewById(R.id.signup_btn);
        final AppCompatTextView loginLink = findViewById(R.id.login_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);
        final AppCompatEditText veri_password = findViewById(R.id.password_veri);

        uc = new UserController(this);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                } else {
                    String veriPasswordText = veri_password.getText().toString().trim();
                    if (usernameText.length() == 0 || passwordText.length() == 0 || veriPasswordText.length() == 0) {
                        Toast.makeText(LoginActivity.this,
                                "Please enter a username, a password, and a password verification.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!passwordText.equals(veriPasswordText)) {
                        Toast.makeText(LoginActivity.this,
                                "Please enter a matching password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uc.checkUserExists(inputtedUser);
                    loginButton.setEnabled(false);
                }
                 */
                final String passwordText = password.getText().toString().trim();
                final String usernameText = username.getText().toString().trim();
                String veriPasswordText = veri_password.getText().toString().trim();

                if (usernameText.length() == 0 || passwordText.length() == 0 || veriPasswordText.length() == 0) {
                    Toast.makeText(SignUpActivity.this,
                            "Please enter a username, a password, and a password verification.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!passwordText.equals(veriPasswordText)) {
                    Toast.makeText(SignUpActivity.this,
                            "Please enter a matching password", Toast.LENGTH_SHORT).show();
                    return;
                }

                final User inputtedUser = new User(usernameText, passwordText);
                uc.checkUserExists(inputtedUser);
                signUpButton.setEnabled(false);
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

    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {

        final AppCompatButton signUpButton = findViewById(R.id.signup_btn);
        View snackBarView = findViewById(R.id.signup_view);
        User user;

        if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                case USERNAME_NOT_TAKEN:
                    if (bundle == null) {
                        Snackbar.make(snackBarView,
                                "Unexpected error: sign up user key bundle should not be null",
                                Snackbar.LENGTH_LONG).show();
                        signUpButton.setEnabled(true);
                        return;
                    }
                    user = (User) bundle.getSerializable(SIGN_UP_USER_KEY);
                    if (user == null) {
                        Snackbar.make(snackBarView,
                                "Unexpected error: sign up user key result should not contain a null user",
                                Snackbar.LENGTH_LONG).show();
                        signUpButton.setEnabled(true);
                        return;
                    }
                    uc.signUpUser(user);
                    return;

                case USERNAME_TAKEN:
                    Toast.makeText(this, "This username is taken", Toast.LENGTH_SHORT).show();
                    signUpButton.setEnabled(true);
                    return;

                default:
                    Log.w(TAG, "unrecognized callback ID: " + callbackId);
            }
        }
    }
}