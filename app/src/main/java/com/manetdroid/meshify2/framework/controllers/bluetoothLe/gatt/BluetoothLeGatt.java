package com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;

public class BluetoothLeGatt {

    private final Context context;

    public BluetoothLeGatt(Context context) {
        this.context = context;
    }

    public BluetoothGatt connectGatt(BluetoothDevice bluetoothDevice, boolean autoConnect, BluetoothGattCallback callback) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (bluetoothDevice == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= 24 || !autoConnect) {
            return this.connectGatt(callback, bluetoothDevice, false);
        }

        // TODO
        return this.connectGatt(callback, bluetoothDevice, true);
    }

    @SuppressLint("MissingPermission")
    private BluetoothGatt connectGatt(BluetoothGattCallback bluetoothGattCallback, BluetoothDevice bluetoothDevice, boolean autoConnect) {
        if (Build.VERSION.SDK_INT >= 23) {
            return bluetoothDevice.connectGatt(this.context, autoConnect, bluetoothGattCallback, 0);
        }
        return bluetoothDevice.connectGatt(this.context, autoConnect, bluetoothGattCallback);
    }

}
