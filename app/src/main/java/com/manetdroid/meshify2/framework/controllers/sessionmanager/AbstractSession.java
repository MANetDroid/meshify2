package com.manetdroid.meshify2.framework.controllers.sessionmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothSocket;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.framework.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import io.reactivex.CompletableEmitter;

public abstract class AbstractSession {

    protected final String TAG = "[Meshify][AbstractSession]";
    CompletableEmitter emitter;
    private BluetoothGattServer gattServer;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;
    private ArrayList<byte[]> arrayList = new ArrayList();
    private BluetoothSocket bluetoothSocket;
    private Socket socket;
    private String uuid;
    private Device device;
    private Config.Antenna antennaType;
    private boolean isConnected = false;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String userId;
    private String publicKey;
    private boolean isClient;

    public AbstractSession(BluetoothSocket bluetoothSocket) {
        this.setBluetoothSocket(bluetoothSocket);
        this.setAntennaType(Config.Antenna.BLUETOOTH);
        this.uuid = Utils.generateSessionId();
    }

    public AbstractSession(Socket socket, Config.Antenna antenna) {
        this.setSocket(socket);
        this.setAntennaType(antenna);
        this.uuid = Utils.generateSessionId();
    }

    public AbstractSession(Config.Antenna antenna) {
        this.setAntennaType(antenna);
    }

    public AbstractSession(BluetoothDevice bluetoothDevice, boolean z, CompletableEmitter completableEmitter) {
        this.emitter = completableEmitter;
        setBluetoothDevice(bluetoothDevice);
        if (z) {
            setAntennaType(Config.Antenna.BLUETOOTH_LE);
        } else {
            setAntennaType(Config.Antenna.BLUETOOTH);
        }
    }

    public AbstractSession() {

    }

    public CompletableEmitter getEmitter() {
        return this.emitter;
    }

    public BluetoothSocket getBluetoothSocket() {
        return this.bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    public ArrayList<byte[]> getArrayList() {
        return this.arrayList;
    }

    public synchronized void setArrayList(ArrayList<byte[]> arrayList) {
        this.arrayList = arrayList;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getDataOutputStream() {
        return this.dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return this.dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public Config.Antenna getAntennaType() {
        return this.antennaType;
    }

    public void setAntennaType(Config.Antenna antennaType) {
        this.antennaType = antennaType;
    }

    public BluetoothDevice getBluetoothDevice() {
        return this.bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothGatt getBluetoothGatt() {
        return this.bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public BluetoothGattServer getGattServer() {
        return this.gattServer;
    }

    public void setGattServer(BluetoothGattServer gattServer) {
        this.gattServer = gattServer;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public boolean isClient() {
        return this.isClient;
    }

    public void setClient(boolean z) {
        this.isClient = z;
    }
}
