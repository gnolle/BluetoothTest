package com.example.jkn.bluetoothtest.btresponse;


import java.util.Date;

/**
 * Created by jkn on 16.01.17.
 */

public class BtTimeResponse extends BtResponse {

    private Date mTime;

    public BtTimeResponse(Date time) {
        responseType = BtResponseType.TIME;
        mTime = time;
    }

    public Date getTime() {
        return mTime;
    }

}
