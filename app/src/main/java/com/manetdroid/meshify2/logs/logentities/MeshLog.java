package com.manetdroid.meshify2.logs.logentities;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.manetdroid.meshify2.framework.entities.MeshifyForwardEntity;

public class MeshLog extends LogEntity {

    String uuid;

    int hops;

    public MeshLog(Event event, MeshifyForwardEntity forwardEntity) {
        super(LogType.MESH, event.ordinal());
        this.uuid = String.valueOf(forwardEntity.getId());
        this.hops = forwardEntity.getHops();
    }

    public MeshLog(MeshifyForwardEntity forwardEntity) {
        super(LogType.MESH, Event.MeshMessageSent.ordinal());
        this.uuid = String.valueOf(forwardEntity.getId());
        this.hops = forwardEntity.getHops();
    }

    public MeshLog(ErrorEvent errorEvent, MeshifyForwardEntity forwardEntity) {
        super(LogEntity.LogType.MESH_ERROR, errorEvent.ordinal());
        this.uuid = String.valueOf(forwardEntity.getId());
        this.hops = forwardEntity.getHops();
    }

    public static MeshLog create(String serializedData) throws JsonSyntaxException {
        return (MeshLog) new Gson().fromJson(serializedData, MeshLog.class);
    }

    @Override
    public String serialize() {
        return new Gson().toJson((Object) this);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public enum ErrorEvent {
        MeshMessageDiscard,
        InvalidSession
    }

    public enum Event {
        MeshMessageSent,
        MeshMessageReceivedToForward,
        MeshMessageReceivedToDestination,
        MeshMessageReceivedDuplicated,
    }

}
