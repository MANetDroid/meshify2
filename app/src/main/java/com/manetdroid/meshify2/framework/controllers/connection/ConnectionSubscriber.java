package com.manetdroid.meshify2.framework.controllers.connection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.MeshifyUtils;
import com.manetdroid.meshify2.api.exceptions.MeshifyException;
import com.manetdroid.meshify2.framework.controllers.bluetooth.BluetoothMeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.DeviceManager;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.MeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.SessionManager;
import com.manetdroid.meshify2.logs.Log;

import io.reactivex.CompletableObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class ConnectionSubscriber extends DisposableSubscriber<Device> {

    private static final String TAG = "[Meshify][ConnectionSubscriber]";

    public ConnectionSubscriber() {
    }

    private static void isAutoConnect() {
        if (Meshify.getInstance().getConfig().isAutoConnect()) {
            throw new MeshifyException(100, "Meshify is configured to auto connect.");
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        request(1L);
    }

    @Override
    public void onNext(Device device) {
        Log.d(TAG, "onNext: device: " + device + " | onThread: " + Thread.currentThread().getName());

        MeshifyDevice meshifyDevice = ConnectionManager.getConnectivity(device);

        if (meshifyDevice != null) {
            CompletableObserver completableObserver = new CompletableObserver() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    Log.d(TAG, "onSubscribe:MeshifyDevice ");
                }

                @Override
                public void onComplete() {
                    Log.d(TAG, "onComplete:MeshifyDevice ");
                    ConnectionManager.setMeshifyDevice((MeshifyDevice) null);
                    ConnectionSubscriber.this.request(1L);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Log.e(TAG, "onError:MeshifyDevice ");
                    MeshifyDevice meshifyDevice = ConnectionManager.getMeshifyDevice();
                    if (meshifyDevice != null && meshifyDevice.getDevice().equals(device)) {
                        if (meshifyDevice instanceof BluetoothMeshifyDevice) {
                            ConnectionSubscriber.this.retry(meshifyDevice.getDevice());
                        }
                        ConnectionManager.setMeshifyDevice((MeshifyDevice) null);
                        ConnectionSubscriber.this.request(1L);
                    }
                }
            };

            if (device.getAntennaType() == Config.Antenna.BLUETOOTH_LE) {
                meshifyDevice.create().subscribe(completableObserver);
            } else {
                Log.e(TAG, "isAutoConnect: " + Meshify.getInstance().getConfig().isAutoConnect());
//                if ( Meshify.getInstance().getConfig().isAutoConnect() ) {
                meshifyDevice.create().subscribeOn(Schedulers.newThread()).subscribe(completableObserver); //completableObserver subscribes meshifyDevice on a new thread
//                }
            }

        } else {
            request(1L);
        }

    }

    @SuppressLint("MissingPermission")
    public void retry(Device device) {
        Log.d(TAG, "retry: " + device.getDeviceName());
        ConnectionManager.retry(DeviceManager.getDevice(device.getDeviceAddress()));
        Log.e(TAG, "retry: Connection failed adding to blacklist");
        SessionManager.removeSession(device.getDeviceAddress());
        BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(Meshify.getInstance().getMeshifyCore().getContext());
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startDiscovery();
        }

    }

    @Override
    public void onError(Throwable t) {
        Log.e(TAG, "onError: " + t.getMessage());
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete: ");
        if (!isDisposed()) {
            dispose();
        }
    }
}
