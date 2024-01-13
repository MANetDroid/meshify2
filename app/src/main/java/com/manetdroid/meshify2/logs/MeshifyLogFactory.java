package com.manetdroid.meshify2.logs;

import com.manetdroid.meshify2.api.Message;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.Session;
import com.manetdroid.meshify2.framework.entities.MeshifyForwardEntity;
import com.manetdroid.meshify2.logs.logentities.LogEntity;
import com.manetdroid.meshify2.logs.logentities.MeshLog;
import com.manetdroid.meshify2.logs.logentities.MessageLog;

public class MeshifyLogFactory {
    public static LogEntity build(Message message, Session session, MessageLog.Event directMessageReceived) {
        return null;
    }

    public static LogEntity build(MeshifyForwardEntity forwardEntity) {
        return null;
    }

    public static LogEntity build(Session session, MeshifyForwardEntity forwardEntity, MeshLog.Event meshMessageReceivedToDestination) {
        return null;
    }
}
