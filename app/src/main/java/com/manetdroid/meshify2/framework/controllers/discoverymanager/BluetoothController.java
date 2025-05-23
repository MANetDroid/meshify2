package com.manetdroid.meshify2.framework.controllers.discoverymanager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.MeshifyUtils;
import com.manetdroid.meshify2.api.exceptions.MeshifyException;
import com.manetdroid.meshify2.framework.controllers.bluetooth.BluetoothDiscovery;
import com.manetdroid.meshify2.framework.controllers.bluetooth.BluetoothServer;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.BluetoothLeDiscovery;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.MeshifyAdvertiseCallback;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt.GattManager;
import com.manetdroid.meshify2.framework.controllers.helper.BluetoothUtils;
import com.manetdroid.meshify2.framework.exceptions.ConnectionException;
import com.manetdroid.meshify2.logs.Log;

public class BluetoothController extends AbstractController {

    public static int state = 0; // BLE Advertising state
    private static String TAG = "[Meshify][BluetoothController]";
    private static GattManager gattManager;
    private Config config;
    private Context context;
    private BluetoothDiscovery bluetoothDiscovery;
    private ThreadServer threadServer;
    private BluetoothLeDiscovery bluetoothLeDiscovery;
    private ThreadServer threadServerBle;
    private boolean isBLE = true; // BLE support check
    private MeshifyAdvertiseCallback advertiseCallback;

    private BluetoothAdapter bluetoothAdapter;


