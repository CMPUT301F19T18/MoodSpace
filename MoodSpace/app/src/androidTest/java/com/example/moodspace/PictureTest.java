package com.example.moodspace;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;

public class PictureTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public IntentsTestRule<AddEditActivity> intentsTestRule =
            new IntentsTestRule<>(AddEditActivity.class);

    // https://stackoverflow.com/a/27625536
    private static Uri convertFileToContentUri(File file) throws FileNotFoundException {
        //Uri localImageUri = Uri.fromFile(localImageFile); // Not suitable as it's not a content Uri
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContentResolver cr = context.getContentResolver();
        String imagePath = file.getAbsolutePath();
        String imageName = null;
        String imageDescription = null;
        String uriString = MediaStore.Images.Media.insertImage(cr, imagePath, imageName, imageDescription);
        return Uri.parse(uriString);
    }

    @Before
    public void stubAllExternalIntents() throws IOException {
        int w = 100;
        int h = 100;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);

        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        File file = File.createTempFile("moodspace_picture", ".png");
        Log.d("WTF", file.toPath().toString());

        FileOutputStream out = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();


        // sets uri into intent
        // https://stackoverflow.com/a/20284270
        Uri uri = convertFileToContentUri(file);
        Intent intent = new Intent();
        intent.setData(uri);

        Files.delete(file.toPath());
        intending(not(isInternal()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, intent));
    }

    public void addMood() {
        onView(withId(R.id.emotionSelector)).perform(click());
        onData(anything()).atPosition(1).perform(click());
    }

    @Test
    public void testValidIntent() throws InterruptedException {
        onView(withId(R.id.image_button)).perform(click());
        Thread.sleep(1000);
        intended(hasAction(Intent.ACTION_CHOOSER));
    }

    @Test
    public void testValidViews() throws InterruptedException {
        // checks that views displayed correctly
        onView(withId(R.id.image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.image_button)).check(matches(isDisplayed()));
        onView(withId(R.id.remove_image_button)).check(matches(not(isDisplayed())));

        onView(withId(R.id.image_button)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.image_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.remove_image_button)).check(matches(isDisplayed()));

        // TODO test imageview has image
        // https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f
    }

    @Test
    public void testValidUri() {

    }
}
