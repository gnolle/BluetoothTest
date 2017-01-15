package com.example.jkn.bluetoothtest;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Created by jkn on 06.01.17.
 */

public enum BtConnectionStatus {
    NOT_CONNECTED,
    DISCOVERING,
    CONNECTING,
    CONNECTED;

    public static Drawable getDrawableForStatus(Context context, BtConnectionStatus connectionStatus) {
        int drawable = 0;
        switch (connectionStatus) {
            case NOT_CONNECTED:
                drawable = R.drawable.ic_bluetooth_disconnect;
                break;
            case DISCOVERING:
            case CONNECTING:
                drawable = R.drawable.ic_bluetooth_connecting;
                break;
            case CONNECTED:
                drawable = R.drawable.ic_bluetooth_connected;
                break;
        }
        return ContextCompat.getDrawable(context, drawable);
    }

    public static String getTextForStatus(BtConnectionStatus connectionStatus) {
        String statusText = "";
        switch (connectionStatus) {
            case NOT_CONNECTED:
                statusText = "disconnected";
                break;
            case DISCOVERING:
                statusText = "searching";
                break;
            case CONNECTING:
                statusText = "connecting";
                break;
            case CONNECTED:
                statusText = "connected";
                break;
        }
        return statusText;
    }
}
