package com.manetdroid.meshify2.framework.controllers.discoverymanager;

import android.content.Context;

import com.manetdroid.meshify2.framework.exceptions.ConnectionException;

abstract class AbstractController {

    AbstractController() {

    }

    abstract void startDiscovery(Context context);

    abstract void stopDiscovery(Context context);

    abstract void startServer(Context context) throws ConnectionException;

    abstract void stopServer() throws ConnectionException;

}
