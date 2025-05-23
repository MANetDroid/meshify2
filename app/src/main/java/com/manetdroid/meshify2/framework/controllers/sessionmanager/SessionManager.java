package com.manetdroid.meshify2.framework.controllers.sessionmanager;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Device;
import com.manetdroid.meshify2.framework.controllers.discoverymanager.DeviceManager;
import com.manetdroid.meshify2.logs.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

public class SessionManager {
    private static final String TAG = "[Meshify][SessionManager]";

//    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 9, 3L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static ConcurrentSkipListMap<String, Session> sessionMap = new ConcurrentSkipListMap<>();

    private SessionManager() {
    }

    public static ArrayList<Session> getSessions() {
        return new ArrayList<Session>(sessionMap.values());
    }

    public static void queueSession(Session session) {
        if (!sessionMap.containsKey(session.getSessionId())) {
            Log.d(TAG, "queueSession: " + session.getSessionId() + " -> first time.");
            session.setConnected(true);
            if (session.getAntennaType() != Config.Antenna.BLUETOOTH_LE) {
                session.run();
            }
            sessionMap.put(session.getSessionId(), session);
        }
    }

    public static synchronized Session getSession(String Id) {
        if (Id == null) {
            return null;
        }
        Session session = sessionMap.get(Id);
        if (session != null) {
            return session;
        }
        for (Session session2 : sessionMap.values()) {
            if (session2.getDevice() == null || session2.getUserId() == null || !session2.getUserId().equals(Id) && !session2.getDevice().getDeviceAddress().equalsIgnoreCase(Id))
                continue;
            return session2;
        }
        return session;
    }

    public static void removeSession(String string) {

        Session session = SessionManager.getSession(string);
        if (session != null && session.getState() != 1) {
            SessionManager.removeQueueSession(session);
        }
    }

    static void removeQueueSession(Session session) {
        Log.e(TAG, "Remove Session: id - " + session.getSessionId());

        Device device = session.getDevice();
        if (session.getEmitter() != null) {
            Log.i(TAG, "removeQueueSession: disconnected");
            if (!session.getEmitter().isDisposed()) {
                session.getEmitter().tryOnError(new Exception("Connection closed"));
            }
        }
        sessionMap.remove(session.getSessionId());
        DeviceManager.removeDevice(device);
    }

    public static void removeAllSessions(Config.Antenna antenna) {
        for (Session session : sessionMap.values()) {
            if (antenna != session.getAntennaType()) continue;
            session.removeSession();
        }
    }

}
