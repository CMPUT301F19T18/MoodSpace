package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;

import io.paperdb.Paper;

import static com.example.moodspace.Utils.getSerializableFromBundle;
import static com.example.moodspace.Utils.makeInfoToast;
import static com.example.moodspace.Utils.makeWarnToast;
import static com.example.moodspace.Utils.newUserBundle;

/**
 * Activity for logging in as an existing user and for signing up an existing user
 */
public class LoginActivity extends AppCompatActivity
        implements ControllerCallback {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String IS_SIGN_IN_ACTIVITY_KEY = "moodspace.LoginActivity.isSignInActivityKey";
    public static final String SIGN_UP_USER_KEY = "moodspace.LoginActivity.signUpKey";
    public static final String LOGIN_USER_KEY = "moodspace.LoginActivity.login";

    private UserController uc;

    // so you can't press the login button multiple times
    private boolean inLoginState = true;
    private DocumentSnapshot fetchedUserData = null;
    private AppCompatButton confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(IS_SIGN_IN_ACTIVITY_KEY)
                && extras.getBoolean(IS_SIGN_IN_ACTIVITY_KEY)) {
            this.inLoginState = false;
        }
        uc = new UserController(this);

        if (inLoginState) {
            setContentView(R.layout.activity_login);

            // TODO create and move to initial loading activity
            // logs in if there exists a saved username/password
            Paper.init(this);
            final String savedUsername = Paper.book().read(UserController.PAPER_USERNAME_KEY);
            final String savedPassword = Paper.book().read(UserController.PAPER_PASSWORD_KEY);
            if (savedUsername != null && savedPassword != null) {
                attemptLogin(new User(savedUsername, savedPassword));
            }

            confirmButton = findViewById(R.id.login_btn);

            final AppCompatTextView signUpLink = findViewById(R.id.signup_link);
            signUpLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent signUpScreen = new Intent(LoginActivity.this, LoginActivity.class);
                    signUpScreen.putExtra(IS_SIGN_IN_ACTIVITY_KEY, true);
                    startActivity(signUpScreen);
                }
            });
        } else {
            setContentView(R.layout.activity_signup);

            confirmButton = findViewById(R.id.signup_btn);

            final AppCompatTextView loginLink = findViewById(R.id.login_link);
            loginLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    // by default, is the login activity so no extra needs to be put in
                    Intent loginScreen = new Intent(LoginActivity.this, LoginActivity.class);
                    startActivity(loginScreen);
                    finish();
                }
            });

        }

        final AppCompatEditText usernameEditText = findViewById(R.id.username);
        final AppCompatEditText passwordEditText = findViewById(R.id.password);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmButton.setEnabled(false);

                Editable usernameText = usernameEditText.getText();
                Editable passwordText = passwordEditText.getText();
                if (usernameText == null) {
                    makeWarnToast(LoginActivity.this, "Cannot read the username, please try again");
                    confirmButton.setEnabled(true);
                    return;
                }
                if (passwordText == null) {
                    makeWarnToast(LoginActivity.this, "Cannot read the password, please try again");
                    confirmButton.setEnabled(true);
                    return;
                }

                String username = usernameText.toString().trim();
                String password = passwordText.toString().trim();
                final User inputtedUser = new User(username, password);

                if (inLoginState) {
                    if (usernameText.length() == 0 || passwordText.length() == 0) {
                        makeWarnToast(LoginActivity.this, "Please enter a username and a password");
                        confirmButton.setEnabled(true);
                        return;
                    }
                    attemptLogin(inputtedUser);
                } else {
                    final AppCompatEditText veriPasswordEditText = findViewById(R.id.password_veri);
                    Editable veriPasswordText = veriPasswordEditText.getText();
                    if (veriPasswordText == null) {
                        makeWarnToast(LoginActivity.this,
                                "Cannot read the password verification, please try again");
                        confirmButton.setEnabled(true);
                        return;
                    }

                    String veriPassword = veriPasswordText.toString().trim();
                    if (username.length() == 0 || password.length() == 0 || veriPassword.length() == 0) {
                        makeInfoToast(LoginActivity.this,
                                "Please enter a username, a password, and a password verification.");
                        confirmButton.setEnabled(true);
                        return;
                    }
                    if (!password.equals(veriPassword)) {
                        makeWarnToast(LoginActivity.this,
                                "Please enter a matching password");
                        confirmButton.setEnabled(true);
                        return;
                    }
                    uc.checkUsernameExists(username, newUserBundle(SIGN_UP_USER_KEY, inputtedUser));
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
        // caches fetchedUserData for repeated usage
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

        final AppCompatButton confirmButton;
        if (this.inLoginState) {
            confirmButton = findViewById(R.id.login_btn);
        } else {
            confirmButton = findViewById(R.id.signup_btn);
        }
        View snackBarView = findViewById(R.id.login_view);

        User user;

        if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                case USERNAME_DOESNT_EXIST:
                    user = (User) getSerializableFromBundle(bundle, SIGN_UP_USER_KEY, snackBarView,
                            "Unexpected error: sign up user key bundle should not be null",
                            "Unexpected error: sign up user key result should not contain a null user");
                    if (user == null) {
                        confirmButton.setEnabled(true);
                        return;
                    }

                    uc.signUpUser(user);
                    break;

                case LOGIN:
                    user = (User) getSerializableFromBundle(bundle, LOGIN_USER_KEY, snackBarView,
                            "Unexpected error: login user key bundle should not be null",
                            "Unexpected error: login user key result should not contain a null user");
                    if (user == null) {
                        confirmButton.setEnabled(true);
                        return;
                    }

                    // stores username and password if checked
                    if (this.inLoginState) {
                        final CheckBox chkBoxRememberMe = findViewById(R.id.rememberMe);
                        if (chkBoxRememberMe.isChecked()) {
                            uc.rememberUser(user);
                        }
                    }

                    // goes to profile list activity
                    Intent i = new Intent(this, ProfileListActivity.class);
                    i.putExtra(Utils.USERNAME_KEY, user.getUsername());
                    startActivity(i);
                    finish();
                    break;

                case USERNAME_EXISTS:
                    makeWarnToast(this, "This username is taken");
                    confirmButton.setEnabled(true);
                    return;

                case INCORRECT_PASSWORD:
                    makeWarnToast(this, "Incorrect password, please try again");
                    confirmButton.setEnabled(true);
                    return;

                case USER_NONEXISTENT:
                    makeWarnToast(this, "This username does not exist");
                    confirmButton.setEnabled(true);
                    return;

                case USER_READ_DATA_FAIL:
                    makeWarnToast(this, "Login failed, please try again");
                    confirmButton.setEnabled(true);
                    return;

                case USER_TASK_NULL:
                    Snackbar.make(snackBarView,
                            "Unexpected error: user task result should not be null",
                            Snackbar.LENGTH_LONG).show();
                    confirmButton.setEnabled(true);
                    return;

                case USER_ADDITION_FAIL:
                case FILTER_INITIALIZE_FAIL:
                    Snackbar.make(snackBarView,
                            "Failed to register user, please try again",
                            Snackbar.LENGTH_LONG).show();
                    confirmButton.setEnabled(true);
                    return;

                default:
                    Log.w(TAG, "unrecognized callback ID: " + callbackId);
            }
        } else {
            Log.w(TAG, "unrecognized callback ID: " + callbackId);
        }
    }
}