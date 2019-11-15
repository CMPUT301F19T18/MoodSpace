package com.example.moodspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import io.paperdb.Paper;

/**
 * Activity for logging in and signing up
 */
public class LoginActivity extends AppCompatActivity {
    UserController uc;

    private boolean inLoginState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();

        final AppCompatButton loginButton = findViewById(R.id.login_btn);
        final AppCompatTextView signUpLink = findViewById(R.id.signup_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);
        final AppCompatEditText veri_password = findViewById(R.id.password_veri);
        final Button logOut = findViewById(R.id.nav_item_log_out);
        Paper.init(this);
        final CheckBox chkBoxRememberMe = findViewById(R.id.rememberMe);
        final String userNameKey = Paper.book().read(SavedUser.userNameKey);
        final String passwordKey = Paper.book().read(SavedUser.passWordKey);

        uc = new UserController(LoginActivity.this);

        if (userNameKey != null && passwordKey != null) {
            uc.loginUser(true, new User(userNameKey, passwordKey));
        }

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
                
                String passwordText = password.getText().toString().trim();
                String usernameText = username.getText().toString().trim();

                if (LoginActivity.this.inLoginState) {
                    if (usernameText.length() > 0 && passwordText.length() > 0) {
                        uc.loginUser(chkBoxRememberMe.isChecked(), new User(usernameText, passwordText));
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Please enter a username and a password", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
        });



    }
}