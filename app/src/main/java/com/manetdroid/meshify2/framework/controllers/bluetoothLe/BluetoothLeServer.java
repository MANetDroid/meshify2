package com.manetdroid.meshify2.framework.controllers.bluetoothLe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt.GattServerCallback;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.ThreadServer;
import com.manetdroid.meshify2.framework.controllers.helper.BluetoothUtils;
import com.manetdroid.meshify2.framework.entities.MeshifyHandshake;
import com.manetdroid.meshify2.framework.exceptions.ConnectionException;

public class BluetoothLeServer  extends ThreadServer<BluetoothDevice, BluetoothGattServer> {

    final String TAG = "[Meshify][BleServer]";

    private BluetoothManager bluetoothManager;

    private BluetoothGattService bluetoothGattService;

    private boolean isRunningLe = false;

    Object syncLock = new Object();

    public BluetoothLeServer(Config config, Context context) throws ConnectionException {
        super(config, context);
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.openGattServer(context);
        Log.d(TAG, "BluetoothLeServer: bluetooth le server created.");
    }


    @Override
    public void startServer() throws ConnectionException {
        Object syncLock1 = this.syncLock;
        synchronized (syncLock1) {
            if (!this.isAlive() && !this.isRunningLe()) {
                this.setRunning(true);
                this.setRunningLe(true);
                this.run();
            }
            else {
                Log.e(TAG, "startServer: trying started server fail, server was started.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopServer() throws ConnectionException {
        if (this.alive()) {
            try {
                this.setRunning(false);
                ((BluetoothGattServer)this.getServerSocket()).clearServices();
                ((BluetoothGattServer)this.getServerSocket()).close();
            }
            catch (Exception exception) {
                Log.e(TAG, "stopServer: " + exception.getMessage());
            }
            finally {
                //TODO - nullify device
                this.setRunningLe(false);
            }
        }
    }



    @Override
    public boolean alive() {
        return this.getServerSocket() != null && ((BluetoothGattServer) this.getServerSocket()).getServices().size() > 0;
    }

    public boolean isRunningLe() {
        return this.isRunningLe;
    }

    public void setRunningLe(boolean b) {
        this.isRunningLe = b;
    }

    private boolean addCharacteristic() {
        byte[] arrby = new MeshifyHandshake(null, null).toString().getBytes();
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.bluetoothGattService.getCharacteristic(BluetoothUtils.getCharacteristicUuid());
        if (bluetoothGattCharacteristic == null) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic2 = new BluetoothGattCharacteristic(BluetoothUtils.getCharacteristicUuid(), 26, 17);
            bluetoothGattCharacteristic2.addDescriptor(new BluetoothGattDescriptor(BluetoothUtils.batteryServiceUuid, 16));
            bluetoothGattCharacteristic2.setValue(arrby);
            if (this.bluetoothGattService != null) {
                this.bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic2);
                return true;
            }
            return false;
        }
        bluetoothGattCharacteristic.setValue(arrby);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        super.run();
        if (this.getServerSocket() != null) {
            this.bluetoothGattService = ((BluetoothGattServer) this.getServerSocket()).getService(BluetoothUtils.getBluetoothUuid());
        }
        if (this.bluetoothGattService == null && this.getServerSocket() != null) {
            this.bluetoothGattService = new BluetoothGattService(BluetoothUtils.getBluetoothUuid(), 0);
            if (this.addCharacteristic()) {
                ((BluetoothGattServer) this.getServerSocket()).addService(this.bluetoothGattService);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void openGattServer(Context context) {
        if (this.getServerSocket() == null) {
            this.acceptConnection(this.bluetoothManager.openGattServer(context, new GattServerCallback()));
        }
    }
}
