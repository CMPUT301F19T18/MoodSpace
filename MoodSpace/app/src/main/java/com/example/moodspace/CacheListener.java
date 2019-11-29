package com.example.moodspace;

import android.util.Log;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;

// thread safe singleton
// https://www.journaldev.com/171/thread-safety-in-java-singleton-classes-with-example-code

/**
 * ensures that all snapshot listeners are unique
 */
public class CacheListener {

    private static volatile CacheListener instance;
    private static final Object mutex = new Object();
    private static final String TAG = CacheListener.class.getSimpleName();
    private final HashMap<String, ListenerRegistration> listeners = new HashMap<>();

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

    /**
     * sets a listener for the moodlist and ensures there is only one for the activity
     *
     * @param key the activity class name
     * @param registration the snapshot listener
     */
    public void setListener(String key, ListenerRegistration registration) {
        removeListener(key);
        Log.d(TAG, "setting registration for key: " + key);
        listeners.put(key, registration);
    }

    public void removeListener(String key) {
        if (listeners.containsKey(key)) {
            ListenerRegistration oldRegistration = listeners.remove(key);
            if (oldRegistration != null) {
                Log.d(TAG, "removing registration for key: " + key);
                oldRegistration.remove();
            }
        }
    }
}
