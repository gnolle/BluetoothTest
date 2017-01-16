package com.example.jkn.bluetoothtest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jan on 16.01.2017.
 */

public class Utils {

    public static String getReadableTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yy - HH:mm", Locale.getDefault());
        return dateFormat.format(new Date());
    }

}
