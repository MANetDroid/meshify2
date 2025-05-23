package com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt.operations;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt.GattOperation;

import java.util.UUID;

public class BatteryService extends GattOperation {

    private final UUID serviceUuid;
    private final UUID characteristicUuid;
    private final UUID descriptorUuid;
    private byte[] descriptorValue;

    public BatteryService(BluetoothDevice bluetoothDevice, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, byte[] descriptorValue) {
        super(bluetoothDevice);
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.descriptorUuid = descriptorUuid;
        this.descriptorValue = descriptorValue;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void writeDescriptor(BluetoothGatt bluetoothGatt) {
        BluetoothGattDescriptor bluetoothGattDescriptor;
        BluetoothGattCharacteristic bluetoothGattCharacteristic;
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(this.serviceUuid);
        if (bluetoothGattService != null && (bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(this.characteristicUuid)) != null && (bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(this.descriptorUuid)) != null) {
            bluetoothGattDescriptor.setValue(this.descriptorValue);
            bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
        }
    }

}
