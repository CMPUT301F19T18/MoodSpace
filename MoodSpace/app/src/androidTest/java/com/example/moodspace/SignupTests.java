package com.example.moodspace;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.os.Trace.isEnabled;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
/**
 * In this test, we'll signup the user and then log out, attempt to signup again with same info to test that it displays an error that there is already an existing user.
 * Login to verify that the account has been created from before then log out
 * Delete the account from firebase db for future tests.
 */
public class SignupTests {
    private String username;
    private String password;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void initValidString() {
        username = "signuptest";
        password = "signuptest";
    }

    @Test
    public void testsignUp() throws InterruptedException {
        onView(withId(R.id.signup_link)).perform(click());
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.password_veri)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.signup_btn)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        Thread.sleep(1500);
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_item_log_out));
        Thread.sleep(1500);

        onView(withId(R.id.signup_link)).perform(click());
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.password_veri)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.signup_btn)).perform(click());
//        Show existing user error.
        onView(withText("This username is taken")).inRoot(withDecorView(not(activityRule.getActivity().getWindow().getDecorView())))
        .check(matches(isDisplayed()));
        Thread.sleep(1500);
        onView(withId(R.id.login_link)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); // no constraints, they are checked above
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        Thread.sleep(1500);
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_item_log_out));
        Thread.sleep(1500);
        //Delete account for future tests.
        db.collection("users").document(username).delete();
    }
}
