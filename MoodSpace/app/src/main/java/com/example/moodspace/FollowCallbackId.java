package com.example.moodspace;

public enum FollowCallbackId implements CallbackId {
    ADD_FOLLOWER_COMPLETE,
    ADD_FOLLOWER_SUCCESS,
    ADD_USER_TO_FOLLOWING_FAIL,
    ADD_USER_AS_FOLLOWER_FAIL,

    REMOVE_FOLLOWER_COMPLETE,
    REMOVE_FOLLOWER_SUCCESS,
    REMOVE_FROM_FOLLOWING_FAIL,
    REMOVE_AS_FOLLOWER_FAIL,

    ADD_FOLLOW_REQUEST_SUCCESS,
    ADD_FOLLOW_REQUEST_COMPLETE,

    SEND_REQUEST_SUCCESS,
    SEND_REQUEST_FAIL,
    REMOVE_REQUEST_SUCCESS,
    REMOVE_REQUEST_FAIL,
    FOLLOWEE_MOOD_READ_FAIL,
}
