package com.example.moodspace;

import com.google.firebase.firestore.DocumentSnapshot;

public class DummyUserCallback extends DummyControllerCallback implements UserController.CallbackUser {
    DocumentSnapshot fetchedUserData;
    String callbackId;
    @Override
    public void callbackUserData(DocumentSnapshot fetchedUserData, String callbackId) {
        this.fetchedUserData = fetchedUserData;
        this.callbackId = callbackId;
    }
}
