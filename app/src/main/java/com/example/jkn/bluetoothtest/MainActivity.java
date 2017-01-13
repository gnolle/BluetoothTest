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
import android.view.View;
import android.widget.Button;

import java.util.UUID;

/**
 * Activity to connect to HC-05 Bluetooth module.
 */
public class MainActivity extends AppCompatActivity implements BtConnectThread.BtConnectionCallback, BtConnectedThread.BtResponseListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String BT_MODULE_NAME = "HC-05";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_CHECK = 2;

    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBtDevice;
    private BtConnectThread mBtConnectThread;
    private BtConnectedThread mBtConnectedThread;
    private BluetoothSocket mBtSocket;
    private BroadcastReceiver mDeviceDiscoveryReceiver;
    private BtConnectionStatus mBtConnectionStatus;

    private IconActionCard onAction;
    private IconActionCard offAction;
    private IconActionCard testAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewReferences();
        setClickListeners();

        initDeviceDiscoveryReceiver();
        registerDeviceDiscoveryReceiver();
    }

    private void setViewReferences() {
        onAction = (IconActionCard) findViewById(R.id.btn_on);
        offAction = (IconActionCard) findViewById(R.id.btn_off);
        testAction = (IconActionCard) findViewById(R.id.btn_test_data);
    }

    private void setClickListeners() {
        onAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String onCommand = "on";
                writeBtMessage(onCommand);
            }
        });
        offAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String onCommand = "off";
                writeBtMessage(onCommand);
            }
        });
        testAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String onCommand = "Very long test data text. äöüß Check if separated. äüü";
                writeBtMessage(onCommand);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setConnectionStatus(BtConnectionStatus.NOT_CONNECTED);

        if (isLocationPermissionGranted()) {
            initConnection();
        } else {
            requestLocationPermission();
        }
    }

    public void initConnection() {
        initBtAdapter();

        if (isBtAvailable()) {
            startConnection();
        } else {
            requestEnableBt();
        }
    }

    private void startConnection() {
        if (mBtDevice == null) {
            setConnectionStatus(BtConnectionStatus.DISCOVERING);
            mBtAdapter.startDiscovery();

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
                } else {
                    finish();
                }
            }
        }
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
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

    private void killConnectedThread() {
        if (mBtConnectedThread != null) {
            mBtConnectedThread.cancel();
            mBtConnectedThread = null;
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
        setUpConnectedThread();
        setConnectionStatus(BtConnectionStatus.CONNECTED);
        Log.d(TAG, "Connection attempt succeeded.");
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
    public void handleBtResponse(String response) {
        Log.d(TAG, "Response: " + response);
    }

    private void writeBtMessage(String message) {
        if (mBtConnectedThread != null && mBtConnectionStatus == BtConnectionStatus.CONNECTED) {
            mBtConnectedThread.write(message);
        }
    }

    private boolean isBtAvailable() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        return (mBtAdapter != null && mBtAdapter.isEnabled());
    }

    private void initBtAdapter() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
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
                    finish();
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
        killConnectedThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceDiscoveryReceiver);
    }
}
