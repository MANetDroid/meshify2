package com.manetdroid.meshify2.framework.controllers.bluetoothLe;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.profile.DeviceProfile;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt.BluetoothLeGatt;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.gatt.operations.BatteryService;
import com.manetdroid.meshify2.framework.controllers.connection.ConnectionManager;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.BluetoothController;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.DeviceManager;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.MeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.helper.BluetoothUtils;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.AbstractSession;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.Session;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.SessionManager;
import com.manetdroid.meshify2.logs.Log;

import java.lang.reflect.Method;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class BleMeshifyDevice extends MeshifyDevice {

    final String TAG = "[Meshify][BleMeshifyDevice]";

    CompletableEmitter completableEmitter;

    GattCallback gattCallback = new GattCallback();


    public BleMeshifyDevice(Device device) {
        super(device);
    }

    @Override
    public Completable create() {
        return Completable.create(CompletableEmitter -> {

            this.completableEmitter = completableEmitter;

            BluetoothDevice bluetoothDevice = this.getDevice().getBluetoothDevice();

            if (Meshify.getInstance().getMeshifyCore() != null) {

                Log.i(TAG, "Connecting GATT " + this.getDevice().getBluetoothDevice().getAddress() + " " + this.getDevice().getUserId());

                BluetoothLeGatt bluetoothLeGatt = new BluetoothLeGatt(Meshify.getInstance().getMeshifyCore().getContext());

                bluetoothLeGatt.connectGatt(bluetoothDevice, false, this.gattCallback);

                // TODO - connect as client - create session

                Log.e(this.TAG, "connect as client device address: " + this.getDevice().getDeviceAddress());

                Session session = SessionManager.getSession(this.getDevice().getDeviceAddress());
                if (session == null) {
//                    session = new Session(bluetoothDevice, true, this.completableEmitter);
                }
            }
        });
    }

    private void sendInitialHandShake(Device device){
        Log.e(TAG, "sendInitialHandShake: " + device.getDeviceAddress());
        BatteryService batteryService = new BatteryService(device.getBluetoothDevice(), BluetoothUtils.getBluetoothUuid(), BluetoothUtils.getCharacteristicUuid(), BluetoothUtils.batteryServiceUuid, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        BluetoothController.getGattManager().addGattOperation(batteryService);

    }

    /**
     * The main interface that the app has to implement in order to receive callbacks for most BluetoothGatt-related operations
     * like reading, writing, or getting notified about incoming notifications or indications.
     */
    private class GattCallback extends BluetoothGattCallback {

        public GattCallback() {
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.e(BleMeshifyDevice.this.TAG, "onPhyUpdate(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.e(BleMeshifyDevice.this.TAG, "onPhyRead(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(BleMeshifyDevice.this.TAG,"onConnectionStateChange()" + " | newState: " + newState + " | status: " + status);

            Object temp;
            if (status == BluetoothGatt.GATT_SUCCESS){

                // check bluetooth profile states
                if (newState == BluetoothProfile.STATE_CONNECTED) { // The profile is in connected state

                    Log.i(BleMeshifyDevice.this.TAG,"BluetoothProfile.STATE_CONNECTED: " + gatt.getDevice().getAddress());

                    // check and create a session
                    temp = SessionManager.getSession(gatt.getDevice().getAddress());
                    if (temp == null) {
                        Log.i(BleMeshifyDevice.this.TAG, "onConnectionStateChange(): create new session");
                        temp = new Session(gatt);
                    }else {
                        ((AbstractSession) temp).setBluetoothGatt(gatt);
                        Log.i(BleMeshifyDevice.this.TAG, "onConnectionStateChange(): reusing previous empty session");
                    }

                    // check and create a device
                    Device device = DeviceManager.getDevice(gatt.getDevice().getAddress());
                    if (device == null) {
                        device = new Device(gatt.getDevice(), true);
                    }
                    device.setAntennaType(Config.Antenna.BLUETOOTH_LE);
                    device.setSessionId(((Session)temp).getSessionId());

                    ((AbstractSession)temp).setClient(true);
                    ((AbstractSession)temp).setDevice(device);

                    SessionManager.queueSession((Session)temp); // Queue the session before handshake
                    DeviceManager.addDevice(device);
                    BluetoothController.getGattManager().getBluetoothGattMap().put(gatt.getDevice().getAddress(), gatt);
                    if (Build.VERSION.SDK_INT >= 21) {
                        gatt.requestMtu(DeviceProfile.getMaxMtuSize()); // Requesting for a larger ATT MTU
                    } else {
                        gatt.discoverServices();
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.e(BleMeshifyDevice.this.TAG, "BluetoothProfile.STATE_DISCONNECTED: " + gatt.getDevice().getAddress());

                    // TODO - remove device and session

                    if (gatt != null) {
                        gatt.close();
                    }
                }
            } else if (status == 133){
                Log.e(BleMeshifyDevice.this.TAG,  "GATT 133 Error!!" + gatt.getDevice().getAddress());

                if (gatt.getDevice() != null && gatt.getDevice().getAddress() != null) {

                }
                this.clearFailedConnection(gatt);

            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.e(BleMeshifyDevice.this.TAG, "BluetoothProfile.STATE_DISCONNECTED: " + gatt.getDevice().getAddress());
                    if (gatt != null) {
                        gatt.close();
                    }
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e(BleMeshifyDevice.this.TAG,"onMtuChanged()" + " | mtu: " + mtu + " | status: " + status);


            Session session = SessionManager.getSession(gatt.getDevice().getAddress());

            if (mtu == DeviceProfile.getMaxMtuSize()) {

            }

            @SuppressLint("MissingPermission") boolean bl = gatt.discoverServices();
            Log.e(BleMeshifyDevice.this.TAG, "start service discovery: " + bl);

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(BleMeshifyDevice.this.TAG,"onServicesDiscovered()" + " | status: " + status + " | size: " + gatt.getServices().size());

            for (BluetoothGattService service : gatt.getServices()) {
                Log.i(BleMeshifyDevice.this.TAG,"service discovered " + service.getUuid().toString());
            }

            if (status == 0) {
                // DOUBLE_RATE
                if (!DeviceProfile.isLeDoubleRateSupported(Meshify.getInstance().getMeshifyCore().getContext())) return;
                gatt.setPreferredPhy(2, 2, 0);

                // TODO - create a config to set DOUBLE_RATE or EXTENDED_RANGE

                BluetoothGattCharacteristic bluetoothGattCharacteristic = gatt.getService(BluetoothUtils.getBluetoothUuid()).getCharacteristic(BluetoothUtils.getCharacteristicUuid());
                if (gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)){
                    BleMeshifyDevice.this.sendInitialHandShake(BleMeshifyDevice.this.getDevice());
                    //TODO - current position
                    return;
                }
            } else {
                this.clearFailedConnection(gatt);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicRead()" + " | characteristic: " + characteristic + " | status: " + status);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicWrite()" + " | characteristic: " + characteristic + " | status: " + status);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicChanged()" + " | characteristic: " + characteristic );

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e(BleMeshifyDevice.this.TAG,"onDescriptorRead()" + " | descriptor: " + descriptor + " | status: " + status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e(BleMeshifyDevice.this.TAG,"onDescriptorWrite()" + " | descriptor: " + descriptor + " | status: " + status);

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.e(BleMeshifyDevice.this.TAG,"onReliableWriteCompleted()" + " | status: " + status);

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.e(BleMeshifyDevice.this.TAG,"onReadRemoteRssi()" + " | rssi: " + rssi + " | status: " + status);

        }

        @SuppressLint("MissingPermission")
        private void clearFailedConnection(BluetoothGatt bluetoothGatt) {
            BluetoothController.getGattManager().removeGattOperation(bluetoothGatt.getDevice());
            BluetoothController.getGattManager().getBluetoothGattMap().remove(bluetoothGatt.getDevice().getAddress());
            if (BluetoothController.getGattManager().getGattOperation() != null && BluetoothController.getGattManager().getGattOperation().getBluetoothDevice().getAddress().equals(bluetoothGatt.getDevice().getAddress())) {
                //
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            this.refreshDeviceCache(bluetoothGatt);
            Device device = DeviceManager.getDevice(bluetoothGatt.getDevice().getAddress());
            Log.e(TAG, "clearFailedConnection(): address " + bluetoothGatt.getDevice().getAddress());
            Log.e(TAG, "clearFailedConnection(): queued device " + device);
            ConnectionManager.retry(device);
            SessionManager.removeSession(bluetoothGatt.getDevice().getAddress());
        }

        void refreshDeviceCache(BluetoothGatt bluetoothGatt) {
            try {
                Method localMethod  = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod  != null) {
                    boolean bl = (Boolean)localMethod .invoke((Object)bluetoothGatt, new Object[0]);
                    Log.w(TAG, "refreshDeviceCache:" + bl);
                }
            }
            catch (Exception exception) {
                Log.e(TAG, "exception occurred while refreshing", exception);
            }
        }
    }

}
