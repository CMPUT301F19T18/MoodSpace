package com.example.moodspace;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO:
 *  - test add/remove for all
 *  - test remove user without being in array
 */
public class FollowControllerTest {
    DummyControllerCallback cc = new DummyControllerCallback();
    FollowController fc = new FollowController(cc);

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

    @Test
    public void testAdd() {
    }

    @AfterClass
    public static void destroyUsers() {
        // TODO stub
        // implement once UserController and AddEditController is better
    }
}
