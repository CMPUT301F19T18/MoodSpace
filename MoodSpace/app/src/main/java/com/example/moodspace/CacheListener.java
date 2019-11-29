package com.example.moodspace;

import com.google.firebase.firestore.FirebaseFirestore;

// thread safe singleton
// https://www.journaldev.com/171/thread-safety-in-java-singleton-classes-with-example-code
public class CacheListener {

    private static volatile CacheListener instance;
    private static final Object mutex = new Object();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CacheListener() {}

    public static CacheListener getInstance() {
        CacheListener result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new CacheListener();
            }
        }
        return result;
    }
}