    public BluetoothController(Context context, Config config) throws MeshifyException {
        this.context = context;
        this.config = config;
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH_LE: {
                this.hardwareCheck();
            }
            case BLUETOOTH: {
                this.requestDiscoverable();
            }
        }
    }

    public static GattManager getGattManager() {
        return gattManager;
    }

    private void hardwareCheck() throws MeshifyException {
        if (!getContext().getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            this.isBLE = false;
            switch (this.getConfig().getAntennaType()) {
                case BLUETOOTH_LE: {
                    Log.e(TAG, "Bluetooth Low Energy not supported.");
                    getConfig().setAntennaType(Config.Antenna.UNREACHABLE);
                    throw new MeshifyException(0, "Bluetooth Low Energy not supported.");
                }
                case BLUETOOTH: {
                    getConfig().setAntennaType(Config.Antenna.BLUETOOTH);
                }
            }
        } else {
            //BLE
            gattManager = new GattManager();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestDiscoverable() {
        BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(getContext());
        this.bluetoothAdapter = bluetoothAdapter;
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE && Meshify.getInstance().getConfig().getAntennaType() == Config.Antenna.BLUETOOTH) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            getContext().startActivity(intent);
        }
    }

    @SuppressLint("MissingPermission")
    public boolean startAdvertising(String userUuid) throws IllegalStateException {

        if (state != 3) {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {

                if (this.bluetoothAdapter != null && this.bluetoothAdapter.getBluetoothLeAdvertiser() != null && this.threadServerBle != null && this.threadServerBle.getServerSocket() != null) {
                    AdvertiseSettings advertiseSettings = BluetoothUtils.getAdvertiseSettings();
                    AdvertiseData advertiseData = BluetoothUtils.getAdvertiseData(userUuid);
                    BluetoothLeAdvertiser bluetoothLeAdvertiser = this.bluetoothAdapter.getBluetoothLeAdvertiser();
                    if (bluetoothLeAdvertiser != null) {
                        this.advertiseCallback = new MeshifyAdvertiseCallback();
                        Log.i(TAG, "startAdvertising: " + advertiseData.toString());
                        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, this.advertiseCallback);
                    }
                    // TODO

                    state = 3;
                    return true;
                }
                state = 1;
                return false;
            } catch (IllegalStateException il) {
                state = 0;
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
        Log.e(TAG, "startAdvertising: advertising already running");
        return false;

    }

    private void disconnectDevices() {
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                //TODO - remove BT sessions
                break;
            }
            case BLUETOOTH_LE: {
                //TODO - remove BLE sessions
            }
        }
    }

    private void stateChangeAction(Intent intent, Context context) {
        BluetoothController bluetooth_controller = this;
        synchronized (bluetooth_controller) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                case BluetoothAdapter.STATE_ON: {
                    try {
                        this.startServer(context.getApplicationContext());
                        // TODO
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                }
                case BluetoothAdapter.STATE_OFF: {
                    try {
                        stopDiscovery(context.getApplicationContext());
                        disconnectDevices();
                        this.stopServer();
                    } catch (Exception e2) {
                        Log.w(TAG, e2.getMessage());
                    }
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void discover(Context context) {
        Log.d(TAG, "discovering... ");
        if (this.bluetoothDiscovery != null) {
            this.bluetoothDiscovery.setDiscoveryRunning(true);
        } else {
            this.bluetoothAdapter.cancelDiscovery();
            BluetoothDiscovery bluetoothDiscovery = new BluetoothDiscovery(context);
            this.bluetoothDiscovery = bluetoothDiscovery;

            this.bluetoothDiscovery.stopDiscovery(context);
            this.bluetoothDiscovery.startDiscovery(context, getConfig());
        }

    }

    @Override
    public void startDiscovery(Context context) {
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                this.startBluetoothDiscovery(context);
                break;
            }
            case BLUETOOTH_LE: {
                this.startBluetoothLeDiscovery(context);
            }
        }
    }

    private void startBluetoothDiscovery(Context context) {
        if (this.bluetoothDiscovery == null) {
            this.bluetoothDiscovery = new BluetoothDiscovery(context);
        }

        if (!this.bluetoothDiscovery.isDiscoveryRunning()) {
            this.bluetoothDiscovery.startDiscovery(context, getConfig());
        }
    }

    private void startBluetoothLeDiscovery(Context context) {
        Log.d(TAG, "startBluetoothLeDiscovery:");
        if (this.isBLE) {
            if (this.bluetoothLeDiscovery == null) {
                this.bluetoothLeDiscovery = new BluetoothLeDiscovery(context);
            } else {
                Log.w(TAG, "startBluetoothLeDiscovery: already exists");
            }
            if (!this.bluetoothLeDiscovery.isDiscoveryRunning()) {
                this.bluetoothLeDiscovery.startDiscovery(context, this.getConfig());
            } else {
                Log.e(TAG, "startBluetoothLeDiscovery: discovery already running");
            }
        }
    }

    @Override
    public void stopDiscovery(Context context) {
        if (this.bluetoothDiscovery != null) {
            this.bluetoothDiscovery.stopDiscovery(context);
        }

        //TODO - stop ble discovery

    }

    @Override
    public void startServer(Context context) throws ConnectionException {
        Log.d(TAG, "startServer: " + this.getConfig().getAntennaType());
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                startBluetoothServer(context);
                break;
            }
            case BLUETOOTH_LE: {
                startBluetoothLeServer(context);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothServer(Context context) throws ConnectionException {
        Log.d(TAG, "startBluetoothServer:");
        ThreadServer threadServer = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH, true);
        this.threadServer = threadServer;
        if (threadServer != null) {
            threadServer.startServer();
        }
        if (this.bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) { //get discoverable permissions
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); //value 0 means 120 seconds discoverable duration
            getContext().startActivity(intent);
        }
    }

    private void startBluetoothLeServer(Context context) {
        if (this.isBLE && this.bluetoothAdapter.getBluetoothLeAdvertiser() != null) {
            this.threadServerBle = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH_LE, true);
            try {
                this.threadServerBle.startServer();
            } catch (ConnectionException connectionException) {
                Log.e(TAG, "startBluetoothLeServer:", connectionException);
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            String userUuid = Meshify.getInstance().getMeshifyClient().getUserUuid();

            this.startAdvertising(userUuid);

        } else {
            //empty
            state = 1;
        }
    }

    @Override
    public void stopServer() throws ConnectionException {
        Log.i(TAG, "stopServer: ");
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                this.stopBluetoothServer();
                break;
            }
            case BLUETOOTH_LE: {
                //TODO - stop BLE server
            }
        }
    }

    private void stopBluetoothServer() throws ConnectionException {
        ThreadServer threadServer = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH_LE, false);
        this.threadServer = threadServer;
        if (threadServer != null) {
            threadServer.stopServer();
            this.threadServer = null;
            ServerFactory.setBluetoothServer((BluetoothServer) null);
        }
    }

    @SuppressLint("MissingPermission")
    public void onReceiveAction(Intent intent, Context context) {
        String actionLog = intent.getAction();
        Log.d(TAG, "onReceiveAction: " + actionLog);

        String action = intent.getAction();
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                this.stateChangeAction(intent, context);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                this.discover(context);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                this.bluetoothDiscovery.DiscoveryFinishedAction(context);
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress() + " | RSSI: " + rssi + "dBm");
                this.bluetoothDiscovery.addBluetoothDevice(intent);
                break;
            case BluetoothDevice.ACTION_UUID:
                this.bluetoothDiscovery.pair(intent);
        }

    }

    public Config getConfig() {
        return this.config;
    }

    public Context getContext() {
        return this.context;
    }
}
