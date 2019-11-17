package com.example.moodspace;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
/**
 * This tests the login functionality of our app, we first login to the logintest account, and then close the app, upon restarting, we should be on the login screen
 * Enters the username and password again and clicks the remember me option. After logging into the profile page, we close the app again and restart.
 * This time it should login immediately without entering any account details, finally log out.
 */
public class LoginActivityTest {
    private String username;
    private String password;

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void initValidString() {
        username = "logintest";
        password = "logintest";
    }

    @Test
    public void testlogin() throws InterruptedException {
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1500);

        Espresso.pressBackUnconditionally();
        activityRule.launchActivity(null);

        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.rememberMe)).perform(click());
        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1500);

        Espresso.pressBackUnconditionally();
        activityRule.launchActivity(null);
        Thread.sleep(1500);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_item_log_out));
        Thread.sleep(1500);
    }
}