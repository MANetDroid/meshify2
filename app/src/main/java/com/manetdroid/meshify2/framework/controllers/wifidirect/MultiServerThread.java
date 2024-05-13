package com.manetdroid.meshify2.framework.controllers.wifidirect;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiServerThread extends Thread {
    String TAG = "[Meshify][MultiServerThread]";
    UUID deviceId;
    String deviceName;
    List<MultiServerThread> clientArray = new ArrayList<MultiServerThread>();
    private Socket socket = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputDataListener listener;
    private WifiDirectStatusListener wifiDirectStatusListener;

    public MultiServerThread(WifiDirectStatusListener wifiDirectStatusListener, Socket socket, int name, List<MultiServerThread> clientArray, OutputDataListener listener) {
        super(String.valueOf(name));
        Log.w("S", "Client ID: " + socket.getLocalPort() + " " + socket.getPort());
        this.socket = socket;
        this.clientArray = clientArray;
        this.listener = listener;
    }

    public Socket getSocket() {
        return socket;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(GroupPacket packet, Socket socket) {
        try {
            if (socket.getOutputStream() != null) {
                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                os.writeObject(packet);
            }

        } catch (IOException e) {
            Log.e("S", e.toString());
        }
    }

    public void forwardToPeer(GroupPacket forwardPacket) {
        for (MultiServerThread m : clientArray
        ) {
            if (m.getSocket().getPort() == forwardPacket.getPort()) {
                Log.v("V", "SEND THIS TO " + forwardPacket.getPort());
                m.write(forwardPacket, m.getSocket());
            }
        }
    }

    public void run() {

        try {
            Log.v("T", "MultithreadStarted");
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();


            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(inputStreamReader);

            String outputLine;


            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;

                    while (socket != null) {
                        try {
                            Log.w("Buffer", Arrays.toString(buffer));
                            Log.w("IStream", inputStream.toString());
//                                bytes = inputStream.read(buffer);

                            // De-Serialize Group Packet
                            ObjectInputStream ois = new ObjectInputStream(inputStream);
                            GroupPacket groupPacket = (GroupPacket) ois.readObject();
                            // De-Serialize Group Packet
                            Log.w("V", "Object message " + groupPacket.getType());

                            if (groupPacket.getType() == 1) {
                                Log.w("V", "Object message-server " + groupPacket.getTextMessage());
                                if (groupPacket.getTextMessage() != null) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i(TAG, socket.getPort() + ": " + groupPacket.getTextMessage());
//                                                read_msg_box.setText(socket.getPort() + ": " + groupPacket.getTextMessage());
                                            if (listener != null) {
                                                // Add to Calllback
                                                listener.onValueChanged(groupPacket);
                                            }

                                        }
                                    });
                                }
                            } else if (groupPacket.getType() == 3) {
                                Log.w("V", "Object message-Send " + groupPacket.getTextMessage() + " To Port: " + groupPacket.getPort());
                                forwardToPeer(groupPacket);
                            }


                        } catch (Exception e) {
//                                setSocketStatus(socketStatus, "Socket Disconnected: Timeout");
                            Log.e(TAG, "Socket Disconnected: Timeout");
                            Log.e("V", String.valueOf(e));
                            wifiDirectStatusListener.onWifiDirectStatusChange(new WifiDirectStatus("Disconnected: Socket Timeout"));
                            break;
                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

