package com.example.jkn.bluetoothtest;

import android.util.Log;

/**
 * Created by jkn on 16.01.17.
 */

public class BtResponseFactory {

    private static final String TAG = BtResponseFactory.class.getSimpleName();

    public static BtResponse createResponse(BtResponseType responseType, String response) {

        switch (responseType) {
            case TEMPERATURE:
                return createTemperatureResponse(response);
        }

        return null;
    }

    private static BtTemperatureResponse createTemperatureResponse(String response) {
        try {
            float temp = Float.valueOf(response.substring(3, response.length()));
            return new BtTemperatureResponse(temp);
        } catch (NumberFormatException e) {
            Log.d(TAG, "Wrong temperature format", e);
        }
        return null;
    }
}
