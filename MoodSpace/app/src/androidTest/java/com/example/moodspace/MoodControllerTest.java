package com.example.moodspace;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Users:
 *  TEST_MoodControllerTest:
 *  - moods:
 *      - anger
 *      - sadness
 *      - enjoyment (has everything)
 *
 * TODO tests:
 *  - getMoodList
 *      - make sure moods match
 *  - addMood/deleteMood
 *      - add surprise
 *      - make sure callbackID matches
 *      - make sure enjoyment is in the mood list
 *      - remove surprise
 *      - make sure callbackID
 *      - make sure enjoyment is not in the mood list
 *  - updateMood
 *      - update anger to disgust
 *      - change all other fields other than photo
 *      - make sure callbackID matches
 *      - change back
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodControllerTest {
    DummyMoodCallback cc;
    MoodController mc;
    private static final int SLEEP_TIME = 6000;
    private static final int LONG_SLEEP_TIME = 10000;
    private static final String user = "TEST_MoodControllerTest";
}




class DummyMoodCallback extends DummyControllerCallback
        implements MoodController.UserMoodsCallback {
    String user = null;
    List<Mood> moodList = new ArrayList<>();

    @Override
    public void callbackMoodList(@NonNull String user, @NonNull List<Mood> userMoodList) {
        this.user = user;
        this.moodList = userMoodList;

    }
}
