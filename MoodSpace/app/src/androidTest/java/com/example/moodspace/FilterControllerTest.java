package com.example.moodspace;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.runner.RunWith;

import java.util.HashSet;

/**
 * Only tests that filters work properly with firestore
 * - FilterController does not control how the filters are used, that's up to ProfileListActivity
 *
 * Users:
 *  TEST_FilterControllerTest:
 *  - filters:
 *      - enjoyment
 *      - fear
 *
 * TODO tests:
 *  - getFilters
 *      - make sure moods match
 *  - filterAdd
 *      - add contempt
 *      - make sure callbackID matches
 *      - make sure contempt is in the filter list
 *  - filterRemove
 *      - remove fear
 *      - make sure callbackID matches
 *      - make sure fear is not in the filter list
 *  - updateFilters
 *      - remove enjoyment and add disgust
 *      - make sure callbackID (complete) matches
 *      - make sure filter match (disgust, contempt)
 *  - cleanup:
 *      - remove disgust, contempt
 *      - remove enjoyment, fear
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterControllerTest {
    DummyFilterCallback cc;
    FilterController ftc;
    private static final int SLEEP_TIME = 6000;
    private static final int LONG_SLEEP_TIME = 10000;
    private static final String user = "TEST_FilterControllerTest";


}


class DummyFilterCallback extends DummyControllerCallback
        implements FilterController.GetFiltersCallback {
    String user = null;
    HashSet<String> filters = new HashSet<>();

    @Override
    public void callbackFilters(@NonNull String user, @NonNull HashSet<String> filters) {
        this.user = user;
        this.filters = filters;
    }
}

