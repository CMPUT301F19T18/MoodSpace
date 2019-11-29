package com.example.moodspace;

import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.IsNot.not;

/**
 * Users:
 *  - TEST_AddMoodTest
 *
 * TODO tests:
 *  - @BeforeClass: Login
 *  - click on add mood event
 *      - Set emotion spinner to enjoyment
 *      - Set social situation to Alone
 *      - Set Reason to "Alone time"
 *      - Save new mood
 *  - click on add mood event
 *      - Set emotion spinner to anger
 *      - Set social situation to With another person
 *      - Set Reason to "fighting"
 *      - Save new mood
 *  - click on add mood event
 *      - Set emotion spinner to sadness
 *      - Set social situation to With two to several peop
 *      - Set Reason to "Movie night"
 *      - Save new mood
 *  - click on add mood event
 *      - Don't select an emotion this time.
 *      - Try to save
 *  - Click on first mood in the listview and check that it matches the last moods.
 */
public class AddMoodTest {

    private String username;
    private String password;
    private String anger;
    private String enjoyment;
    private String sadness;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule =
            new ActivityTestRule<>(LoginActivity.class);
    @Before
    public void initValidString() {
        // Specify a valid string.
        username = "TEST_AddMoodTest";
        password = "TEST_AddMoodTest";
        anger = new String(Character.toChars(0x1F621));
        enjoyment = new String(Character.toChars(0x1F604));
        sadness = new String(Character.toChars(0x1F62D));
    }

    @Test
    public void addMoods() throws InterruptedException {
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1500);
//         Add Enjoyment Mood with Alone social situation and set reason to alone time.
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.situationText)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("alone time"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(1500);

        // Add Anger Mood with Another person situation and set reason to fighting.
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.situationText)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("fighting"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(1500);

        // Add Sad Mood with two or more people social situation and set reason to movie night.
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.situationText)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("Movie night"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(1500);

        // Do not select a mood
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.saveBtn)).perform(click());
        onView(withText("Select an emotion")).inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).perform(click());

        Thread.sleep(1500);

//        Check that the first mood in the list view is the sad mood, has two to several people as social situation and reason is movie night.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());

        onView(withId(R.id.emotionSelector)).check(matches(withChild(withText(containsString(sadness)))));
        onView(withId(R.id.situationText)).check(matches(withChild(withText(containsString("With two to several people")))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Movie night"))));
    }
}
