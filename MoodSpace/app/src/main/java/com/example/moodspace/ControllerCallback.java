package com.example.moodspace;

/**
 * Used to send messages back to the activity from the controller
 *   without having to store the activity inside the controller itself
 */
public interface ControllerCallback {
    void callback(String callbackId);
}
