package com.manetdroid.meshify2.framework.controllers.wifidirect;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerClass extends Thread {
    UUID deviceID;
    String[] socketArray;
    ServerSocket serverSocket;
    MultiServerThread selectedClient;
    List<MultiServerThread> clientArray = new ArrayList<MultiServerThread>();
    List<MultiServerThread> peerSocketList = new ArrayList<MultiServerThread>();
    private OutputDataListener listener;
    private OutputPortListener portListener;
    private WifiDirectStatusListener wifiDirectStatusListener;

    public ServerClass(WifiDirectMeshifyDevice wifiDirectMeshifyDevice) {
        // wifiDirectStatusListener, listener, portListener
        this.deviceID = wifiDirectMeshifyDevice.getMeshifyWifiDirectUUID();
        this.listener = wifiDirectMeshifyDevice.getListener();
        this.portListener = wifiDirectMeshifyDevice.getPortListener();
        this.wifiDirectStatusListener = wifiDirectMeshifyDevice.getWifiDirectStatusListener();
    }

    public String[] getSocketArray() {
        return socketArray;
    }

    public List<MultiServerThread> getPeerSocketList() {
        return peerSocketList;
    }

    public void setSelectedClient(MultiServerThread selectedClient) {
        this.selectedClient = selectedClient;
    }

    public void write(byte[] bytes) {
        try {
            if (selectedClient.getOutputStream() != null) {
                selectedClient.getOutputStream().write(bytes);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(GroupPacket packet) {
        try {
            if (selectedClient.getOutputStream() != null) {
                ObjectOutputStream os = new ObjectOutputStream(selectedClient.getOutputStream());
                os.writeObject(packet);
            }

        } catch (IOException e) {
            Log.e("S", e.toString());
        }
    }

    private void setSocketListView(MultiServerThread client) {
        peerSocketList.add(client);
        Log.v("S", peerSocketList + " " + client.getName());
        socketArray = new String[peerSocketList.size()];
        int i = 0;
        for (MultiServerThread socket : peerSocketList
        ) {
            socketArray[i] = socket.getName();
            i++;
        }
        Log.i("S-P:", socketArray.toString() + socketArray.length);
        portListener.onPortValueChanged(socketArray);
    }

    private void broadcastPorts() {

        for (MultiServerThread m : clientArray) {
            try {
                if (m == null) {
                    Log.e("S", "Error Config packet: ");
                    continue;
                }
                GroupPacket g = new GroupPacket(clientArray, m.getSocket().getPort());
                ObjectOutputStream os = new ObjectOutputStream(m.getOutputStream());
                os.writeObject(g);
            } catch (IOException e) {
                Log.e("S", "Error Config packet: " + m.getName());
                Log.e("S", e.toString());
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("S", "Error Config packet: " + m);
                Log.e("S", e.toString());
                e.printStackTrace();
            }

        }
    }

    // Get dynamic port from fixed port -> array of threads for connecting each client
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8888);

            while (true) {
                Log.v("S", "Server Socket ready...");
                Socket clientSocket = serverSocket.accept();

                Log.v("S", "Client accepted..." + clientSocket.getInetAddress() + " " + clientSocket.isConnected() + " " + clientSocket.getPort());
                MultiServerThread multiServerThread = new MultiServerThread(wifiDirectStatusListener, clientSocket, clientSocket.getPort(), this.clientArray, listener);
                multiServerThread.start();
                clientArray.add(multiServerThread);
                setSocketListView(multiServerThread);
                broadcastPorts();
                Log.v("S", "Thread Started");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
