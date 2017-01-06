package com.example.jkn.bluetoothtest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Activity to connect to HC-05 Bluetooth module.
 */
public class MainActivity extends AppCompatActivity implements BtConnectThread.BtConnectionCallback, BtConnectedThread.BtResponseListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String BT_MODULE_NAME = "HC-05";
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_CHECK = 2;

    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBtDevice;
    private BtConnectThread mBtConnectThread;
    private BtConnectedThread mBtConnectedThread;
    private BluetoothSocket mBtSocket;
    private BroadcastReceiver mDeviceDiscoveryReceiver;
    private BtConnectionStatus mBtConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDeviceDiscoveryReceiver();
        registerDeviceDiscoveryReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setConnectionStatus(BtConnectionStatus.NOT_CONNECTED);

        try {
            initBtAdapter();
        } catch (BtException be) {
            Log.d(TAG, "Could not initialise Bluetooth adapter.");
            return;
        }

        if (mBtDevice == null) {
            setConnectionStatus(BtConnectionStatus.DISCOVERING);
            if (checkLocationPermission()) {
                mBtAdapter.startDiscovery();
            } else {
                requestForPermissions();
            }

        } else {
            setConnectionStatus(BtConnectionStatus.CONNECTING);
            connect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CHECK: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted.");
                    mBtAdapter.startDiscovery();
                } else {
                    finish();
                }
            }
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION_CHECK);
    }

    private void setConnectionStatus(BtConnectionStatus status) {
        Log.d(TAG, "Connection status: " + mBtConnectionStatus + " -> " + status);
        mBtConnectionStatus = status;
    }

    private void killConnectThread() {
        if (mBtConnectThread != null) {
            mBtConnectThread.cancel();
            mBtConnectThread = null;
        }
    }

    private void connect() {
        mBtConnectThread = new BtConnectThread(
                mBtDevice,
                SPP_UUID,
                this);

        mBtConnectThread.start();
    }

    @Override
    public void onConnectionSuccess(BluetoothSocket bluetoothSocket) {
        mBtSocket = bluetoothSocket;
        setConnectionStatus(BtConnectionStatus.CONNECTED);
        Log.d(TAG, "Connection attempt succeeded.");
        setUpConnectedThread();
    }

    @Override
    public void onConnectionFailure(String message) {
        setConnectionStatus(BtConnectionStatus.NOT_CONNECTED);
        Log.d(TAG, message);
    }

    private void setUpConnectedThread() {
        mBtConnectedThread = new BtConnectedThread(mBtSocket, this);
        mBtConnectedThread.start();
    }

    @Override
    public void handleBtResponse(byte[] response) {
        Log.d(TAG, "Response: " + new String(response, UTF8_CHARSET));
    }

    private void writeBtMessage(String message) {
        if (mBtConnectedThread != null) {
            mBtConnectedThread.write(message.getBytes(UTF8_CHARSET));
        }
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
                    BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();

                    Log.d(TAG, "Found device. Name: " +
                            deviceName + " address: " +
                            deviceHardwareAddress);

                    if (deviceName != null && deviceName.equals(BT_MODULE_NAME)) {
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
    protected void onPause() {
        killConnectThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceDiscoveryReceiver);
    }
}
