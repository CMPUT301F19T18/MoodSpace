package com.example.moodspace;

public enum SocialSituation {
    NOT_PROVIDED(0, "Not Provided"),
    ALONE(1, "Alone"),
    WITH_ONE(2, "With another person"),
    WITH_TWO_TO_SEVERAL(3, "With two to several people"),
    WITH_CROWD(4, "With a crowd");

    private final int id;
    private final String description;

    SocialSituation(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}

