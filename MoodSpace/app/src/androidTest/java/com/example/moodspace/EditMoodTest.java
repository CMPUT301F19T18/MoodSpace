package com.example.moodspace;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Users:
 *  - TEST_EditMoodTest (no moods)
 *
 * TODO tests:
 *  - @BeforeClass: add dummy mood
 *  - click on mood event
 *      - check activity
 *      - check all fields match
 *  - click on mood event, edit mood
 *      - check all fields match after editing
 *  - click on mood event, edit mood, click cancel, click on mood event
 *      - check all fields remain the same
 *  - click on mood event, edit mood, click okay, click on mood event
 *      - check all fields are different
 *  - @AfterClass: remove dummy mood
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditMoodTest {
    private final String username = "TEST_EditMoodTest";
    private final String password = "TEST_EditMoodTest";
    private final String anger = new String(Character.toChars(0x1F621));
    private final String enjoyment = new String(Character.toChars(0x1F604));
    private final String sadness = new String(Character.toChars(0x1F62D));
    private final String fear = new String(Character.toChars(0x1F631));
    private final String surprise = new String(Character.toChars(0x1F62E));
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void editMoods() throws InterruptedException {
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(3000);

        // Add Enjoyment Mood make social situation as "Alone" and set reason to "Quiet"
        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.reason_text)).perform(typeText("Quiet"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(250);

        // Verify and set mood to Anger, make social situation "With another person" and reason to "Gaming"
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        // onData(withId(R.id.moodList)).check(matches(withChild(withText(containsString(enjoyment))))).perform(click());
        onView(withId(R.id.situationSelector)).check(matches((withChild(withText(containsString("Alone"))))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Quiet"))));
        onView(withId(R.id.emotionSelector)).check(matches(withChild(withText(containsString(enjoyment)))));

        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.reason_text)).perform(replaceText("Gaming"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(250);

        // Verify and set the mood to fear, social situation to "With two to several people" and reason to "Movie night"
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.situationSelector)).check(matches(withChild(withText(containsString("With another person")))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Gaming"))));
        onView(withId(R.id.emotionSelector)).check(matches(withChild(withText(containsString(anger)))));

        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(5).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.reason_text)).perform(replaceText("Movie night"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(250);

        // Verify the mood info, change the fields and press back button.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.situationSelector)).check(matches(withChild(withText(containsString("With two to several people")))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Movie night"))));
        onView(withId(R.id.emotionSelector)).check(matches(withChild(withText(containsString(fear)))));

        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(4).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.reason_text)).perform(replaceText("Feeling ill"), closeSoftKeyboard());
        onView(withId(R.id.backBtn)).perform(click());

        Thread.sleep(1500);

        // Verify that the data remained the same, then change the fields this time.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.situationSelector)).check(matches(withChild((withText(containsString("With two to several people"))))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Movie night"))));
        onView(withId(R.id.emotionSelector)).check(matches(withChild(withText(containsString(fear)))));

        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(7).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(4).perform(click());
        onView(withId(R.id.reason_text)).perform(replaceText("Spring Festival"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        Thread.sleep(250);

        // Verify for the final time that the changes have been saved, then delete the mood.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());

        Thread.sleep(250);

        onView(withId(R.id.situationSelector)).check(matches(withChild(withText(containsString("With a crowd")))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Spring Festival"))));
        onView(withId(R.id.emotionSelector)).check(matches(withChild(withText(containsString(surprise)))));
        onView(withId(R.id.backBtn)).perform(click());
        Thread.sleep(250);

        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(longClick());
        onView(withText("Delete")).perform(click());
        Thread.sleep(1500);
    }
}
