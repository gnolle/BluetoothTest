package com.example.jkn.bluetoothtest;

import android.provider.Settings;
import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testTimestamp_works() throws Exception {
        System.out.println("Timestamp (sec): " +  String.valueOf(Utils.getCurrentTimestamp()));
    }
    @Test
    public void testSetTimeCommand() throws Exception {
        System.out.println("Command: " +  String.format(BtCommands.SET_TIME, Utils.getCurrentTimestamp()));
    }
}