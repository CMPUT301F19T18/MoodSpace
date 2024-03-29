package com.example.moodspace;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Users:
 *  TEST_FollowControllerTest1:
 *      - enjoyment (newest)
 *      - sad (oldest)
 *  TEST_FollowControllerTest2
 *      - anger
 *  TEST_FollowControllerTest3
 *  TEST_FollowControllerTest4
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowControllerTest {
    DummyFollowCallback cc;
    FollowController fc;
    private static final int SLEEP_TIME = 6000;
    private static final int LONG_SLEEP_TIME = 10000;
    private static final String user1 = "TEST_FollowControllerTest1";
    private static final String user2 = "TEST_FollowControllerTest2";
    private static final String user3 = "TEST_FollowControllerTest3";
    private static final String user4 = "TEST_FollowControllerTest4";
    private static final String[] users = {user1, user2, user3, user4};

    private static final String FOLLOW_LISTS_LISTENER_KEY1 = "moodspace.FollowControllerTest.followListsListenerKey1";
    private static final String FOLLOW_LISTS_LISTENER_KEY2 = "moodspace.FollowControllerTest.followListsListenerKey2";
    private static final String FOLLOW_LISTS_LISTENER_KEY3 = "moodspace.FollowControllerTest.followListsListenerKey3";
    private static final String FOLLOW_LISTS_LISTENER_KEY4 = "moodspace.FollowControllerTest.followListsListenerKey4";
    private static final String[] FOLLOW_LISTENER_KEYS = {
            FOLLOW_LISTS_LISTENER_KEY1,
            FOLLOW_LISTS_LISTENER_KEY2,
            FOLLOW_LISTS_LISTENER_KEY3,
            FOLLOW_LISTS_LISTENER_KEY4,
    };

    @BeforeClass
    public static void initializeUsers() {
        // TODO stub
        // implement once UserController and MoodController is better

        // NOTE: because this isn't done yet, if tests fail,
        //   arrays might have to be cleared manually in firestore.
    }

    @Before
    public void initializeDummy() {
        cc = new DummyFollowCallback();
        fc = new FollowController(cc);
    }

    /**
     * Ensure that all users have no followers and follow requests
     */
    @Test
    public void testEmptyArrays() throws InterruptedException {
        for (int i = 0; i < users.length; i++) {
            String user = users[i];
            String FOLLOW_LISTS_LISTENER_KEY = FOLLOW_LISTENER_KEYS[i];
            fc.getFollowData(user, FOLLOW_LISTS_LISTENER_KEY);
        }

        Thread.sleep(LONG_SLEEP_TIME);

        for (String user: users) {
            assertTrue(cc.followDataMap.containsKey(user));

            FollowDataStorage data = cc.followDataMap.get(user);
            assertNotNull(data);
            assertEquals(data.followers.size(), 0);
            assertEquals(data.following.size(), 0);
            assertEquals(data.followRequestsFrom.size(), 0);
            assertEquals(data.followRequestsTo.size(), 0);
        }
    }

    /**
     * Test 1 => 2
     * Test 1 =/> 2
     */
    @Test
    public void testAddRemoveFollower() throws InterruptedException {
        int i;
        Bundle bundle;
        FollowDataStorage data;

        // 1 => 2
        fc.addFollower(user1, user2);
        Thread.sleep(SLEEP_TIME);
        fc.getFollowData(user1, FOLLOW_LISTS_LISTENER_KEY1);
        fc.getFollowData(user2, FOLLOW_LISTS_LISTENER_KEY2);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.ADD_FOLLOWER_COMPLETE));

        // callback should be successful
        assertEquals(cc.receivedBundles.size(), 1);
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.ADD_FOLLOWER_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        // user1 should have user2 in following array
        assertTrue(cc.followDataMap.containsKey(user1));
        data = cc.followDataMap.get(user1);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 1);
        assertEquals(data.following.get(0), user2);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);

        // user2 should have user1 in followers array
        assertTrue(cc.followDataMap.containsKey(user2));
        data = cc.followDataMap.get(user2);
        assertNotNull(data);
        assertEquals(data.followers.size(), 1);
        assertEquals(data.followers.get(0), user1);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);


        // 1 =/> 2
        cc.followDataMap.clear();
        cc.receivedCallbackIds.clear();
        cc.receivedBundles.clear();

        fc.removeFollower(user1, user2);
        Thread.sleep(SLEEP_TIME);
        fc.getFollowData(user1, FOLLOW_LISTS_LISTENER_KEY1);
        fc.getFollowData(user2, FOLLOW_LISTS_LISTENER_KEY2);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.REMOVE_FOLLOWER_COMPLETE));

        // callback should be successful
        assertEquals(cc.receivedBundles.size(), 1);
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.REMOVE_FOLLOWER_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        // all arrays should be empty
        assertTrue(cc.followDataMap.containsKey(user1));
        data = cc.followDataMap.get(user1);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);

        assertTrue(cc.followDataMap.containsKey(user2));
        data = cc.followDataMap.get(user2);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);
    }

    /**
     * 3 -> 4
     * 3 -/> 4
     */
    @Test
    public void testAddRemoveFollowRequest() throws InterruptedException {
        int i;
        Bundle bundle;
        FollowDataStorage data;

        // 3 -> 4
        fc.sendFollowRequest(user3, user4);
        Thread.sleep(SLEEP_TIME);
        fc.getFollowData(user3, FOLLOW_LISTS_LISTENER_KEY3);
        fc.getFollowData(user4, FOLLOW_LISTS_LISTENER_KEY4);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE));

        // callback should be successful
        assertEquals(cc.receivedBundles.size(), 1);
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        // user3 should have user4 in its followRequestsTo array
        assertTrue(cc.followDataMap.containsKey(user3));
        data = cc.followDataMap.get(user3);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 1);
        assertEquals(data.followRequestsTo.get(0), user4);

        // user4 should have user3 in its followRequestsFrom array
        assertTrue(cc.followDataMap.containsKey(user4));
        data = cc.followDataMap.get(user4);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 1);
        assertEquals(data.followRequestsFrom.get(0), user3);
        assertEquals(data.followRequestsTo.size(), 0);


        // 1 -/> 2
        cc.followDataMap.clear();
        cc.receivedCallbackIds.clear();
        cc.receivedBundles.clear();

        fc.removeFollowRequest(user3, user4);
        Thread.sleep(SLEEP_TIME);
        fc.getFollowData(user3, FOLLOW_LISTS_LISTENER_KEY1);
        fc.getFollowData(user4, FOLLOW_LISTS_LISTENER_KEY2);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.REMOVE_FOLLOW_REQUEST_COMPLETE));

        // callback should be successful
        assertEquals(cc.receivedBundles.size(), 1);
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.REMOVE_FOLLOW_REQUEST_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        // all arrays should be empty
        assertTrue(cc.followDataMap.containsKey(user3));
        data = cc.followDataMap.get(user3);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);

        assertTrue(cc.followDataMap.containsKey(user4));
        data = cc.followDataMap.get(user4);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);
    }

    /**
     * attempts to 3 =/> 4 even though all arrays are null
     * currently is a SUCCESSFUL result, may change later
     */
    @Test
    public void testRemoveNonexistentFollower() throws InterruptedException {
        FollowDataStorage data;

        // makes sure 3 is not following 4 and 4 is not a follower of 3
        fc.getFollowData(user3, FOLLOW_LISTS_LISTENER_KEY3);
        fc.getFollowData(user4, FOLLOW_LISTS_LISTENER_KEY4);
        Thread.sleep(SLEEP_TIME);

        assertTrue(cc.followDataMap.containsKey(user3));
        data = cc.followDataMap.get(user3);
        assertNotNull(data);
        assertEquals(data.following.size(), 0);

        assertTrue(cc.followDataMap.containsKey(user4));
        data = cc.followDataMap.get(user4);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);

        fc.removeFollower(user3, user4);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
    }

    /**
     * 3 => 3
     * (shouldn't work)
     */
    @Test
    public void testAddFollowerToSelf() throws InterruptedException {
        fc.addFollower(user3, user3);
        Thread.sleep(SLEEP_TIME);
        fc.getFollowData(user3, FOLLOW_LISTS_LISTENER_KEY3);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.ADD_FOLLOWER_COMPLETE));

        // callback should NOT be successful
        assertEquals(cc.receivedBundles.size(), 1);
        int i = cc.receivedCallbackIds.indexOf(FollowCallbackId.ADD_FOLLOWER_COMPLETE);
        Bundle bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertFalse(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        assertTrue(cc.followDataMap.containsKey(user3));
        FollowDataStorage data = cc.followDataMap.get(user3);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);
    }


    /**
     * 4 sends follow request to 2
     * 4 -> 2
     *
     * 2 accepts 4
     * 4 -/> 2
     * 4 => 2
    */
    @Test
    public void testAcceptRequest() throws InterruptedException {
        int i;
        Bundle bundle;

        // 4 -> 2
        fc.sendFollowRequest(user4, user2);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE));

        // callback should be successful
        assertEquals(cc.receivedBundles.size(), 1);
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        cc.receivedCallbackIds.clear();
        cc.receivedBundles.clear();
        cc.followDataMap.clear();

        // 2 accepts 4
        // so 4 => 2
        fc.acceptFollowRequest(user2, user4);
        Thread.sleep(LONG_SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.ACCEPT_FOLLOW_REQUEST_COMPLETE));
        assertEquals(cc.receivedBundles.size(), 1);

        // callback should be successful
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.ACCEPT_FOLLOW_REQUEST_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));

        fc.getFollowData(user2, FOLLOW_LISTS_LISTENER_KEY2);
        fc.getFollowData(user4, FOLLOW_LISTS_LISTENER_KEY4);

        Thread.sleep(LONG_SLEEP_TIME);

        FollowDataStorage data;
        // user4 should have user2 in following array
        assertTrue(cc.followDataMap.containsKey(user4));
        data = cc.followDataMap.get(user4);
        assertNotNull(data);
        assertEquals(data.followers.size(), 0);
        assertEquals(data.following.size(), 1);
        assertEquals(data.following.get(0), user2);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);

        // user2 should have user4 in followers array
        assertTrue(cc.followDataMap.containsKey(user2));
        data = cc.followDataMap.get(user2);
        assertNotNull(data);
        assertEquals(data.followers.size(), 1);
        assertEquals(data.followers.get(0), user4);
        assertEquals(data.following.size(), 0);
        assertEquals(data.followRequestsFrom.size(), 0);
        assertEquals(data.followRequestsTo.size(), 0);


        // removes follower for cleanup
        cc.receivedCallbackIds.clear();
        cc.receivedBundles.clear();
        cc.followDataMap.clear();

        fc.removeFollower(user4, user2);
        Thread.sleep(SLEEP_TIME);

        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        assertTrue(cc.receivedCallbackIds.contains(FollowCallbackId.REMOVE_FOLLOWER_COMPLETE));

        // callback should be successful
        assertEquals(cc.receivedBundles.size(), 1);
        i = cc.receivedCallbackIds.indexOf(FollowCallbackId.REMOVE_FOLLOWER_COMPLETE);
        bundle = cc.receivedBundles.get(i);
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(FollowController.IS_SUCCESSFUL_KEY));
        assertTrue(bundle.getBoolean(FollowController.IS_SUCCESSFUL_KEY));
    }

    /**
     * 4 => 1 (happy)
     * 4 => 2 (angry)
     * 4 => 3 (no moods)
     * order should be newest to oldest (happy, angry)
     */
    @Test
    public void testFollowingMoods() throws InterruptedException {
        fc.addFollower(user4, user1);
        fc.addFollower(user4, user2);
        fc.addFollower(user4, user3);
        Thread.sleep(LONG_SLEEP_TIME);

        // makes sure they all are complete and successful
        assertEquals(cc.receivedCallbackIds.size(), 3);
        for (CallbackId cid: cc.receivedCallbackIds) {
            assertEquals(cid, FollowCallbackId.ADD_FOLLOWER_COMPLETE);
        }
        for (Bundle b: cc.receivedBundles) {
            assertNotNull(b);
            assertTrue(b.containsKey(FollowController.IS_SUCCESSFUL_KEY));
            assertTrue(b.getBoolean(FollowController.IS_SUCCESSFUL_KEY));
        }

        fc.getFollowingMoods(user4);
        Thread.sleep(SLEEP_TIME);

        assertEquals(cc.user, user4);
        assertEquals(cc.followingMoodList.size(), 2);
        assertEquals(cc.followingMoodList.get(0).getEmotion(), Emotion.ENJOYMENT);
        assertEquals(cc.followingMoodList.get(1).getEmotion(), Emotion.ANGER);

        // clean up
        cc.receivedCallbackIds.clear();
        cc.receivedBundles.clear();

        fc.removeFollower(user4, user1);
        fc.removeFollower(user4, user2);
        fc.removeFollower(user4, user3);
        Thread.sleep(LONG_SLEEP_TIME);

        // makes sure they all are complete and successful
        assertEquals(cc.receivedCallbackIds.size(), 3);
        for (CallbackId cid: cc.receivedCallbackIds) {
            assertEquals(cid, FollowCallbackId.REMOVE_FOLLOWER_COMPLETE);
        }
        for (Bundle b: cc.receivedBundles) {
            assertNotNull(b);
            assertTrue(b.containsKey(FollowController.IS_SUCCESSFUL_KEY));
            assertTrue(b.getBoolean(FollowController.IS_SUCCESSFUL_KEY));
        }
    }

    @After
    public void clearDummy() {
        cc.followDataMap.clear();
        cc.followingMoodList.clear();
        cc.user = null;
        CacheListener cacheListener = CacheListener.getInstance();
        cacheListener.removeListener(FOLLOW_LISTS_LISTENER_KEY1);
        cacheListener.removeListener(FOLLOW_LISTS_LISTENER_KEY2);
        cacheListener.removeListener(FOLLOW_LISTS_LISTENER_KEY3);
        cacheListener.removeListener(FOLLOW_LISTS_LISTENER_KEY4);
    }

    @AfterClass
    public static void destroyUsers() {
        // TODO stub
        // implement once UserController and MoodController is better
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

class DummyFollowCallback extends DummyControllerCallback
        implements FollowController.GetDataCallback, FollowController.OtherMoodsCallback {

    HashMap<String, FollowDataStorage> followDataMap = new HashMap<>();
    String user = null;
    List<MoodView> followingMoodList = new ArrayList<>();

    @Override
    public void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                   @NonNull List<String> followers, @NonNull List<String> followRequestsFrom,
                                   @NonNull List<String> followRequestsTo) {
        FollowDataStorage followData
                = new FollowDataStorage(following, followers, followRequestsFrom, followRequestsTo);
        followDataMap.put(user, followData);
    }

    @Override
    public void callbackFollowingMoods(@NonNull String user, @NonNull ArrayList<MoodView> followingMoodsList) {
        this.user = user;
        this.followingMoodList = followingMoodsList;
    }
}
