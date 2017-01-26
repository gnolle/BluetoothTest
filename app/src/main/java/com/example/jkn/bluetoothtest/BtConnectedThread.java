package com.example.jkn.bluetoothtest;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by Jan on 06.01.2017.
 */

class BtConnectedThread extends Thread {

    private static final String TAG = BtConnectedThread.class.getSimpleName();
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final int MAX_RESPONSE_LENGTH = 128;

    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private BtResponseListener mResponseListener;

    BtConnectedThread(BluetoothSocket socket, BtResponseListener responseListener) {
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
        byte[] buffer = new byte[MAX_RESPONSE_LENGTH];
        int pos = 0;
        int serialByte;

        while (true) {
            try {
                serialByte = mInStream.read();
                if (serialByte > 0) {
                    switch (serialByte) {
                        case '\n':
                            break;
                        case '\r':
                            handleResponse(buffer, pos);
                            pos = 0;
                            break;
                        default:
                        if (pos < MAX_RESPONSE_LENGTH - 1) {
                            buffer[pos] = (byte) serialByte;
                            pos++;
                        }
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    private void handleResponse(byte[] response, int length) {
        mResponseListener.handleBtResponse(new String(response, 0, length, UTF8_CHARSET));
    }

    void write(String message) {
        try {
            message += '\r';
            mOutStream.write(message.getBytes(UTF8_CHARSET));
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

    void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    interface BtResponseListener {
        void handleBtResponse(String response);
    }

}