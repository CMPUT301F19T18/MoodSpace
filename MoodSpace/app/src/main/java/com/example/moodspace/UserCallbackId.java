package com.example.moodspace;

public enum UserCallbackId implements CallbackId {
    USERNAME_TAKEN,
    USERNAME_NOT_TAKEN,
    USER_NONEXISTENT,
    LOGIN,
    LOGIN_READ_FAIL,
    INCORRECT_PASSWORD,
    USER_TASK_NULL,
    PASSWORD_FETCH_NULL,
    USER_ADDITION_FAIL,
    FILTER_INITIALIZE_FAIL,
}
