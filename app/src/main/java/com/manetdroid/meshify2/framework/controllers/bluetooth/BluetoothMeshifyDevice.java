package com.manetdroid.meshify2.framework.controllers.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.MeshifyUtils;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.DeviceManager;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.MeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.helper.BluetoothUtils;
import com.manetdroid.meshify2.framework.controllers.helper.RetryWhenLambda;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.Session;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.SessionManager;
import com.manetdroid.meshify2.logs.Log;

import java.io.IOException;

import io.reactivex.Completable;

/**
 * <p>This class represents a Bluetooth-enabled meshify device..</p>
 *
 * @author Kavinda Perera
 * @version 1.0
 */
public class BluetoothMeshifyDevice extends MeshifyDevice {

    final String TAG = "[Meshify][BluetoothMeshifyDevice]";

    private boolean isSecure = false;

    public BluetoothMeshifyDevice(Device device, boolean isSecure) {
        super(device);
        this.isSecure = isSecure;
    }

    /**
     * @return a RxJava Completable object
     */
    @SuppressLint("MissingPermission")
    @Override
    public Completable create() {
        return Completable.create(completableEmitter -> {
            try {
                BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(Meshify.getInstance().getMeshifyCore().getContext());
                if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                BluetoothSocket bluetoothSocket = this.isSecure ? this.getDevice().getBluetoothDevice().createRfcommSocketToServiceRecord(BluetoothUtils.getBluetoothUuid()) : this.getDevice().getBluetoothDevice().createInsecureRfcommSocketToServiceRecord(BluetoothUtils.getBluetoothUuid());
                bluetoothSocket.connect();
                Log.e(TAG, "connect(): success [ " + BluetoothUtils.getBluetoothUuid() + " | onThread: " + Thread.currentThread().getName() + " ]");

                Session session = new Session(bluetoothSocket);
                session.setSessionId(this.getDevice().getDeviceAddress());
                session.setClient(true);
                this.getDevice().setSessionId(session.getSessionId());
                session.setDevice(this.getDevice());
                SessionManager.queueSession(session);
                DeviceManager.addDevice(session.getDevice());
                completableEmitter.onComplete(); //call connection subscriber onComplete
            } catch (IOException iOException) {
                Log.e(TAG, "connect(): fail [ " + iOException.getMessage() + " | onThread: " + Thread.currentThread().getName() + " ]");
                completableEmitter.tryOnError((Throwable) iOException); //call connection subscriber onError
            }
        }).retryWhen(new RetryWhenLambda(3, 1000));
    }
}
