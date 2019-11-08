package com.example.moodspace;

import org.junit.*;
import static org.junit.Assert.*;

public class SocialSituationTests {

    @Test
    public void ChecktoString(){
        SocialSituation s = SocialSituation.ALONE;
        assertEquals("Alone", s.toString());

        s = SocialSituation.NOT_PROVIDED;
        assertEquals("Not Provided", s.toString());

        s = SocialSituation.WITH_ONE;
        assertEquals("With another person", s.toString());

        s = SocialSituation.WITH_TWO_TO_SEVERAL;
        assertEquals("With two to several people", s.toString());

        s = SocialSituation.WITH_CROWD;
        assertEquals("With a crowd", s.toString());
    }
}
