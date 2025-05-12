package com.manetdroid.meshify2.framework.controllers.discoverymanager;

import android.content.Context;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.framework.exceptions.ConnectionException;

public abstract class ThreadServer<SOCKET, SERVER_SOCKET> extends Thread { //can be of type socket or server socket

    public final String TAG = getClass().getCanonicalName();
    protected Context context;
    private SERVER_SOCKET server_socket;

    private boolean isRunning = false;

    protected ThreadServer(Config config, Context context) throws ConnectionException {
        this.context = context;
    }

    public abstract void startServer() throws ConnectionException;

    public abstract void stopServer() throws ConnectionException;

    public abstract boolean alive();

    public SERVER_SOCKET getServerSocket() {
        return this.server_socket;
    }

    public void acceptConnection(SERVER_SOCKET server_socket) {
        this.server_socket = server_socket;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean b) {
        this.isRunning = b;
    }

}
