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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ListActivityTest {
    private String username;
    private String password;
    private String happy;
    private String angry;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);
    private LoginActivity activity;

    @Before
    public void setup() {
        activity = activityRule.getActivity();
        username = "Person";
        password = "password";
        happy = new String(Character.toChars(0x1F604));
        angry = new String(Character.toChars(0x1F621));

    }

    @Test
    public void testDelete() throws InterruptedException{
        onView(withId(R.id.signup_link)).perform(click());
        onView(withId(R.id.username)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.login_btn)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.addMoodButton)).perform(click());
//        onView(withId(R.id.emotionSelector)).perform(click());
//        onData(anything()).atPosition(1).perform(click());
//        onView(withId(R.id.saveBtn)).perform(click());
//
//        onView(withId(R.id.addMoodButton)).perform(click());
//        onView(withId(R.id.emotionSelector)).perform(click());
//        onData(anything()).atPosition(2).perform(click());
//        onView(withId(R.id.saveBtn)).perform(click());
//
//        onData(anything()).atPosition(1).perform(longClick());
//        onView(withId(R.menu.menu_list)).perform(click());

//        onView(withId(R.id.moodList)).check(matches(not(hasDescendant(withText("angry")))));



    }
}
