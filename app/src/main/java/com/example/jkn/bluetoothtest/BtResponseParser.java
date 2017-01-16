package com.example.jkn.bluetoothtest;

/**
 * Created by jkn on 16.01.17.
 */

public class BtResponseParser {

    public static BtResponse parseResponse(String response) throws BtException {

        if (response.length() < 3) {
            throw new BtException();
        }

        String prefix = response.substring(0, 3);

        BtResponseType responseType = BtResponseType.typeForPrefix(prefix);

        if (responseType == null) {
            throw new BtException();
        }

        return BtResponseFactory.createResponse(responseType, response);
    }

}
