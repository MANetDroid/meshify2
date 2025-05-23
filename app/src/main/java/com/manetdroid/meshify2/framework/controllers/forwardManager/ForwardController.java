package com.manetdroid.meshify2.framework.controllers.forwardManager;

import android.os.AsyncTask;

import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.framework.controllers.MeshifyCore;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.Session;
import com.manetdroid.meshify2.framework.controllers.sessionmanager.SessionManager;
import com.manetdroid.meshify2.framework.entities.MeshifyEntity;
import com.manetdroid.meshify2.framework.entities.MeshifyForwardEntity;
import com.manetdroid.meshify2.framework.entities.MeshifyForwardTransaction;
import com.manetdroid.meshify2.logs.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ForwardController {

    public final String TAG = "[Meshify][ForwardController]";

    // DD Cache for storing MeshifyForwardEntity and reached status
    private final ConcurrentNavigableMap<MeshifyForwardEntity, Boolean> meshNavigableMap = new ConcurrentSkipListMap<MeshifyForwardEntity, Boolean>(); // DD Cache
    private final ConcurrentNavigableMap<String, Boolean> reachedNavigableMap = new ConcurrentSkipListMap<String, Boolean>();

    ForwardController() {
    }

    // Add MeshifyForwardEntity to the list for forwarding
    void addForwardEntitiesToList(MeshifyForwardEntity forwardEntity, boolean z) {

        if (this.reachedNavigableMap.containsKey(forwardEntity.getId())) { // check already in the reached list
            return;
        }

        synchronized (meshNavigableMap) {

            if (this.checkAvailability(forwardEntity) != null) {
                Log.e(TAG, "Forward Entity already in the mesh map");
                return;
            }

            this.discardExpiredEntities(); //remove expire entities

            MeshifyForwardEntity selectedEntity = null;

            if (this.meshNavigableMap.size() < 10) { // TODO - Queue Size
                selectedEntity = forwardEntity;
            } else {
                MeshifyForwardEntity oldestEntity = null;
                for (MeshifyForwardEntity entity : this.meshNavigableMap.descendingKeySet()) {
                    if (oldestEntity == null) {
                        oldestEntity = entity;
                        continue;
                    }
                    if (oldestEntity.getAdded().getTime() <= entity.getAdded().getTime()) continue;
                    oldestEntity = entity;
                }
                if (oldestEntity.getAdded().getTime() < forwardEntity.getAdded().getTime()) {
                    this.meshNavigableMap.remove(oldestEntity); // delete oldest entity
                    selectedEntity = forwardEntity;
                }
            }
            if (selectedEntity != null) {
                this.meshNavigableMap.put(selectedEntity, true);
            } else {
                this.reachedNavigableMap.put(forwardEntity.getId(), Boolean.TRUE);
            }
        }

        if (z) {
            this.sendEntity(SessionManager.getSessions(), false); // forward to all available neighbors
        }
    }

    private void discardExpiredEntities() {

        synchronized (meshNavigableMap) {

            ArrayList<MeshifyForwardEntity> removeArrayList = new ArrayList<MeshifyForwardEntity>();

            for (MeshifyForwardEntity entity : this.meshNavigableMap.descendingKeySet()) {
                if (!entity.expired()) continue;
                removeArrayList.add(entity);
            }

            if (removeArrayList.size() > 0) {
                Log.e(TAG, "discardExpiredEntities: remove MeshifyForwardEntities: " + removeArrayList.size());
            }

            ArrayList<String> discardArrayList = new ArrayList<>();
            for (MeshifyForwardEntity forwardEntity : removeArrayList) {
                this.meshNavigableMap.remove(forwardEntity);
                discardArrayList.add(forwardEntity.getId());
            }

            Iterator<String> iterator = discardArrayList.iterator();
            while (iterator.hasNext()) {
                String entityId = (String) iterator.next();
                this.reachedNavigableMap.put(entityId, Boolean.TRUE);
                Log.e(TAG, "discardExpiredEntities: discard id" + entityId);
            }
        }
    }


    private MeshifyForwardEntity checkAvailability(MeshifyForwardEntity forwardEntity) {
        if (this.meshNavigableMap.containsKey(forwardEntity)) {
            for (MeshifyForwardEntity forwardEntity1 : this.meshNavigableMap.descendingKeySet()) {
                if (!forwardEntity1.equals(forwardEntity)) continue;
                return forwardEntity1;
            }
        }
        return null;
    }

    public void processReach(String reach) {
        this.processReach(this.getEntityFromUuid(reach), true);
    }

    private void processReach(MeshifyForwardEntity meshifyForwardEntity, boolean bl) {

        if (meshifyForwardEntity != null) {
            this.meshNavigableMap.remove(meshifyForwardEntity);

            if (bl) {
                this.reachedNavigableMap.put(meshifyForwardEntity.getId(), Boolean.TRUE);
            }
        }

    }

    private List<MeshifyForwardEntity> getMessageList() {
        ArrayList<MeshifyForwardEntity> arrayList = new ArrayList<>();
        for (MeshifyForwardEntity forwardEntity : this.meshNavigableMap.descendingKeySet()) {
            arrayList.add(forwardEntity);
        }
        return arrayList;
    }

    MeshifyForwardEntity getEntityFromUuid(String string) {
        for (MeshifyForwardEntity meshifyForwardEntity : this.meshNavigableMap.descendingKeySet()) {
            if (!meshifyForwardEntity.getId().equalsIgnoreCase(string.trim())) continue;
            return meshifyForwardEntity;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    void sendEntity(ArrayList<Session> arrayList, boolean z) {
        new sendEntityToSession(z).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList.toArray(new Session[arrayList.size()]));
    }

    void forwardAgain(ArrayList<MeshifyForwardEntity> entityArrayList, Session session) {
        for (MeshifyForwardEntity forwardEntity : entityArrayList) {
            if (this.reachedNavigableMap.containsKey(forwardEntity.getId()) || this.meshNavigableMap.containsKey(forwardEntity)) {
                Log.e(TAG, "forwardAgain: Not forwarding duplicated or already reached entity " + forwardEntity);
            } else {
                this.addForwardEntitiesToList(forwardEntity, false); //adding all message entities without start forwarding
            }
        }
        this.sendEntity(SessionManager.getSessions(), false); //start forwarding all message entities
    }

    protected boolean forwardAgain(MeshifyForwardEntity forwardEntity) {
        return this.reachedNavigableMap.containsKey(forwardEntity.getId()) || this.meshNavigableMap.containsKey(forwardEntity);
    }

    public void sendReach(MeshifyForwardEntity forwardEntity) {
        MeshifyEntity<MeshifyForwardTransaction> meshifyEntity = MeshifyEntity.reachMessage(forwardEntity.getId());
        for (Session session : SessionManager.getSessions()) {
            try {
                Log.e(TAG, "sendReach: sending " + meshifyEntity);
                MeshifyCore.sendEntity(session, meshifyEntity); // sending reached message
            } catch (Exception exception) {
                Log.e(TAG, "sendReach: Error sending reach message", exception);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private class sendEntityToSession extends AsyncTask<Session, Void, Void> {
        private boolean z = false;

        sendEntityToSession(boolean z) {
            this.z = z;
        }

        @Override
        protected Void doInBackground(Session... sessions) {
            if (ForwardController.this.meshNavigableMap.size() > 0 || this.z) {
                ArrayList<Session> arrayList = new ArrayList<>();
                for (Session session : sessions) {
                    if (session == null || session.getUserId() == null) continue;
                    arrayList.add(session);
                }
                for (Session session : arrayList) {
                    List list = ForwardController.this.getMessageList();
                    Log.d(TAG, "Sending " + list.size() + " messages to: " + session.getDevice().getDeviceName());
                    if (list.size() > 0 || this.z) {
                        MeshifyEntity<MeshifyForwardTransaction> meshifyEntity = MeshifyEntity.meshMessage((ArrayList) list, Meshify.getInstance().getMeshifyClient().getUserUuid());
                        try {
                            MeshifyCore.sendEntity(session, meshifyEntity); // forwarding message
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
    }

}
