package com.example.jkn.bluetoothtest;

/**
 * Created by jkn on 16.01.17.
 */

public class BtTemperatureResponse extends BtResponse {

    private float mTemperature;

    public BtTemperatureResponse(float temperature) {
        responseType = BtResponseType.TEMPERATURE;
        mTemperature = temperature;
    }

    public float getTemperature() {
        return mTemperature;
    }

}
