/**
 * @author Manu Parashar
 * This file contains the unit tests for SocialSituation enum
 * @see com.example.moodspace.SocialSituation
 */
package com.example.moodspace;

import org.junit.*;
import static org.junit.Assert.*;

public class SocialSituationTests {

    /**
     * Checks if the toString method in SocialSituation enum works appropriately
     */
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
