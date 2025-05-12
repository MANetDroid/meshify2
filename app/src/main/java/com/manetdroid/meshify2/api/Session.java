package com.manetdroid.meshify2.api;

public interface Session {

    String getPublicKey();

    String getUserId();

    Config.Antenna getAntennaType();

    void disconnect();

    boolean isClient();
}