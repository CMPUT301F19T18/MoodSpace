package com.example.moodspace;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * TODO:
 *  - test add/remove for all
 *  - test remove user without being in array
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowControllerTest {
    DummyFollowCallback cc = new DummyFollowCallback();
    FollowController fc = new FollowController(cc);
    private static final String user1 = "TEST_FollowControllerTest1";
    private static final String user2 = "TEST_FollowControllerTest2";
    private static final String user3 = "TEST_FollowControllerTest3";
    private static final String user4 = "TEST_FollowControllerTest4";
    private static final String[] users = {user1, user2, user3, user4};

    /**
     * Initializing users:
     *  TEST_FollowControllerTest1:
     *      - happy
     *      - sad
     *  TEST_FollowControllerTest2
     *      - angry
     *  TEST_FollowControllerTest3
     *  TEST_FollowControllerTest4
     */
    @BeforeClass
    public static void initializeUsers() {
        // TODO stub
        // implement once UserController and AddEditController is better
    }

    /**
     * Ensure that all users have no followers and follow requests
     */
    @Test
    public void testEmptyArrays() throws InterruptedException {
        for (String user: users) {
            fc.getFollowData(user);
        }

        Thread.sleep(3000);

        for (String user: users) {
            fc.getFollowData(user);
            assertTrue(cc.followDataMap.containsKey(user));

            FollowDataStorage data = cc.followDataMap.get(user);
            assertNotNull(data);
            assertEquals(data.followers.size(), 0);
            assertEquals(data.following.size(), 0);
            assertEquals(data.followRequestsFrom.size(), 0);
            assertEquals(data.followRequestsTo.size(), 0);
        }
    }


    /*
     * Test 1 => 2
    @Test
    public void testAddFollower() {
        fc.addFollower(user1, user2);
    }
     */

    @AfterClass
    public static void destroyUsers() {
        // TODO stub
        // implement once UserController and AddEditController is better
    }
}

class FollowDataStorage {
    List<String> following;
    List<String> followers;
    List<String> followRequestsFrom;
    List<String> followRequestsTo;

    public FollowDataStorage(List<String> following, List<String> followers,
                             List<String> followRequestsFrom, List<String> followRequestsTo) {
        this.following = following;
        this.followers = followers;
        this.followRequestsFrom = followRequestsFrom;
        this.followRequestsTo = followRequestsTo;
    }
}

class DummyFollowCallback extends DummyControllerCallback implements FollowController.Callback {

    HashMap<String, FollowDataStorage> followDataMap = new HashMap<>();
    String user = null;
    List<MoodOther> followingMoodList = null;

    @Override
    public void callbackFollowingMoods(@NonNull String user, @NonNull List<MoodOther> followingMoodsList) {
        this.user = user;
        this.followingMoodList = followingMoodsList;
    }

    @Override
    public void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                   @NonNull List<String> followers, @NonNull List<String> followRequestsFrom,
                                   @NonNull List<String> followRequestsTo) {
        FollowDataStorage followData
                = new FollowDataStorage(following, followers, followRequestsFrom, followRequestsTo);
        followDataMap.put(user, followData);
    }
}
