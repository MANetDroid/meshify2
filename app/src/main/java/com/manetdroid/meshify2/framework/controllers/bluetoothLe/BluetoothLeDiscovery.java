package com.manetdroid.meshify2.framework.controllers.bluetoothLe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.profile.DeviceProfile;
import com.manetdroid.meshify2.framework.controllers.connection.ConnectionManager;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.DeviceManager;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.Discovery;
import com.manetdroid.meshify2.framework.controllers.helper.BluetoothUtils;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.Session;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.SessionManager;
import com.manetdroid.meshify2.logs.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;

public class BluetoothLeDiscovery extends Discovery {

    private static String TAG = "[Meshify][BluetoothLeDiscovery]";
    private final String bleUuid = BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getConfig().isAutoConnect()).toString();   // j
    private final String btUuid = BluetoothUtils.getHashedBluetoothUuid(Meshify.getInstance().getConfig().isAutoConnect()).toString();      // k
    private final String bleUuid2 = BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getMeshifyClient().getApiKey());          // l
    private BluetoothAdapter bluetoothAdapter;
    private HashMap<String, String> discoveredDevices;
    private HashMap<String, ScheduledFuture> scheduledDiscoveredDevices;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ScanCallback scanCallback;
    private ScheduledThreadPoolExecutor threadPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(40);


    public BluetoothLeDiscovery(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            Log.e(TAG, " BluetoothAdapter was NULL");
        }
        this.discoveredDevices = new HashMap<>();
        this.scheduledDiscoveredDevices = new HashMap<>();
    }

    @SuppressLint("CheckResult")
    @Override
    public void startDiscovery(Context context, Config config) {
        this.deviceFlowable = Flowable.create(new FlowableOnSubscribe<Device>() {
            @SuppressLint("MissingPermission")
            @Override
            public void subscribe(@NonNull FlowableEmitter<Device> emitter) throws Exception {
                if (BluetoothLeDiscovery.this.bluetoothAdapter != null && BluetoothLeDiscovery.this.bluetoothAdapter.isEnabled()) {
                    if (BluetoothLeDiscovery.this.bluetoothAdapter.getBluetoothLeScanner() == null) {
                        Log.w(TAG, "getBluetoothLeScanner() was null, sleeping for 300 ms.");
                        try {
                            Thread.sleep(300L);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                    try {
                        BluetoothLeDiscovery.this.bluetoothAdapter.getBluetoothLeScanner().startScan(BluetoothUtils.getBluetoothLeScanFilter(), BluetoothUtils.getScanSettings(), BluetoothLeDiscovery.this.scanCallback = new ScanCallback() {
                            @Override
                            public void onScanResult(int callbackType, ScanResult result) {
                                super.onScanResult(callbackType, result);
                                BluetoothLeDiscovery.this.onScanResultAction(result, (FlowableEmitter<Device>) emitter);
                            }

                            @Override
                            public void onBatchScanResults(List<ScanResult> results) {
                                super.onBatchScanResults(results);
                                Log.d(TAG, "onBatchScanResults: ");
                            }

                            @Override
                            public void onScanFailed(int errorCode) {
                                super.onScanFailed(errorCode);
                                BluetoothLeDiscovery.this.scanFailedAction(errorCode);
                            }
                        });
                        BluetoothLeDiscovery.this.setDiscoveryRunning(true);


                    } catch (IllegalStateException illegalStateException) {
                        Log.e(TAG, "error: " + illegalStateException.getMessage());
                    }
                }
            }
        }, BackpressureStrategy.BUFFER);
        super.startDiscovery(context, config);
        this.setConfig(config);
        Completable.timer(60L, TimeUnit.SECONDS).subscribe(() -> {
            if (SessionManager.sessionMap.isEmpty()) {
                Log.i(TAG, "startDiscovery: resetting");
                this.stopDiscovery(null);
                this.startDiscovery(null, this.getConfig());
            }
        }, throwable -> Log.e(TAG, "error: " + throwable.getMessage()));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopDiscovery(Context context) {
        super.stopDiscovery(context);
        this.disposable.clear();
        if (this.bluetoothAdapter != null && this.scanCallback != null && this.bluetoothAdapter.isEnabled() && this.bluetoothAdapter.getBluetoothLeScanner() != null) {
            Log.i(TAG, "stopDiscovery: stopping scan");
        } else {
            // TODO
        }
        this.setDiscoveryRunning(false);
    }

    @SuppressLint("MissingPermission")
    private void onScanResultAction(ScanResult scanResult, FlowableEmitter<Device> flowableEmitter) {
        String string = this.processScanResult(scanResult);
        if (string != null && this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled()) {
            Device device = this.processPresenceResult(string, scanResult.getDevice(), scanResult.getRssi());
            if (device != null && SessionManager.getSession(device.getDeviceAddress()) == null && Meshify.getInstance().getConfig().isAutoConnect()) {
                flowableEmitter.onNext(device);
            } else if (device != null) {
                this.addToScheduledFuture(device, string);
            }
        }
    }

    @SuppressLint("MissingPermission")
    synchronized Device processPresenceResult(String string, BluetoothDevice bluetoothDevice, int rssi) {
        if (this.bluetoothAdapter.isEnabled() && bluetoothDevice != null) {
            if (ConnectionManager.checkConnection(bluetoothDevice.getAddress())) {
                Log.e(TAG, "Device is blacklisted");
                return null;
            }
            if (SessionManager.sessionMap.get(bluetoothDevice.getAddress()) != null) {
                return null;
            }
            String userUuid = Meshify.getInstance().getMeshifyClient().getUserUuid();
            boolean bl = false;

            Collection<Session> collection = SessionManager.sessionMap.values();
            int count = 0;
            for (Session session : collection) {
                //TODO - get a session count
                if (session.getBluetoothGatt() != null && session.getBluetoothGatt().getDevice() != null && session.getBluetoothGatt().getDevice().equals(bluetoothDevice)) {
                    bl = true;
                    break;
                }
                count++;
            }
            if ((count < DeviceProfile.getMaxConnectionsForDevice()) || !Meshify.getInstance().getConfig().isAutoConnect()) { //TODO - check maximum connections
                Device device = DeviceManager.getDevice(bluetoothDevice.getAddress());
                if (device == null) {
                    device = new Device(bluetoothDevice, true);
                    device.setAntennaType(Config.Antenna.BLUETOOTH_LE);
                    device.setDeviceName(string);
                    device.setUserId(string);
                    device.setRssi(rssi);
                }
                return device;
            }
            return null;
        }
        Log.e(TAG, "processPresenceResult: could not process Bluetooth device");
        return null;
    }

    private String processScanResult(ScanResult scanResult) {
        String string;
        int rssi;

        blockX:
        {
            BluetoothDevice bluetoothDevice;
            Map<ParcelUuid, byte[]> map;
            blockY:
            {
                List list = scanResult.getScanRecord().getServiceUuids();
                map = scanResult.getScanRecord().getServiceData();
                bluetoothDevice = scanResult.getDevice();
                rssi = scanResult.getRssi();
                Log.i(TAG, "scanResult: " + map.toString() + " | Rssi: " + rssi);
                string = null;
                if (list == null || list.isEmpty()) break blockY;

                break blockX;
            }
            if (map == null || map.entrySet() == null) break blockX;
            for (Map.Entry entry : map.entrySet()) {
                String string3 = new String((byte[]) entry.getValue());
                UUID uuid = ((ParcelUuid) entry.getKey()).getUuid();
                string = this.discoveredDevices.get(bluetoothDevice.getAddress());
                if (string != null) continue;
                Log.w(TAG, "\nAPPKEY: " + Meshify.getInstance().getMeshifyClient().getApiKey() + "\nDeviceData: " + string3 + "\nService UUID: " + uuid.toString() + "\nCustom UUID: " + this.bleUuid2 + "\nBT UUID: " + this.btUuid + "\nBLE UUID: " + this.bleUuid);
                if (!uuid.toString().equalsIgnoreCase(this.bleUuid) && !uuid.toString().equalsIgnoreCase(this.btUuid) && !uuid.toString().equalsIgnoreCase(this.bleUuid2))
                    continue;
                string = BluetoothUtils.getUuidFromDataString(string3);
                this.discoveredDevices.put(bluetoothDevice.getAddress(), string);
                Log.i(TAG, "Device Found " + string3 + " userUuid: " + string);
            }
        }
        return string;
    }

    private void addToScheduledFuture(Device device, String string) {
        ScheduledFuture<?> scheduledFuture;
        if (SessionManager.getSession(device.getDeviceAddress()) == null && !this.scheduledDiscoveredDevices.containsKey(string)) {
            Meshify.getInstance().getMeshifyCore().getConnectionListener().onDeviceDiscovered(device);
        }
        if ((scheduledFuture = this.scheduledDiscoveredDevices.remove(string)) != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }

        int delay = Build.VERSION.SDK_INT < 24 ? 30 : 5;
        scheduledFuture = this.threadPoolExecutor.schedule(new scheduleDevice(device), delay, TimeUnit.SECONDS);
        this.scheduledDiscoveredDevices.put(string, scheduledFuture);
    }

    private void scanFailedAction(int errorCode) {
        this.setDiscoveryRunning(false);
        switch (errorCode) {
            case 1: {
                Log.e(TAG, "onScanFailed: Fails to start scan as BLE scan with the same settings is already started by the app | SCAN_FAILED_ALREADY_STARTED | code: " + errorCode);
                break;
            }
            case 2: {
                Log.e(TAG, "onScanFailed: Fails to start scan as app cannot be registered | SCAN_FAILED_APPLICATION_REGISTRATION_FAILED | code: " + errorCode);
                break;
            }
            case 23: {
                Log.e(TAG, "onScanFailed: Fails to start scan due an internal error | SCAN_FAILED_INTERNAL_ERROR | code: " + errorCode);
                break;
            }
            case 4: {
                Log.e(TAG, "onScanFailed: Fails to start power optimized scan as this feature is not supported | SCAN_FAILED_FEATURE_UNSUPPORTED | code: " + errorCode);
            }
        }
        Log.e(TAG, "SCANNING_FAILED_WITH_ERROR | code: " + errorCode);
    }

    private class scheduleDevice implements Runnable {
        private Device device;

        scheduleDevice(Device device) {
            this.device = device;
        }

        @Override
        public void run() {
            ScheduledFuture scheduledFuture = (ScheduledFuture) BluetoothLeDiscovery.this.scheduledDiscoveredDevices.remove(this.device.getUserId());
            Log.d(TAG, "Schedule active for: " + this.device.getUserId() + " and found: " + BluetoothLeDiscovery.this.discoveredDevices.containsKey(this.device.getBluetoothDevice().getAddress()));

            if (BluetoothLeDiscovery.this.discoveredDevices.containsKey(this.device.getBluetoothDevice().getAddress())) {
                Iterator iterator = BluetoothLeDiscovery.this.discoveredDevices.entrySet().iterator();
                String key = null;

                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    if (!((String) entry.getValue()).equals(this.device.getUserId())) continue;
                    key = (String) entry.getKey();
                }
                Log.i(TAG, "Key found: " + key);

                if (key != null) {
                    BluetoothLeDiscovery.this.discoveredDevices.remove(key);
                    Log.e(TAG, "discoveredDevices: " + BluetoothLeDiscovery.this.discoveredDevices.size() + " scheduledDiscoveredDevices: " + BluetoothLeDiscovery.this.scheduledDiscoveredDevices.size());

                    if (SessionManager.getSession(this.device.getBluetoothDevice().getAddress()) == null) {

                    } else {
                        Log.d(TAG, "Device already have a session");
                    }
                }
            }
        }
    }

}
