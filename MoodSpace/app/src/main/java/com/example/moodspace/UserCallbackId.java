package com.example.moodspace;

public enum UserCallbackId implements CallbackId {
    USERNAME_TAKEN,
    USERNAME_NOT_TAKEN,
    USER_NONEXISTENT,
    LOGIN,
    INCORRECT_PASSWORD,

    USER_READ_DATA_FAIL,
    USER_TASK_NULL,
    PASSWORD_FETCH_NULL,
    USER_ADDITION_FAIL,
    FILTER_INITIALIZE_FAIL,
}
