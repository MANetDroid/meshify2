package com.manetdroid.meshify2.api;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import com.manetdroid.meshify2.logs.Log;
import com.manetdroid.meshify2.api.exceptions.MeshifyException;

public class MeshifyUtils {

    private static final String TAG = "[Meshify][MeshifyUtils]";
    private static final String LOCATION_PERMISSION_COARSE = "android.permission.ACCESS_COARSE_LOCATION";
    private static final String LOCATION_PERMISSION_FINE = "android.permission.ACCESS_FINE_LOCATION";

    // Checks if the device has Bluetooth Low Energy (BLE) hardware support
    private static boolean checkHardware(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    // Gets the BluetoothAdapter from the BluetoothManager
    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        return ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

    // Checks if location services (GPS or network) are available
    public static boolean isLocationAvailable(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network");
        } catch (Exception e) {
            Log.e(TAG, "Error checking location availability", e);
            return false;
        }
    }

    // Checks if the device is an Android Things device
    // Android Things is a platform for building connected devices with the Android OS
    public static boolean isThingsDevice(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature("android.hardware.type.embedded");
    }

    // Initializes Meshify, checking permissions and hardware support
    static void initialize(Context context, Config config) {
        switch (config.getAntennaType()) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                MeshifyUtils.checkPermissions(context);
                if (config.getAntennaType() == Config.Antenna.BLUETOOTH_LE && !MeshifyUtils.checkHardware(context)) {
                    throw new MeshifyException(Constants.BLE_NOT_SUPPORTED, Constants.BLE_NOT_SUPPORTED_STRING);
                }
            }
        }
    }

    // Checks if location permissions are granted
    public static boolean checkLocationPermissions(Context context) {
        return context.checkCallingOrSelfPermission(LOCATION_PERMISSION_COARSE) == PackageManager.PERMISSION_GRANTED
                || context.checkCallingOrSelfPermission(LOCATION_PERMISSION_FINE) == PackageManager.PERMISSION_GRANTED;
    }

    // Checks if Bluetooth permissions are granted
    public static boolean checkBluetoothPermission(Context context) {
        return context.checkCallingOrSelfPermission(LOCATION_PERMISSION_COARSE) == PackageManager.PERMISSION_GRANTED
                || context.checkCallingOrSelfPermission(LOCATION_PERMISSION_FINE) == PackageManager.PERMISSION_GRANTED;
    }

    // Enables Bluetooth (requires the BLUETOOTH_ADMIN permission)
    @SuppressLint("MissingPermission")
    public static void enableBluetooth(Context context) {
        MeshifyUtils.getBluetoothAdapter(context).enable();
    }

    /**
     * Determine whether you have granted location and Bluetooth permissions to Meshify.
     *
     * @param context
     * @throws MeshifyException
     */
    private static void checkPermissions(Context context) throws MeshifyException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkLocationPermissions(context)) {
                throw new MeshifyException(Constants.INSUFFICIENT_PERMISSIONS, Constants.INSUFFICIENT_LOCATION_PERMISSIONS_STRING);
            } else if (!isLocationAvailable(context)) {
                throw new MeshifyException(Constants.LOCATION_SERVICES_DISABLED, Constants.LOCATION_SERVICES_STRING);
            }
        }

        if (!checkBluetoothPermission(context)) {
            throw new MeshifyException( Constants.INSUFFICIENT_PERMISSIONS, Constants.INSUFFICIENT_BLUETOOTH_PERMISSIONS_STRING);
        }
    }
}
