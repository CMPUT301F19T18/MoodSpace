/**
 * @author Manu Parashar
 * This file contains unit tests for Utils class
 * @see com.example.moodspace.Utils
 *
 */

package com.example.moodspace;

import org.junit.*;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;


public class UtilsTest {
    /**
     * Checks if the formatDate static function works appropriately
     */
    @Test
    public void CheckFormatDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 10, 18, 0, 0, 0);
        Date date1 = calendar.getTime();
        String date1_string = Utils.formatDate(date1);
        assertEquals("2016-11-18", date1_string);

    }

    /**
     * Checks if the formatTime static function works appropriately
     */
    @Test
    public void CheckFormatTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 10, 18, 19, 32, 6);
        Date date1 = calendar.getTime();
        String date1_string = Utils.formatTime(date1);
        assertEquals("19:32", date1_string);

    }


}
