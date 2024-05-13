package com.manetdroid.meshify2.framework.controllers.wifidirect;

import android.app.Activity;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class GroupPacket implements Serializable {
    private UUID meshifyWifiUUID;
    private String ip;
    private String mac;
    private int port;
    private int originPort;

    private byte[] message;
    private String textMessage;
    private int type; // 0 - config, 1 - String message Host-Peer, 2 - Group device ports, 3 - String message Peer - Peer
    private int[] GroupDevicePortArray;
    private String[] GroupDeviceNameArray;
    private UUID[] GroupDeviceUUIDArray;

    public GroupPacket(String ip, String mac) {
        this.type = 0;
        this.ip = ip;
        this.mac = mac;
    }

    public GroupPacket(UUID deviceId, String textMessage) {
        this.type = 1;
        this.textMessage = textMessage;
        this.meshifyWifiUUID = deviceId;
    }

    public GroupPacket(byte[] message) {
        this.type = 1;
        this.message = message;
    }

    public GroupPacket(List<MultiServerThread> threadArray, int myPort) {
        this.type = 2;
        int[] portArray = new int[threadArray.size() - 1];
        UUID[] uuIDArray = new UUID[threadArray.size() - 1];
        String[] nameArray = new String[threadArray.size() - 1];
        int i = 0;
        for (MultiServerThread m : threadArray) {
            if (m.getSocket().getPort() == myPort) {
                continue;
            }
            portArray[i] = m.getSocket().getPort();
            i++;
        }
        this.GroupDevicePortArray = portArray;
    }

    public GroupPacket(UUID meshifyWifiUUID, String textMessage, int groupPort, int originPort) {
        this.type = 3;
        this.port = groupPort;
        this.originPort = originPort;
        this.textMessage = textMessage;
        this.meshifyWifiUUID = meshifyWifiUUID;
    }

    public UUID getMeshifyWifiUUID() {
        return meshifyWifiUUID;
    }

    public int getOriginPort() {
        return originPort;
    }

    public int getPort() {
        return port;
    }

    public int[] getGroupDevicePortArray() {
        return GroupDevicePortArray;
    }

    public byte[] getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTextMessage() {
        return textMessage;
    }
}

