package com.example.moodspace;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Users:
 *  TEST_AddMoodTest (no moods)
 *
 * TODO tests:
 *  - add mood button
 *      - check activity
 *  - add mood button, click submit without selecting mood
 *      - check error
 *      - check activity
 *  - add mood button, select mood, click submit
 *      - check mood exists in firestore
 *      - check mood exists in list
 *      - check mood
 *  - @AfterClass: remove all moods
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddMoodTest {
    private final String username = "TEST_AddMoodTest";
    private final String password = "TEST_AddMoodTest";
    private final String angry = new String(Character.toChars(0x1F621));
    private final String happy = new String(Character.toChars(0x1F604));
    private final String sad = new String(Character.toChars(0x1F62D));

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule =
            new ActivityTestRule<>(LoginActivity.class);
    @Rule
    public ActivityTestRule<AddEditActivity> mActivityRule2 =
            new ActivityTestRule<>(AddEditActivity.class);

    @Test
    public void addMoods() throws InterruptedException {
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(3000);

        // Add Happy Mood with Alone social situation and set reason to alone time.
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("alone time"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(1500);

        // Add Angry Mood with Another person situation and set reason to fighting.
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("fighting"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(1500);

        // Add Sad Mood with two or more people social situation and set reason to movie night.
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("Movie night"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(1500);

        // Do not select a mood
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.saveBtn)).perform(click());
        onView(withText("Select an emotion")).inRoot(withDecorView(not(mActivityRule2.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).perform(click());

        Thread.sleep(1500);

//        Check that the first mood in the list view is the sad mood, has two to several people as social situation and reason is movie night.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(angry)))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(happy)))));
        onView(withId(R.id.situationSelector)).check(matches(withSpinnerText(containsString("With two to several people"))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Movie night"))));

    }
}
