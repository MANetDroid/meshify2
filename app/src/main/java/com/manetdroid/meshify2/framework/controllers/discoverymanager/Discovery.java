package com.manetdroid.meshify2.framework.controllers.discoverymanager;

import android.content.Context;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.framework.controllers.connection.ConnectionSubscriber;
import com.manetdroid.meshify2.logs.Log;

import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;

public abstract class Discovery {

    protected String TAG = "[Meshify][Discovery]";

    public DisposableSubscriber<Device> disposableSubscriber;

    public Flowable<Device> deviceFlowable; //emits device objects

    private Config config;

    private boolean discoveryRunning = false;

    public Discovery() {
    }

    public void startDiscovery(Context context, Config config) {
        Log.d(TAG, "startDiscovery:");
        this.disposableSubscriber = new ConnectionSubscriber();
        this.deviceFlowable.subscribe(this.disposableSubscriber); //subscribe to the connection
    }

    public void stopDiscovery(Context context) {
        Log.d(TAG, "stopDiscovery:");
        if (this.disposableSubscriber != null) {
            this.disposableSubscriber.dispose();
        }
    }

    public boolean isDiscoveryRunning() {
        return discoveryRunning;
    }

    public void setDiscoveryRunning(boolean discoveryRunning) {
        this.discoveryRunning = discoveryRunning;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    protected void removeDeviceByAntennaType(Config.Antenna antenna) {
        //TODO
    }


}
