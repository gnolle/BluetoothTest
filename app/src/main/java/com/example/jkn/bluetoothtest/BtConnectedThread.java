package com.example.jkn.bluetoothtest;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jan on 06.01.2017.
 */

class BtConnectedThread extends Thread {

    private static final String TAG = BtConnectedThread.class.getSimpleName();
    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private byte[] mBuffer;
    private BtResponseListener mResponseListener;

    public BtConnectedThread(BluetoothSocket socket, BtResponseListener responseListener) {
        mSocket = socket;
        mResponseListener = responseListener;

        try {
            mInStream = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            mOutStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
    }

    public void run() {
        mBuffer = new byte[1024];
        int numBytes;

        while (true) {
            try {
                numBytes = mInStream.read(mBuffer);
                // Send the obtained bytes to the UI activity.
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            mOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    interface BtResponseListener {
        void handleBtResponse(byte[] response);
    }

}