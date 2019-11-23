package com.example.moodspace;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileListActivityTest {
    private String username;
    private String password;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void setup() {
        username = "Person";
        password = "password";
    }

    @Test
    public void testDelete() throws InterruptedException{
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.saveBtn)).perform(click());

        onView(withId(R.id.addMoodButton)).perform(click());
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.saveBtn)).perform(click());

        onData(anything()).atPosition(1).perform(longClick());
        onView(withText("Delete")).perform(click());

        onData(anything())
                .inAdapterView(allOf(withId(R.id.moodList), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.emotionSelector)).check(matches(not(withSpinnerText(containsString("angry")))));

    }

}

