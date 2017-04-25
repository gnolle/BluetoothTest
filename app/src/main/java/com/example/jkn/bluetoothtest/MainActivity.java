package com.example.jkn.bluetoothtest;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
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

import com.example.jkn.bluetoothtest.btresponse.BtResponse;
import com.example.jkn.bluetoothtest.btresponse.BtResponseParser;
import com.example.jkn.bluetoothtest.btresponse.BtTemperatureResponse;
import com.example.jkn.bluetoothtest.btresponse.BtTimeResponse;
import com.example.jkn.bluetoothtest.cards.IconActionCard;
import com.example.jkn.bluetoothtest.cards.TextActionCard;
import com.example.jkn.bluetoothtest.colorpicker.ColorPickerFragment;

import java.util.Date;
import java.util.UUID;

/**
 * Activity to connect to HC-05 Bluetooth module.
 */
public class MainActivity extends AppCompatActivity implements BtConnectThread.BtConnectionCallback, BtConnectedThread.BtResponseListener, ColorPickerFragment.ColorPickerListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String BT_MODULE_NAME = "HC-05";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_CHECK = 2;

    private static final int TEMP_READ_INTERVAL = 5000;
    private static final int TIME_READ_INTERVAL = 5000;

    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBtDevice;
    private BtConnectThread mBtConnectThread;
    private BtConnectedThread mBtConnectedThread;
    private BluetoothSocket mBtSocket;
    private BroadcastReceiver mDeviceDiscoveryReceiver;
    private BtConnectionStatus mBtConnectionStatus;

    private IconActionCard colorAction;
    private IconActionCard bluetoothStatusCard;
    private TextActionCard tempStatusCard;
    private TextActionCard timeCard;
    private IconActionCard modeCard;

    private Handler mHandler;
    private Runnable mTemperatureReadingThread;
    private Runnable mTimeReadingThread;

    private boolean mIsLedOn = false;
    private int mMode = 0;
    private int mBrightnessSteps = 11;
    private int mBrightness = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewReferences();
        setClickListeners();

        initDeviceDiscoveryReceiver();
        registerDeviceDiscoveryReceiver();

        createScheduler();
        createTemperatureReadingThread();
        createTimeReadingThread();
    }

    private void setViewReferences() {
        colorAction = (IconActionCard) findViewById(R.id.btn_color);
        bluetoothStatusCard = (IconActionCard) findViewById(R.id.btn_bluetooth);
        tempStatusCard = (TextActionCard) findViewById(R.id.temperature_card);
        timeCard = (TextActionCard) findViewById(R.id.time_card);
        modeCard = (IconActionCard) findViewById(R.id.btn_mode);
    }

    private void createScheduler() {
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

    private void createTimeReadingThread() {
        mTimeReadingThread = new Runnable() {
            @Override
            public void run() {
                try {
                    requestTime();
                } finally {
                    mHandler.postDelayed(mTimeReadingThread, TIME_READ_INTERVAL);
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

    private void startTimeReading() {
        mTimeReadingThread.run();
    }

    private void stopTimeReading() {
        mHandler.removeCallbacks(mTimeReadingThread);
    }

    private void setClickListeners() {
        colorAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsLedOn) {
                    switchTestLedOff();
                } else {
                    switchTestLedOn();
                }
            }
        });
        colorAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorPicker();
            }
        });
        timeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String setTimeCommand = String.format(BtCommands.SET_TIME, Utils.getCurrentTimestamp());
                writeBtMessage(setTimeCommand);
            }
        });

        modeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMode = (mMode + 1) % 2;

                String setModeCommand = String.format(BtCommands.SET_MODE, mMode);
                writeBtMessage(setModeCommand);
                if (mMode == 0)
                    modeCard.setTextBottom("Time only");
                else
                    modeCard.setTextBottom("Time / Temperature");
            }
        });
    }

    private void sendColorCommand(HSVColor color) {
        String colorCommand = String.format(BtCommands.SET_COLOR, color.getHue(), color.getSaturation(), color.getValue());
        writeBtMessage(colorCommand);
        colorAction.setTextBottom(color.toString());
    }

    private void switchTestLedOn() {
        String onCommand = "on";
        writeBtMessage(onCommand);
        mIsLedOn = true;
        colorAction.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_light_on));
    }

    private void switchTestLedOff() {
        String onCommand = "off";
        writeBtMessage(onCommand);
        mIsLedOn = false;
        colorAction.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_light_off));
    }

    private void requestTemperature() {
        writeBtMessage(BtCommands.REQUEST_TEMPERATURE);
    }

    private void requestTime() {
        writeBtMessage(BtCommands.REQUEST_TIME);
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
        startTimeReading();
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

        try {
            BtResponse btResponse = BtResponseParser.parseResponse(response);

            switch (btResponse.getResponseType()) {
                case TEMPERATURE:
                    handleTemperatureResponse((BtTemperatureResponse) btResponse);
                    break;
                case TIME:
                    handleTimeResponse((BtTimeResponse) btResponse);
                    break;
            }
        } catch (BtException e) {
            Log.d(TAG, e.getMessage(), e);
        }
    }

    private void handleTemperatureResponse(BtTemperatureResponse temperatureResponse) {
        updateTemperature(temperatureResponse.getTemperature());
    }

    private void handleTimeResponse(BtTimeResponse timeResponse) {
        updateTime(timeResponse.getTime());
    }

    private void updateTemperature(final float temperature) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempStatusCard.setActionText(String.valueOf(temperature) + "°");
                tempStatusCard.setTextBottom(Utils.getReadableDateTime());
            }
        });
    }

    private void updateTime(final Date time) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeCard.setActionText(Utils.getShortTimeFromDate(time));
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

    private void showColorPicker() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ColorPickerFragment colorPicker = new ColorPickerFragment();
        colorPicker.show(ft, "colorPicker");
    }

    @Override
    public void onColorPicked(HSVColor pickedColor) {
        sendColorCommand(pickedColor);
    }

    @Override
    protected void onPause() {
        killConnectThread();
        killConnectedThread();
        stopTemperatureReading();
        stopTimeReading();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceDiscoveryReceiver);
    }
}
