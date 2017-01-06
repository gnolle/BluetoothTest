package com.example.jkn.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String BT_MODULE_NAME = "HC-05";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBtDevice;
    private BtConnectThread mBtConnectThread;
    private BluetoothSocket mBtSocket;
    private BroadcastReceiver mDeviceDiscoveryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initBtAdapter();
        } catch (BtException be) {
            Log.d(TAG, "Could not initialise Bluetooth adapter.");
        }

        initDeviceDiscoveryReceiver();
        registerDeviceDiscoveryReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetConnectThread();

        if (mBtDevice == null) {
            mBtAdapter.startDiscovery();
        } else {
            connect();
        }
    }

    private void resetConnectThread() {
        if (mBtConnectThread != null) {
            mBtConnectThread.cancel();
            mBtConnectThread = null;
        }
    }

    private void connect() {
        mBtConnectThread = new BtConnectThread(mBtDevice, SPP_UUID, new BtConnectThread.BtConnectionCallback() {
            @Override
            public void onSuccess(BluetoothSocket bluetoothSocket) {
                mBtSocket = bluetoothSocket;
                Log.d(TAG, "Connection attempt succeeded.");
            }

            @Override
            public void onFailure(String message) {
                Log.d(TAG, message);
            }
        });

        mBtConnectThread.run();

    }

    private void initBtAdapter() throws BtException {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) {
            throw new BtException();
        }

        if (!mBtAdapter.isEnabled()) {
            requestEnableBt();
        }
    }

    private void requestEnableBt() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Bluetooth enabled.");
                } else {
                    Log.d(TAG, "Bluetooth not enabled.");
                }
        }
    }

    private void initDeviceDiscoveryReceiver() {
        mDeviceDiscoveryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    Log.d(TAG, "Found device. Name: " + deviceName + " address: " + deviceHardwareAddress);
                    if (deviceName.equals(BT_MODULE_NAME)) {
                        Log.d(TAG, "Found Bluetooth device with name " + deviceName + ".");
                        mBtDevice = device;
                        mBtAdapter.cancelDiscovery();
                        connect();
                    }
                }
            }
        };
    }

    private void registerDeviceDiscoveryReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDeviceDiscoveryReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceDiscoveryReceiver);
    }
}
