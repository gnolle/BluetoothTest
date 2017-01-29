package com.example.jkn.bluetoothtest.btresponse;

/**
 * Created by jkn on 16.01.17.
 */

public enum BtResponseType {

    TEMPERATURE("TMP"),
    TIME("TIM");

    private String mPrefix;

    BtResponseType(String prefix) {
        mPrefix = prefix;
    }

    public static BtResponseType typeForPrefix(String prefix) {
        for (BtResponseType responseType : BtResponseType.values()) {
            if (responseType.mPrefix.equals(prefix)) {
                return responseType;
            }
        }
        return null;
    }

}
