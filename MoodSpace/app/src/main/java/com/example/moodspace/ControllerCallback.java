package com.example.moodspace;

import android.os.Bundle;

/**
 * Used to send messages back to the activity from the controller
 *   without having to store the activity inside the controller itself
 */
public interface ControllerCallback {
    void callback(final CallbackId callbackId);
    void callback(final CallbackId callbackId, final Bundle bundle);
}
