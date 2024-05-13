package com.manetdroid.meshify2.framework.controllers.wifidirect;

import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WifiDirectMeshifyDevice {
    String username = null;
    String outputText = null;
    UUID meshifyWifiDirectUUID = UUID.randomUUID();
    String TAG = "A";
    MultiServerThread selectedClient;
    //    EditText writeMsg;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<MultiServerThread> peerSocketList = new ArrayList<MultiServerThread>();
    List<Integer> clientPeerSocketList = new ArrayList<Integer>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    // Sockets
    Socket socket;
    ServerClass serverClass;
    ClientClass clientClass;
    int selectedClientPort;
    boolean isHost;

    private OutputDataListener listener;
    private OutputPortListener portListener;
    private WifiDirectMeshifyDevice wifiDirectMeshifyDevice;

    private WifiDirectStatusListener wifiDirectStatusListener;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            Log.w("A", "Device");
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.w("A", "IsServer");

                isHost = true;
                if (serverClass == null) {
                    serverClass = new ServerClass(wifiDirectMeshifyDevice);
                    serverClass.start();
                    wifiDirectStatusListener.onWifiDirectStatusChange(new WifiDirectStatus("Server"));
                }

            } else if (wifiP2pInfo.groupFormed) {
                Log.w("A", "IsClient" + groupOwnerAddress);
                isHost = false;
                if (groupOwnerAddress != null) {
                    clientClass = new ClientClass(wifiDirectMeshifyDevice, groupOwnerAddress);
                    clientClass.start();
                }

            }
        }
    };
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    public WifiDirectMeshifyDevice(String username, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.wifiDirectMeshifyDevice = this;
        this.username = username;
        this.manager = manager;
        this.channel = channel;
    }

    public void setValueChangeListener(OutputDataListener listener) {
        this.listener = listener;
    }

    public void setValueChangePortListener(OutputPortListener portListener) {
        this.portListener = portListener;
    }

    public void setValueChangeWifiDirectStatusListener(WifiDirectStatusListener wifiDirectStatusListener) {
        this.wifiDirectStatusListener = wifiDirectStatusListener;
    }

    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return connectionInfoListener;
    }

    public String[] getDeviceNameArray() {
        return deviceNameArray;
    }

    public List<WifiP2pDevice> getPeers() {
        return peers;
    }

    public void writeTextMessage(String msg) {
        // This is a blocking method there concurrency is used
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (msg != null && isHost && serverClass != null) {
                    GroupPacket g = new GroupPacket(meshifyWifiDirectUUID, msg);
                    serverClass.write(g);
                } else if (msg != null && !isHost && clientClass != null) {
                    GroupPacket g = new GroupPacket(meshifyWifiDirectUUID, msg);
                    clientClass.write(g);
                } else {
                    Log.w("Error", String.valueOf(serverClass));
                    Log.w("Error", String.valueOf(clientClass));
                }
            }
        });
    }

    public void discover() {
        Log.i(TAG, "Discover Function called");
        Log.i(TAG, "Peer " + peers.toString());
        Log.i(TAG, "Manager " + manager.toString());
        peers.clear();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Discovery Started");
//                connectionStatus.setText("Discovery Started");
            }

            @Override
            public void onFailure(int i) {
                Log.i(TAG, "Discovery Not Started " + i);
//                connectionStatus.setText("Discovery Not Started " + i);
            }
        });
    }

    public void connectDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Log.w("device Config", String.valueOf(config));
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                try {
                    if (device != null) {
                        Log.w("X", "Connected: " + device.deviceAddress);
//                                connectionStatus.setText("Connected: " + device.deviceAddress);
                    }
                } catch (Exception e) {
                    Log.e("A", e.toString());
                }
            }

            @Override
            public void onFailure(int i) {
                Log.e("A", "Failed to Connect " + i);
//                        connectionStatus.setText("Failed to Connect");
            }
        });
    }

    public String[] getSocketArrayServer() {
        return serverClass.getSocketArray();
    }

    public List<Integer> getSocketArrayClient() {
        return clientClass.getClientPeerSocketList();
    }

    public boolean isHost() {
        return isHost;
    }

    public OutputDataListener getListener() {
        return listener;
    }

    public OutputPortListener getPortListener() {
        return portListener;
    }

    public WifiDirectStatusListener getWifiDirectStatusListener() {
        return wifiDirectStatusListener;
    }

    public String getUsername() {
        return username;
    }

    public String getOutputText() {
        return outputText;
    }

    public UUID getMeshifyWifiDirectUUID() {
        return meshifyWifiDirectUUID;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getSelectedClientPort() {
        return selectedClientPort;
    }

    public void setSelectedClientPort(int selectedClientPortIndex) {
        Log.i(TAG, "Port Position:- " + selectedClientPortIndex);
        if (isHost) {
            peerSocketList = serverClass.getPeerSocketList();
            Log.i(TAG, "Size Server:- " + peerSocketList.size());
            selectedClient = peerSocketList.get(selectedClientPortIndex);
            serverClass.setSelectedClient(selectedClient);
        } else {
            clientPeerSocketList = clientClass.getClientPeerSocketList();
            Log.i(TAG, "Size Client:- " + clientPeerSocketList.size());
            selectedClientPort = clientPeerSocketList.get(selectedClientPortIndex);
            clientClass.setSelectedClientPort(selectedClientPort);
        }
    }
}

