//package com.manetdroid.meshify2.framework.controllers.wifidirect;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.net.NetworkInfo;
//import android.net.wifi.p2p.WifiP2pDeviceList;
//import android.net.wifi.p2p.WifiP2pInfo;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.util.Log;
//
//public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
//    private static final String TAG = "A";
//
//    private WifiP2pManager manager;
//    private WifiP2pManager.Channel channel;
//    private WifiDirectMeshifyDevice activity;
//
//    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiDirectMeshifyDevice activity) {
//        this.manager = manager;
//        this.channel = channel;
//        this.activity = activity;
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        Log.v("Test", "On Receive Fired");
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            // Check if WiFi is enabled
//
//        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//            // Call WifiP2pManager.requestPeers() to get a list current peers
//            Log.v("Test", "Connection Peer Changed");
//            if (manager != null) {
//                manager.requestPeers(channel, activity.peerListListener);
//            }
//
//        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//            // Respond to new connection or disconnection
//            Log.v("Test", "Connection Changed");
//            if (manager != null) {
//                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//                if (networkInfo.isConnected()) {
//                    manager.requestConnectionInfo(channel, activity.connectionInfoListener);
//                } else {
//                    Log.i(TAG, "Not Connected");
////                    activity.connectionStatus.setText("Not Connected");
//                }
//            }
//        }
//    }
//
//    // Implement necessary listeners for peer discovery and connection information
//
//    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
//        @Override
//        public void onPeersAvailable(WifiP2pDeviceList peers) {
//            // Process list of available peers
//            // Example: Update UI with the list of peers
////            activity.displayPeers(peers);
//        }
//    };
//
//    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
//        @Override
//        public void onConnectionInfoAvailable(WifiP2pInfo info) {
//            // Process connection information
//            // Example: Get group owner IP address, check if connected
//            Log.v("Test", info.toString());
//        }
//    };
//}
