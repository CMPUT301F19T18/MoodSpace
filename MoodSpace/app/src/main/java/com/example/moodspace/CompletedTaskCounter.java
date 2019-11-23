package com.example.moodspace;

public class CompletedTaskCounter {
    private int limit;
    private int complete = 0;
    private int success = 0;

    public CompletedTaskCounter(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("inputted limit should be greater than 0");
        }
        this.limit = limit;
    }

    public static com.example.moodspace.CompletedTaskCounter getDefault() {
        return new com.example.moodspace.CompletedTaskCounter(2);
    }

    public void incrementComplete() {
        if (complete + 1 > limit) {
            throw new IllegalArgumentException("Did not expect to increment complete past limit=" + limit);
        }
        complete++;
    }

    public void incrementSuccess() {
        if (success + 1 > limit) {
            throw new IllegalArgumentException("Did not expect to increment success past limit=" + limit);
        }
        success++;
    }

    public boolean isComplete() {
        return complete == limit;
    }

    public boolean isSuccess() {
        return success == limit;
    }
}
