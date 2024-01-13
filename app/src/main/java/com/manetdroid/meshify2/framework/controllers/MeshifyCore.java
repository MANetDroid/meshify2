package com.manetdroid.meshify2.framework.controllers;

import android.content.Context;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.ConfigProfile;
import com.manetdroid.meshify2.api.ConnectionListener;
import com.manetdroid.meshify2.api.Message;
import com.manetdroid.meshify2.api.MessageListener;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.Session;
import com.manetdroid.meshify2.framework.entities.MeshifyEntity;
import com.manetdroid.meshify2.framework.entities.MeshifyForwardTransaction;
import com.manetdroid.meshify2.framework.exceptions.MessageException;
import com.manetdroid.meshify2.logs.Log;

import java.io.IOException;

public class MeshifyCore {
    public static final String PREFS_PUBLIC_KEY = "check1" ;
    public static final String PREFS_PRIVATE_KEY = "check2";
    public static final String PREFS_USER_UUID = "check3";
    public static final String PREFS_NAME = "check4";
    public static final String PREFS_APP_KEY = "check5";

    private static final String TAG = "[Meshify][MeshifyCore]";
    public MeshifyCore(Context context, Config config) {
    }

    public static void sendEntity(Session session, MeshifyEntity meshifyEntity) throws MessageException, IOException {
        Log.d(TAG, "sendEntity:" + meshifyEntity );
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

    public MessageListener getMessageListener() {
        return null;
    }
}
