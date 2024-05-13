package com.manetdroid.meshify2.framework.controllers.wifidirect;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientClass extends Thread {
    String TAG = "[Meshify][ClientClass]";
    String hostAdd;
    List<Integer> clientPeerSocketList = new ArrayList<Integer>();
    private UUID deviceId;
    private String username;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private int selectedClientPort;
    private OutputDataListener listener;
    private WifiDirectStatusListener wifiDirectStatusListener;

    public ClientClass(WifiDirectMeshifyDevice wifiDirectMeshifyDevice, InetAddress hostAddress) {
        this.deviceId = wifiDirectMeshifyDevice.getMeshifyWifiDirectUUID();
        this.hostAdd = hostAddress.getHostAddress();
        this.socket = wifiDirectMeshifyDevice.getSocket();
        this.selectedClientPort = wifiDirectMeshifyDevice.getSelectedClientPort();
        this.listener = wifiDirectMeshifyDevice.getListener();
        this.wifiDirectStatusListener = wifiDirectMeshifyDevice.getWifiDirectStatusListener();
        this.username = wifiDirectMeshifyDevice.getUsername();
    }

    public void setSelectedClientPort(int selectedClientPort) {
        this.selectedClientPort = selectedClientPort;
    }

    private void setSocketListView(int[] clientArray) {
        clientPeerSocketList.clear();
        for (int socket : clientArray
        ) {
            clientPeerSocketList.add(socket);
        }
    }

    public List<Integer> getClientPeerSocketList() {
        return clientPeerSocketList;
    }

    public void write(GroupPacket packet) {
        try {
            Log.v("C", "Selected: " + selectedClientPort);
            if (selectedClientPort != 0) {
                GroupPacket groupMultihopPacket = new GroupPacket(packet.getMeshifyWifiUUID(), packet.getTextMessage(), selectedClientPort, socket.getLocalPort());
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(groupMultihopPacket);
            } else {
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(packet);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // connect with fixed port-> receive port number from Server ie group owner
    @Override
    public void run() {
        try {
            Log.w("HostAddress", hostAdd);
            socket = new Socket(hostAdd, 8888);

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            wifiDirectStatusListener.onWifiDirectStatusChange(new WifiDirectStatus("Client"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (socket != null) {
                    try {
                        // De-Serialize Group Packet
                        ObjectInputStream ois = new ObjectInputStream(inputStream);
                        GroupPacket groupPacket = (GroupPacket) ois.readObject();
                        // De-Serialize Group Packet

                        Log.w("Buffer", Arrays.toString(buffer));
                        if (groupPacket.getType() == 1) {
                            Log.w("V", "Object message-client " + groupPacket.getTextMessage());
                            if (groupPacket.getTextMessage() != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, groupPacket.getTextMessage());
                                        listener.onValueChanged(groupPacket);
                                    }
                                });
                            }
                        } else if (groupPacket.getType() == 2) {
                            Log.w("V", "Object config packet-portArray " + groupPacket.getGroupDevicePortArray() + " Client count: " + groupPacket.getGroupDevicePortArray().length);
                            setSocketListView(groupPacket.getGroupDevicePortArray());
                            listener.onValueChanged(groupPacket);
                        } else if (groupPacket.getType() == 3) {
                            Log.w("V", "Message From another client port: " + groupPacket.getOriginPort() + ": " + groupPacket.getTextMessage());
                            if (groupPacket.getTextMessage() != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, groupPacket.getOriginPort() + ": " + groupPacket.getTextMessage());
                                        listener.onValueChanged(groupPacket);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        wifiDirectStatusListener.onWifiDirectStatusChange(new WifiDirectStatus("Disconnected: Socket Timeout"));
                        break;
//                            throw new RuntimeException(e);
                    }
                }
            }
        });

    }
}
