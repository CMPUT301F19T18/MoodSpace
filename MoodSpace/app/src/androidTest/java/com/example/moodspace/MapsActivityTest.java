package com.example.moodspace;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertEquals;

/**
 * checks with UI Automator if appropriate markers are present on the screen
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapsActivityTest {
    private String username = "Test_MapUITest";
    private String password = "passwordMaps";
    private String sad = new String(Character.toChars(0x1F62D));
    private String happy = new String(Character.toChars(0x1F604));



    @Rule
    public ActivityTestRule<LoginActivity> activityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void mapTest() throws InterruptedException{
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1000);

        ViewInteraction appCompatImageButton = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0)),
                        1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0)),
                        4),
                        isDisplayed()));
        navigationMenuItemView.perform(click());

        // check if sad mood is displayed in my mood history.
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject marker = device.findObject(new UiSelector().descriptionContains(sad));

        try {
            marker.click();
        }
        catch (Exception e){
            // if the marker is not found test should fail
            assertEquals(0,1);
        }

        // check if the happy mood of following is displayed

        onView(withText("FOLLOWING")).perform(click());


        marker = device.findObject(new UiSelector().descriptionContains(happy));

        try {
            marker.click();
        }
        catch (Exception e){
            // if the marker is not found test should fail
            assertEquals(0,1);
        }


    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
