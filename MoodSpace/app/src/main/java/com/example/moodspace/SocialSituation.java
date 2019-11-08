package com.example.moodspace;

public enum SocialSituation {
    NOT_PROVIDED("Not Provided"),
    ALONE("Alone"),
    WITH_ONE("With another person"),
    WITH_TWO_TO_SEVERAL("With two to several people"),
    WITH_CROWD("With a crowd");

    private String SocialSit;

    SocialSituation(String SocialSit) {
        this.SocialSit =  SocialSit;
    }

    @Override
    public String toString(){
        return SocialSit;
    }

}
