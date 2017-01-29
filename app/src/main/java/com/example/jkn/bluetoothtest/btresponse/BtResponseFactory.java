package com.example.jkn.bluetoothtest.btresponse;

import android.util.Log;

import java.util.Date;

/**
 * Created by jkn on 16.01.17.
 */

class BtResponseFactory {

    private static final String TAG = BtResponseFactory.class.getSimpleName();

    static BtResponse createResponse(BtResponseType responseType, String response) {

        switch (responseType) {
            case TEMPERATURE:
                return createTemperatureResponse(response);
            case TIME:
                return createTimeResponse(response);
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

    private static BtTimeResponse createTimeResponse(String response) {
        try {
            Date time = new Date(Long.valueOf(response.substring(3, response.length())) * 1000);
            return new BtTimeResponse(time);
        } catch (NumberFormatException e) {
            Log.d(TAG, "Wrong time format", e);
        }
        return null;
    }
}
