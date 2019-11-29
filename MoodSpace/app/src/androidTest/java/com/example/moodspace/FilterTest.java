package com.example.moodspace;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Users:
 *  - TEST_FilterTest
 *      - enjoyment
 *      - anger
 *      - can view all
 *
 * TODO tests:
 *  - @BeforeClass: create user, add dummy moods
 *  - do nothing
 *      - check all moods are shown
 *  - filter so nothing shows
 *      - check that nothing shows
 *  - filter so one shows
 *      - check one mood shows
 *  - reset filter
 *      - check all moods are shown
 *  - @AfterClass: remove user, filters and moods
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterTest {
    private String username = "TEST_FilterTest";
    private String password = "TEST_FilterTest";
    private String anger = new String(Character.toChars(0x1F621));
    private String enjoyment = new String(Character.toChars(0x1F604));

    @Rule
    public ActivityTestRule<LoginActivity> activityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void filter() throws InterruptedException {
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1000);


        onView(withId(R.id.moodList))
                .check(matches(hasDescendant(withText(containsString(enjoyment)))));

        onView(withId(R.id.moodList))
                .check(matches((hasDescendant(withText(containsString(anger))))));


        onView(withId(R.id.filter)).perform(click());

        Thread.sleep(1000);

        onData(anything())
                .inAdapterView(Matchers.allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(1).perform(click());

        onView(Matchers.allOf(withId(android.R.id.button1), withText("OK"), childAtPosition(
                childAtPosition(
                        withClassName(is("android.widget.ScrollView")),
                        0),
                3))).perform(scrollTo(), click());

        Thread.sleep(1000);



        onView(withId(R.id.moodList))
                .check(matches(hasDescendant(withText(containsString(enjoyment)))));

        onView(withId(R.id.moodList))
                .check(matches(not(hasDescendant(withText(containsString(anger))))));

        onView(withId(R.id.filter)).perform(click());

        onData(anything())
                .inAdapterView(Matchers.allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(1).perform(click());

        Thread.sleep(1000);

        onView(Matchers.allOf(withId(android.R.id.button1), withText("OK"), childAtPosition(
                childAtPosition(
                        withClassName(is("android.widget.ScrollView")),
                        0),
                3))).perform(scrollTo(), click());

        Thread.sleep(1000);


        onView(withId(R.id.moodList))
                .check(matches(hasDescendant(withText(containsString(enjoyment)))));


        onView(withId(R.id.moodList))
                .check(matches((hasDescendant(withText(containsString(anger))))));
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
