package com.example.moodspace;

import android.os.Bundle;

import java.util.ArrayList;

public class DummyControllerCallback implements ControllerCallback {
    private ArrayList<CallbackId> receivedCallbackIds = new ArrayList<>();
    private ArrayList<Bundle> receivedBundles = new ArrayList<>();

    @Override
    public void callback(CallbackId callbackId) {
        callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        receivedCallbackIds.add(callbackId);
        receivedBundles.add(bundle);
    }
}
