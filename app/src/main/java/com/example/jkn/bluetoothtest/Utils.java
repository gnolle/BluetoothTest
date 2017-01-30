package com.example.jkn.bluetoothtest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jan on 16.01.2017.
 */

public class Utils {

    public static String getReadableDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yy - H:mm", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static String getShortTimeFromDate(Date time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
        return dateFormat.format(time);
    }

    public static int getCurrentTimestamp() {
        return (int) (new Date().getTime() / 1000);
    }

}
