package com.manetdroid.meshify2.framework.controllers;

import android.content.Context;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.ConfigProfile;
import com.manetdroid.meshify2.api.ConnectionListener;
import com.manetdroid.meshify2.api.Message;
import com.manetdroid.meshify2.api.MessageListener;

public class MeshifyCore {
    public static final String PREFS_PUBLIC_KEY = "check1" ;
    public static final String PREFS_PRIVATE_KEY = "check2";
    public static final String PREFS_USER_UUID = "check3";
    public static final String PREFS_NAME = "check4";
    public static final String PREFS_APP_KEY = "check5";

    public MeshifyCore(Context context, Config config) {
    }

    public void setMessageListener(MessageListener messageListener) {
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
    }

    public void initializeServices() {
    }

    public void shutdownServices() {
    }

    public void pauseServices() {
    }

    public void resumeServices() {
    }

    public void sendMessage(Message message, String receiverId, ConfigProfile configProfile) {
    }

    public void sendBroadcastMessage(Message message, ConfigProfile configProfile) {
    }
}
