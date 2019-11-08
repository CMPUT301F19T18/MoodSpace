package com.example.moodspace;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.IsNot.not;

public class PictureUnitTest {
    File file;


    @Before
    public void createPicture() throws IOException {
        int w = 100;
        int h = 100;

        file = File.createTempFile("moodspace_picture2", ".png");
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);

        FileOutputStream out = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();

        Files.delete(file.toPath());
    }

    @Test
    public void testUpload() throws InterruptedException {
        AddEditController aec = new AddEditController(null);
        UploadTask uploadTask = aec.uploadPhoto(file.getAbsolutePath(), "picture_test");
        Thread.sleep(5000);
        assertTrue(uploadTask.isComplete());
        assertTrue(uploadTask.isSuccessful());
    }
}
