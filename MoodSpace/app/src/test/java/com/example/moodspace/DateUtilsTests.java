package com.example.moodspace;

import org.junit.*;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;


public class DateUtilsTests {

    @Test
    public void CheckFormatDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 10, 18, 0, 0, 0);
        Date date1 = calendar.getTime();
        String date1_string = DateUtils.formatDate(date1);
        assertEquals("2016-11-18", date1_string);

    }

    @Test
    public void CheckFormatTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 10, 18, 19, 32, 6);
        Date date1 = calendar.getTime();
        String date1_string = DateUtils.formatTime(date1);
        assertEquals("19:32", date1_string);

    }


}
