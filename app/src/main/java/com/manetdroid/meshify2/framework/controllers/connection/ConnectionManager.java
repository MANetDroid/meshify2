package com.manetdroid.meshify2.framework.controllers.connection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.os.Handler;
import android.os.Looper;

import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.MeshifyUtils;
import com.manetdroid.meshify2.api.exceptions.MeshifyException;
import com.manetdroid.meshify2.framework.controllers.bluetooth.BluetoothMeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.BleMeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.bluetoothLe.MeshifyAdvertiseCallback;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.BluetoothController;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.MeshifyDevice;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.SessionManager;
import com.manetdroid.meshify2.logs.Log;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * <p>This class manages all the connections</p>
 *
 * @author Kavinda Perera
 * @version 1.0
 */
public class ConnectionManager {

    private static final String TAG = "[Meshify][ConnectionManager]";

    private static HashMap<String, Connection> connections = new HashMap<>();

    private static ConcurrentLinkedQueue<Device> devices = new ConcurrentLinkedQueue<>(); //unbounded thread-safe queue which arranges the element in FIFO

    private static MeshifyDevice meshifyDevice;

    /**
     * @return MeshifyDevice
     */
    public static MeshifyDevice getMeshifyDevice() {
        return meshifyDevice;
    }

    /**
     * @param meshifyDevice
     */
    static void setMeshifyDevice(MeshifyDevice meshifyDevice) {
        ConnectionManager.meshifyDevice = meshifyDevice;
    }

    /**
     * @param device
     * @return
     */
    static synchronized MeshifyDevice getConnectivity(Device device) {
        if (SessionManager.getSession(device.getDeviceAddress()) != null) {
            Log.d(TAG, "getConnectivity: already in session");
            return null;
        }

        if (ConnectionManager.getMeshifyDevice() != null && ConnectionManager.getMeshifyDevice().getDevice().equals(device)) {
            Log.d(TAG, "getConnectivity: already connecting to " + device.getDeviceAddress());
            return null;
        }

        switch (device.getAntennaType()) {
            case BLUETOOTH: {
                ConnectionManager.setMeshifyDevice(new BluetoothMeshifyDevice(device, false));
                break;
            }
            case BLUETOOTH_LE: {
                ConnectionManager.setMeshifyDevice(new BleMeshifyDevice(device));
            }
        }
        return meshifyDevice;
    }

    public static boolean checkConnection(String string) {
        Connection connection = connections.get(string);
        return connection != null && !connection.isConnected();
    }

    public static void retry(Device device) {
        Connection connection = connections.get(device.getDeviceAddress());
        if (connection != null) {
            connection.setConnectionRetries(connection.getConnectionRetries() + 1);
        } else {
            connection = new Connection(false, Meshify.getInstance().getConfig().getMaxConnectionRetries());
        }
        connections.put(device.getDeviceAddress(), connection);
        ConnectionManager.retryConnection(device);
    }

    private static void retryConnection(Device device) {
        Connection connection = connections.get(device.getDeviceAddress());
        if (connection.getConnectionRetries() <= Meshify.getInstance().getConfig().getMaxConnectionRetries()) {
            int n2 = connection.getConnectionRetries() * 2 * 1000;
            Completable.timer(n2, TimeUnit.MILLISECONDS).subscribe(() -> {
                Log.i(TAG, "run: opening connection of device: " + device.getDeviceAddress() + " to retry");
                connection.setConnected(true);
                connections.put(device.getDeviceAddress(), connection);
            });
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (Meshify.getInstance().getMeshifyCore() != null && Meshify.getInstance().getMeshifyCore().getConnectionListener() != null) {
                    Log.i(TAG, "onDeviceBlackListed:");
                    Meshify.getInstance().getMeshifyCore().getConnectionListener().onDeviceBlackListed(device);
                }
            });
        }
    }

    public static void reset() {
        connections = new HashMap();
    }

    private static void isAutoConnect() {
        if (Meshify.getInstance().getConfig().isAutoConnect()) {
            throw new MeshifyException(100, "Meshify is configured to auto connect.");
        }
    }


    public static void connect(Device device) {
        isAutoConnect();
        if (!devices.contains(device)) {
            devices.add(device);
        }
        ConnectionManager.connect();
    }


    @SuppressLint("MissingPermission")
    private static void connect() {
        isAutoConnect();
        if (meshifyDevice == null && devices.size() > 0) {
            final Device device = devices.poll();
            MeshifyDevice meshifyDevice1 = ConnectionManager.getConnectivity(device);
            CompletableObserver completableObserver = new CompletableObserver() {
                public void onSubscribe(Disposable d2) {
                    Log.d(TAG, "onSubscribe: ");
                }

                public void onComplete() {
                    Log.d(TAG, "onComplete: ");
                    ConnectionManager.setMeshifyDevice(null);
                    ConnectionManager.connect();
                }

                public void onError(Throwable e2) {
                    Log.e(TAG, "onError: " + e2);
                    MeshifyDevice meshifyDevice2 = ConnectionManager.getMeshifyDevice();
                    if (meshifyDevice2 != null && meshifyDevice2.getDevice().equals(device)) {
                        ConnectionManager.setMeshifyDevice(null);
                    }

                }
            };

            if (meshifyDevice1 != null) {

                if (BluetoothController.state == 3) {
                    BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(Meshify.getInstance().getMeshifyCore().getContext());
                    bluetoothAdapter.getBluetoothLeAdvertiser().stopAdvertising((AdvertiseCallback) new MeshifyAdvertiseCallback());
                    bluetoothAdapter.cancelDiscovery();
                    Log.i(TAG, "stop advertising & discovering " + bluetoothAdapter.isDiscovering());
                }

                Log.i(TAG, "start to connect: " + meshifyDevice1.getDevice().toString());
                meshifyDevice1.create().subscribeOn(Schedulers.newThread()).subscribe(completableObserver);
            }

        } else if (meshifyDevice != null) {
            Log.e(TAG, "wait to connect: " + meshifyDevice.getDevice().toString());
        }
    }
}
