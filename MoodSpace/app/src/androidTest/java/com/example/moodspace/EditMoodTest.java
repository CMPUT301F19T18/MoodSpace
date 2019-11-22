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
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

public class EditMoodTest {
    private String username;
    private String password;
    private String angry;
    private String enjoyment;
    private String sad;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule =
            new ActivityTestRule<>(LoginActivity.class);
    @Before
    public void initValidString() {
        // Specify a valid string.
        username = "EditTest";
        password = "EditTest";
        angry = new String(Character.toChars(0x1F621));
        enjoyment = new String(Character.toChars(0x1F604));
        sad = new String(Character.toChars(0x1F62D));
    }

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


        // Verify and set mood to Angry, make social situation "With another person" and reason to "Gaming"
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.situationSelector)).check(matches(withSpinnerText(containsString("Alone"))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Quiet"))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(sad)))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(angry)))));

        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.reason_text)).perform(replaceText("Gaming"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        // Verify and set the mood to sad, social situation to "With two to several people" and reason to "Movie night"
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.situationSelector)).check(matches(withSpinnerText(containsString("With another person"))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Gaming"))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(sad)))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(enjoyment)))));

        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.situationSelector)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.reason_text)).perform(replaceText("Movie night"), closeSoftKeyboard());
        onView(withId(R.id.saveBtn)).perform(click());

        // Verify the mood info and go back to main page
        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.situationSelector)).check(matches(withSpinnerText(containsString("With two to several people"))));
        onView(withId(R.id.reason_text)).check(matches(withText(containsString("Movie night"))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(angry)))));
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString(enjoyment)))));
        onView(withId(R.id.backBtn)).perform(click());

    }
}
