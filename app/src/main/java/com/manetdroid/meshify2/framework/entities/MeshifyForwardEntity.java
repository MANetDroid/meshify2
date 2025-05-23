package com.manetdroid.meshify2.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.manetdroid.meshify2.api.ConfigProfile;
import com.manetdroid.meshify2.api.Message;
import com.manetdroid.meshify2.logs.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;


/**
 * Represents an entity for forwarding within the Meshify framework.
 * Implements Parcelable for Android inter-process communication.
 * Implements Comparable for sorting based on creation time.
 */
public class MeshifyForwardEntity implements Parcelable, Comparable {

    public static final Creator<MeshifyForwardEntity> CREATOR = new Creator<MeshifyForwardEntity>() {
        @Override
        public MeshifyForwardEntity createFromParcel(Parcel in) {
            return new MeshifyForwardEntity(in);
        }

        @Override
        public MeshifyForwardEntity[] newArray(int size) {
            return new MeshifyForwardEntity[size];
        }
    };
    private static final int[] hopLimits = new int[]{50, 1, 20, 10};
    private static final int[] sharingTime = new int[]{15000, 10000, 10000, 10000};
    @JsonProperty(value = "id")
    String id;
    @JsonProperty(value = "payload")
    HashMap<String, Object> payload;
    @JsonProperty(value = "sender")
    String sender;
    @JsonProperty(value = "receiver")
    String receiver;
    @JsonProperty(value = "created_at")
    long created_at;
    @JsonProperty(value = "hops")
    int hops;
    @JsonProperty(value = "profile")
    int profile;
    @JsonProperty(value = "mesh_type")
    int mesh_type;
    @JsonIgnore
    Date added;

    /**
     * Constructor for creating a MeshifyForwardEntity from a Message, mesh type, and profile.
     *
     * @param message   The Message to create the entity from.
     * @param mesh_type The mesh type.
     * @param profile   The profile for hop limit and sharing time.
     */
    public MeshifyForwardEntity(Message message, int mesh_type, ConfigProfile profile) {
        this.id = message.getUuid() == null ? UUID.randomUUID().toString() : message.getUuid();
        this.payload = message.getContent();
        this.sender = message.getSenderId();
        this.receiver = message.getReceiverId();
        this.created_at = message.getDateSent();
        this.hops = this.getHopLimitForConfigProfile();
        this.profile = profile.ordinal();
        this.mesh_type = mesh_type;
        this.added = new Date(System.currentTimeMillis());
    }

    protected MeshifyForwardEntity(Parcel in) {
        this.id = in.readString();
        this.payload = new Gson().fromJson(in.readString(), new TypeToken<HashMap<String, Object>>() {
        }.getType());
        this.sender = in.readString();
        this.receiver = in.readString();
        this.created_at = in.readLong();
        this.hops = in.readInt();
        this.profile = in.readInt();
        this.mesh_type = in.readInt();
        this.added = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(new Gson().toJson(this.payload));
        dest.writeString(this.sender);
        dest.writeString(this.receiver);
        dest.writeLong(this.created_at);
        dest.writeInt(this.hops);
        dest.writeInt(this.profile);
        dest.writeInt(this.mesh_type);
        if (this.added == null) {
            this.added = new Date(System.currentTimeMillis());
        }
        dest.writeLong(this.added.getTime());
    }

    public String toString() {
        return new Gson().toJson((Object) this);
    }

    @JsonProperty(value = "id")
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty(value = "payload")
    public HashMap<String, Object> getPayload() {
        return this.payload;
    }

    public void setPayload(HashMap<String, Object> payload) {
        this.payload = payload;
    }

    @JsonProperty(value = "sender")
    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty(value = "receiver")
    public String getReceiver() {
        return this.receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @JsonProperty(value = "created_at")
    public long getCreatedAt() {
        return this.created_at;
    }

    public void setCreatedAt(long date_sent) {
        this.created_at = date_sent;
    }

    @JsonProperty(value = "mesh_type")
    public int getMeshType() {
        return this.mesh_type;
    }

    @JsonProperty(value = "hops")
    public int getHops() {
        return this.hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public int decreaseHops() {
        return --this.hops;
    }

    @JsonProperty(value = "profile")
    public int getProfile() {
        return this.profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    @JsonIgnore
    public int getHopLimitForConfigProfile() {
        return hopLimits[this.profile];
    }

    @JsonIgnore
    int getSharingTimeForConfigProfile() {
        return sharingTime[this.profile] * 1000;
    }

    public Date getAdded() {
        return this.added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    @Override
    public int compareTo(Object object) {
        MeshifyForwardEntity forwardEntity = (MeshifyForwardEntity) object;
        return ("" + forwardEntity.getCreatedAt()).compareTo("" + this.getCreatedAt());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof MeshifyForwardEntity) {
            MeshifyForwardEntity forwardEntity = (MeshifyForwardEntity) obj;
            return forwardEntity.getId().trim().equalsIgnoreCase(this.getId().trim());
        }
        assert obj != null;
        throw new IllegalArgumentException(obj.getClass().getCanonicalName() + " is not a instance of " + this.getClass().getName());
    }

    /**
     * Checks if the entity has expired based on hop count and sharing time.
     *
     * @return True if expired, false otherwise.
     */
    public boolean expired() {
        if (this.getHops() <= 0) {
            Log.e("[Meshify]", "expired because: " + this.getId() + " hops " + this.getHops());
            return true;
        }
        long period = new Date(System.currentTimeMillis()).getTime() - this.added.getTime();
        if (period > (long) this.getSharingTimeForConfigProfile()) {
            Log.e("[Meshify]", "expired because: " + this.getId() + " sharing period: " + period + " > " + this.getSharingTimeForConfigProfile() + " | profile : " + this.getProfile());
            return true;
        }
        return false;
    }
}
