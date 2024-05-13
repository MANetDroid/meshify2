package com.manetdroid.meshify2.framework.controllers.wifidirect;

public class WifiDirectStatus {
    private String connectionStatus;

    public WifiDirectStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }
}
