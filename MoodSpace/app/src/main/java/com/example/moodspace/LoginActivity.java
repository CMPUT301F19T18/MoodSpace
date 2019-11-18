package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;

import io.paperdb.Paper;

/**
 * Activity for logging in as an existing user
 */
public class LoginActivity extends AppCompatActivity
        implements ControllerCallback {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String USERNAME_KEY = "moodspace.LoginActivity.username";
    //public static final String SIGN_UP_USER_KEY = "moodspace.LoginActivity.signUpKey";
    public static final String LOGIN_USER_KEY = "moodspace.LoginActivity.login";

    private UserController uc;

    // so you can't press the login button multiple times
    private boolean inLoginState = true;
    private DocumentSnapshot fetchedUserData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_login);

        final AppCompatButton loginButton = findViewById(R.id.login_btn);
        final AppCompatTextView signUpLink = findViewById(R.id.signup_link);
        final AppCompatEditText username = findViewById(R.id.username);
        final AppCompatEditText password = findViewById(R.id.password);
        final AppCompatEditText veri_password = findViewById(R.id.password_veri);

        Paper.init(this);

        final String savedUsername = Paper.book().read(UserController.PAPER_USERNAME_KEY);
        final String savedPassword = Paper.book().read(UserController.PAPER_PASSWORD_KEY);

        uc = new UserController(this);

        // logs in if there exists a saved username/password
        // TODO create and move to initial loading activity
        if (savedUsername != null && savedPassword != null) {
            attemptLogin(new User(savedUsername, savedPassword));
        }

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpScreen = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpScreen);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwordText = password.getText().toString().trim();
                String usernameText = username.getText().toString().trim();
                final User inputtedUser = new User(usernameText, passwordText);

                if (LoginActivity.this.inLoginState) {
                    if (usernameText.length() == 0 || passwordText.length() == 0) {
                        Toast.makeText(LoginActivity.this,
                                "Please enter a username and a password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    attemptLogin(inputtedUser);
                    loginButton.setEnabled(false);
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
            }
        });
    }

    /**
     * attempts to log in the user.
     * - fetches the user data from firestore only if not already gotten
     */
    private void attemptLogin(final User user) {
        final String username = user.getUsername();
        if (fetchedUserData == null || fetchedUserData.get("username") != username) {
            uc.getUserData(username, new UserController.CallbackUser() {
                @Override
                public void callbackUserData(DocumentSnapshot fetchedUserData, String callbackId) {
                    LoginActivity.this.fetchedUserData = fetchedUserData;
                    uc.checkPassword(user, fetchedUserData);
                }
            });
        } else {
            uc.checkPassword(user, fetchedUserData);
        }
    }


    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        final CheckBox chkBoxRememberMe = findViewById(R.id.rememberMe);
        final AppCompatButton loginButton = findViewById(R.id.login_btn);
        View snackBarView = findViewById(R.id.login_view);
        User user;

        if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                /*
                case USERNAME_NOT_TAKEN:
                    if (bundle == null) {
                        Snackbar.make(snackBarView,
                                "Unexpected error: sign up user key bundle should not be null",
                                Snackbar.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                        return;
                    }
                    user = (User) bundle.getSerializable(SIGN_UP_USER_KEY);
                    if (user == null) {
                        Snackbar.make(snackBarView,
                                "Unexpected error: sign up user key result should not contain a null user",
                                Snackbar.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                        return;
                    }
                    uc.signUpUser(user);
                    return;
                 */

                case LOGIN:
                    if (bundle == null) {
                        Snackbar.make(snackBarView,
                                "Unexpected error: login user key bundle should not be null",
                                Snackbar.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                        return;
                    }
                    user = (User) bundle.getSerializable(LOGIN_USER_KEY);
                    if (user == null) {
                        Snackbar.make(snackBarView,
                                "Unexpected error: login user key result should not contain a null user",
                                Snackbar.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                        return;
                    }
                    // stores username and password if checked
                    if (chkBoxRememberMe.isChecked()) {
                        uc.rememberUser(user);
                    }
                    Intent i = new Intent(this, ProfileListActivity.class);
                    i.putExtra(USERNAME_KEY, user.getUsername());
                    startActivity(i);
                    finish();
                    return;

                case USERNAME_TAKEN:
                    Toast.makeText(this, "This username is taken", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    return;

                case INCORRECT_PASSWORD:
                    Toast.makeText(this, "Incorrect password, please try again", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    return;

                case USER_NONEXISTENT:
                    Toast.makeText(this, "This username does not exist", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    return;

                case USER_READ_DATA_FAIL:
                    Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    return;

                case USER_TASK_NULL:
                    Snackbar.make(snackBarView,
                            "Unexpected error: user task result should not be null",
                            Snackbar.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    return;

                case USER_ADDITION_FAIL:
                case FILTER_INITIALIZE_FAIL:
                    Snackbar.make(snackBarView,
                            "Failed to register user, please try again",
                            Snackbar.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    return;

                default:
                    Log.w(TAG, "unrecognized callback ID: " + callbackId);
            }
        } else {
            Log.w(TAG, "unrecognized callback ID: " + callbackId);
        }
    }
}