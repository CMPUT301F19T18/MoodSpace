package com.example.moodspace;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.storage.UploadTask;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.IsNot.not;

public class PictureTest {
    private File file;

    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    public static Matcher<View> noDrawable() {
        return new DrawableMatcher(-1);
    }

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

        file = File.createTempFile("moodspace_picture", ".png");
        FileOutputStream out = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();

        // sets uri into intent
        // https://stackoverflow.com/a/20284270
        Uri uri = convertFileToContentUri(file);
        Intent intent = new Intent();
        intent.setData(uri);

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

    /**
     * checks that views displayed correctly
     */
    @Test
    public void testValidViews() throws InterruptedException {
        onView(withId(R.id.image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.image_button)).check(matches(isDisplayed()));
        onView(withId(R.id.remove_image_button)).check(matches(not(isDisplayed())));

        onView(withId(R.id.image_button)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.image_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.remove_image_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testUpload() throws InterruptedException {
        AddEditController aec = new AddEditController(null);
        UploadTask uploadTask = aec.uploadPhoto(file.getAbsolutePath(), "picture_test");
        Thread.sleep(5000);
        assertTrue(uploadTask.isComplete());
        assertTrue(uploadTask.isSuccessful());
    }

    @After
    public void cleanupFiles() throws IOException {
        Files.delete(file.toPath());
    }
}

// checking if imageview has matching image
// https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f
class DrawableMatcher extends TypeSafeMatcher<View> {
    private final int expectedId;
    private String resourceName;
    private static final int EMPTY = -1;
    private static final int ANY = -2;

    DrawableMatcher(int expectedId) {
        super(View.class);
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(View target) {
        if (!(target instanceof ImageView)) {
            return false;
        }
        ImageView imageView = (ImageView) target;
        if (expectedId == EMPTY) {
            return imageView.getDrawable() == null;
        }
        if (expectedId == ANY) {
            return imageView.getDrawable() != null;
        }
        Resources resources = target.getContext().getResources();
        Drawable expectedDrawable = resources.getDrawable(expectedId);
        resourceName = resources.getResourceEntryName(expectedId);

        if (expectedDrawable == null) {
            return false;
        }

        Bitmap bitmap = getBitmap(imageView.getDrawable());
        Bitmap otherBitmap = getBitmap(expectedDrawable);
        return bitmap.sameAs(otherBitmap);
    }

    private Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with drawable from resource id: ");
        description.appendValue(expectedId);
        if (resourceName != null) {
            description.appendText("[");
            description.appendText(resourceName);
            description.appendText("]");
        }
    }
}
