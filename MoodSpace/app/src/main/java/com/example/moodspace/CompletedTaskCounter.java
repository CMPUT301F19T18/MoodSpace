package com.example.moodspace;

public class CompletedTaskCounter {
    private int limit;
    private int completedTasks = 0;
    private int successfulTasks = 0;

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
        if (completedTasks + 1 > limit) {
            throw new IllegalArgumentException("Did not expect to increment complete past limit=" + limit);
        }
        completedTasks++;
    }

    public void incrementSuccess() {
        if (successfulTasks + 1 > limit) {
            throw new IllegalArgumentException("Did not expect to increment success past limit=" + limit);
        }
        successfulTasks++;
    }

    public void reset() {
        this.completedTasks = this.successfulTasks = 0;
    }

    public int getCompletedTasks() {
        return this.completedTasks;
    }

    public int getSuccessfulTasks() {
        return this.successfulTasks;
    }

    public boolean isComplete() {
        return completedTasks == limit;
    }

    public boolean isSuccess() {
        return successfulTasks == limit;
    }
}
