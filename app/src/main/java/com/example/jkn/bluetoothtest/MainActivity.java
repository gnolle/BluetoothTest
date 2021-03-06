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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

    private static final int TEMP_READ_INTERVAL = 5000;

    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBtDevice;
    private BtConnectThread mBtConnectThread;
    private BtConnectedThread mBtConnectedThread;
    private BluetoothSocket mBtSocket;
    private BroadcastReceiver mDeviceDiscoveryReceiver;
    private BtConnectionStatus mBtConnectionStatus;

    private IconActionCard ledAction;
    private IconActionCard testAction;
    private IconActionCard bluetoothStatusCard;
    private TextActionCard tempStatusCard;

    private Handler mHandler;
    private Runnable mTemperatureReadingThread;

    private boolean mIsLedOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewReferences();
        setClickListeners();

        initDeviceDiscoveryReceiver();
        registerDeviceDiscoveryReceiver();

        createTemperatureScheduler();
        createTemperatureReadingThread();
    }

    private void setViewReferences() {
        ledAction = (IconActionCard) findViewById(R.id.btn_led);
        testAction = (IconActionCard) findViewById(R.id.btn_test_data);
        bluetoothStatusCard = (IconActionCard) findViewById(R.id.btn_bluetooth);
        tempStatusCard = (TextActionCard) findViewById(R.id.temperature_card);
    }

    private void createTemperatureScheduler() {
        mHandler = new Handler();
    }

    private void createTemperatureReadingThread() {
        mTemperatureReadingThread = new Runnable() {
            @Override
            public void run() {
                try {
                    requestTemperature();
                } finally {
                    mHandler.postDelayed(mTemperatureReadingThread, TEMP_READ_INTERVAL);
                }
            }
        };
    }

    private void startTemperatureReading() {
        mTemperatureReadingThread.run();
    }

    private void stopTemperatureReading() {
        mHandler.removeCallbacks(mTemperatureReadingThread);
    }

    private void setClickListeners() {
        ledAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsLedOn) {
                    switchTestLedOff();
                } else {
                    switchTestLedOn();
                }
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

    private void switchTestLedOn() {
        String onCommand = "on";
        writeBtMessage(onCommand);
        mIsLedOn = true;
        ledAction.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_light_on));
    }

    private void switchTestLedOff() {
        String onCommand = "off";
        writeBtMessage(onCommand);
        mIsLedOn = false;
        ledAction.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_light_off));
    }

    private void requestTemperature() {
        String tempRequestCommand = "TMP";
        writeBtMessage(tempRequestCommand);
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
        updateConnectionStatus();
    }

    private void updateConnectionStatus() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bluetoothStatusCard.setIcon(BtConnectionStatus.getDrawableForStatus(MainActivity.this, mBtConnectionStatus));
                bluetoothStatusCard.setTextBottom(BtConnectionStatus.getTextForStatus(mBtConnectionStatus));
            }
        });
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

        startTemperatureReading();
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

        if (response.length() >= 3) {
            String responseType = response.substring(0, 3);

            switch (responseType) {
                case "TMP":
                    try {
                        float temp = Float.valueOf(response.substring(3, response.length()));
                        updateTemperature(temp);
                    } catch (NumberFormatException e) {
                        Log.d(TAG, "Wrong temperature format", e);
                    }
            }
        }
    }

    private void updateTemperature(final float temperature) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempStatusCard.setActionText(String.valueOf(temperature) + "°");
            }
        });
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
        stopTemperatureReading();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceDiscoveryReceiver);
    }
}
