package com.manetdroid.meshify2.logs.logentities;

public class MeshLog {
    public enum Event {
        MeshMessageSent,
        MeshMessageReceivedToForward,
        MeshMessageReceivedToDestination,
        MeshMessageReceivedDuplicated,
    }
}
