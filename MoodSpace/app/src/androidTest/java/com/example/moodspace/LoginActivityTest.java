package com.example.moodspace;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {
    private String username;

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);
    private LoginActivity activity1;

    @Before
    public void setup() {
        activity1 = activityRule.getActivity();
    }

    @Test
    public void testsignUp() {
        onView(withId(R.id.username)).check(matches(withText("")));
        onView(withId(R.id.password)).check(matches(withText("")));
        onView(withId(R.id.login_btn)).perform(click());
    }


    @Test
    public void testsignuplink() throws InterruptedException {
        onView(withId(R.id.signup_link)).check(matches(withText("New user? SIGN UP")));
        onView(withId(R.id.signup_link)).perform(click());
        onView(withId(R.id.signup_link)).check(matches(withText("Already registered? LOGIN")));

    }

    @Test
    public void testlogin() {
        onView(withId(R.id.username)).check(matches(withText("")));
        onView(withId(R.id.password)).check(matches(withText("")));
        onView(withId(R.id.login_btn)).perform(click());
        onView(withText("Please enter a username and a password")).inRoot(withDecorView(not(is(activity1.getWindow().getDecorView())))).check(matches(isDisplayed()));

    }



}