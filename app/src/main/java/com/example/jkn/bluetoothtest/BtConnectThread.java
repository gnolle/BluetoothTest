package com.example.jkn.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Class that connects to a given Bluetooth device.
 * Created by jkn on 06.01.17.
 */

class BtConnectThread extends Thread {

    private static final String TAG = BtConnectThread.class.getSimpleName();
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private UUID mUuid;
    private BtConnectionCallback mCallback;

    BtConnectThread(BluetoothDevice btDevice, UUID uuid, BtConnectionCallback connectionCallback) {
        mDevice = btDevice;
        mUuid = uuid;
        mCallback = connectionCallback;
    }

    public void run() {
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(mUuid);
            mSocket.connect();
        } catch (IOException connectException) {
            Log.e(TAG, "Could not create socket.", connectException);
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            mCallback.onFailure("Could not connect to socket.");
            return;
        }

        // The connection attempt succeeded
        mCallback.onSuccess(mSocket);
    }

    void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    interface BtConnectionCallback {
        void onSuccess(BluetoothSocket bluetoothSocket);

        void onFailure(String message);
    }
}
