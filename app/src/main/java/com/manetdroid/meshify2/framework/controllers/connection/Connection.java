package com.manetdroid.meshify2.framework.controllers.connection;

public class Connection {

    private boolean isConnected;

    private int connectionRetries;

    Connection(boolean isConnected, int connectionRetries) {
        this.isConnected = isConnected;
        this.connectionRetries = connectionRetries;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public int getConnectionRetries() {
        return this.connectionRetries;
    }

    public void setConnectionRetries(int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }
}
