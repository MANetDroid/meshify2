package com.manetdroid.meshify2.api;

/**
 * This interface allows defining callback functions for the connection process.
 */
public interface ConnectionListener {

    /**
     * Callback when there is an error at the start.
     *
     * @param message   A string describing the error occurred.
     * @param errorCode An integer representing the error.
     */
    void onStartError(String message, int errorCode);

    /**
     * Callback when the connection is successfully started.
     */
    void onStarted();

    /**
     * Callback when a device is discovered successfully.
     *
     * @param device The Device object that was discovered.
     */
    void onDeviceDiscovered(Device device);

    /**
     * Callback when a device is connected successfully.
     *
     * @param device  The Device object that was connected.
     * @param session The Session object created for the device's connection.
     */
    void onDeviceConnected(Device device, Session session);

    /**
     * Callback when a device is blacklisted.
     *
     * @param device The Device object that was blacklisted.
     */
    void onDeviceBlackListed(Device device);

    /**
     * Callback when a device is lost due to connection loss.
     *
     * @param device The Device object that was lost.
     */
    void onDeviceLost(Device device);

    /**
     * Callback when an indirect device is discovered.
     *
     * @param device The Device object that was discovered indirectly.
     */
    void onIndirectDeviceDiscovered(Device device);
}

