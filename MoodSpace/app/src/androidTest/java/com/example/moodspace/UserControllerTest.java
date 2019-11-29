package com.example.moodspace;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

import static com.example.moodspace.LoginActivity.SIGN_UP_USER_KEY;
import static com.example.moodspace.UserController.PAPER_PASSWORD_KEY;
import static com.example.moodspace.UserController.PAPER_USERNAME_KEY;
import static com.example.moodspace.Utils.newUserBundle;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserControllerTest {
    DummyUserCallback cc;
    UserController uc;
    private static final int SLEEP_TIME = 6000;
    private static final int LONG_SLEEP_TIME = 10000;
    private static final String user1 = "TEST_UserControllerTest1";
    private static final String user2 = "TEST_UserControllerTest2";
    private static final String user3 = "TEST_UserControllerTest3";
    private static final String user4 = "TEST_UserControllerTest4";
    private static final String user5 = "TEST_UserControllerTest5";
    private static final String[] users = {user1, user2, user3, user4, user5,};
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static Context context;

    @Before
    public void initializeDummy() {
        cc = new DummyUserCallback();
        uc = new UserController(cc);
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void checkUsernameExistsTest() throws InterruptedException{
        User inputtedUser = new User(user1, "password");
        uc.checkUsernameExists(user4, newUserBundle(SIGN_UP_USER_KEY, inputtedUser));

        Thread.sleep(SLEEP_TIME);
        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        // User should not exist
        assertTrue(cc.receivedCallbackIds.contains(UserCallbackId.USERNAME_DOESNT_EXIST));
    }

    @Test
    public void signUpUserTest() throws InterruptedException{
        User newUser = new User(user2, "password");
        uc.signUpUser(newUser);

        Thread.sleep(SLEEP_TIME);
        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        // User sign up should be successful
        assertTrue(cc.receivedCallbackIds.contains(UserCallbackId.LOGIN));

        Bundle bundle = cc.receivedBundles.get(cc.receivedCallbackIds.indexOf(UserCallbackId.LOGIN));
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(LoginActivity.LOGIN_USER_KEY));

        // User should be the same
        assertEquals(newUser, bundle.get(LoginActivity.LOGIN_USER_KEY));

    }

    @Test
    public void getUserDataTest() throws InterruptedException{
        final User newUser = new User(user3, "UserDataPassword");
        uc.signUpUser(newUser);

        // SignUp

            Thread.sleep(SLEEP_TIME);
            // callback should be complete
            assertEquals(cc.receivedCallbackIds.size(), 1);
            // User sign up should be successful
            assertTrue(cc.receivedCallbackIds.contains(UserCallbackId.LOGIN));

            Bundle bundle = cc.receivedBundles.get(cc.receivedCallbackIds.indexOf(UserCallbackId.LOGIN));
            assertNotNull(bundle);
            assertTrue(bundle.containsKey(LoginActivity.LOGIN_USER_KEY));

            // User should be the same
            assertEquals(newUser, bundle.get(LoginActivity.LOGIN_USER_KEY));

        // get user data
        uc.getUserData(newUser.getUsername(), "DATA_FETCH_SUCCESSFUL");
        Thread.sleep(SLEEP_TIME);
        //The user data was right
        assertEquals(cc.callbackId, "DATA_FETCH_SUCCESSFUL");
        assertEquals(newUser.getUsername(), cc.fetchedUserData.get("username"));

    }

    @Test
    public void checkPasswordTest() throws InterruptedException{
        final User newUser = new User(user4, "UserDataPassword");
        uc.signUpUser(newUser);

        // SignUp

        Thread.sleep(SLEEP_TIME);
        // callback should be complete
        assertEquals(cc.receivedCallbackIds.size(), 1);
        // User sign up should be successful
        assertTrue(cc.receivedCallbackIds.contains(UserCallbackId.LOGIN));

        Bundle bundle = cc.receivedBundles.get(cc.receivedCallbackIds.indexOf(UserCallbackId.LOGIN));
        assertNotNull(bundle);
        assertTrue(bundle.containsKey(LoginActivity.LOGIN_USER_KEY));

        // User should be the same
        assertEquals(newUser, bundle.get(LoginActivity.LOGIN_USER_KEY));

        // get user data
        uc.getUserData(newUser.getUsername(), "DATA_FETCH_SUCCESSFUL");
        Thread.sleep(SLEEP_TIME);

        //The user data was right
        assertEquals(cc.callbackId, "DATA_FETCH_SUCCESSFUL");

        // callback should be complete
        uc.checkPassword(newUser, cc.fetchedUserData);
        assertEquals(cc.receivedCallbackIds.size(), 2);

        // Password is right
        assertTrue(cc.receivedCallbackIds.contains(UserCallbackId.LOGIN));

        // Password is wrong
        final User wrongUser = new User(newUser.getUsername(),"WrongPassword");

        // callback should be complete
        uc.checkPassword(wrongUser, cc.fetchedUserData);
        assertEquals(cc.receivedCallbackIds.size(), 3);

        // Password is right
        assertTrue(cc.receivedCallbackIds.contains(UserCallbackId.INCORRECT_PASSWORD));
    }

    @Test
    public void rememberUserTest() throws InterruptedException{
        final User newUser = new User(user5, "UserDataPassword");
        Paper.init(context);
        String storedUsername = Paper.book().read(PAPER_USERNAME_KEY);
        String storedPassword = Paper.book().read(PAPER_PASSWORD_KEY);
        uc.rememberUser(newUser);
        Thread.sleep(SLEEP_TIME);

        String newUsername = Paper.book().read(PAPER_USERNAME_KEY);
        String newPassword = Paper.book().read(PAPER_PASSWORD_KEY);
        assertEquals(newPassword, newUser.getPassword());
        assertEquals(newUsername, newUser.getUsername());
        Paper.book().write(PAPER_USERNAME_KEY, storedUsername);
        Paper.book().write(PAPER_PASSWORD_KEY, storedPassword);


    }

    @AfterClass
    public static void destroyUsers() {
        // TODO stub
        // implement once UserController and AddEditController is better
        for(String u: users){
            db.collection("users").document(u)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("UserControllerTest", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("UserControllerTest", "Error deleting document", e);
                        }
                    });
        }

    }
}
