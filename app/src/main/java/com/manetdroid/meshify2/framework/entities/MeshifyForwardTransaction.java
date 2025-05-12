package com.manetdroid.meshify2.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.List;

/**
 * Represents a transaction within the Meshify framework.
 * It contains information about the sender, mesh entities, and reach.
 */
public class MeshifyForwardTransaction implements Parcelable {

    public static final Creator<MeshifyForwardTransaction> CREATOR = new Creator<MeshifyForwardTransaction>() {
        @Override
        public MeshifyForwardTransaction createFromParcel(Parcel in) {
            return new MeshifyForwardTransaction(in);
        }

        @Override
        public MeshifyForwardTransaction[] newArray(int size) {
            return new MeshifyForwardTransaction[size];
        }
    };
    @JsonProperty(value = "sender")
    String sender;
    @JsonProperty(value = "mesh")
    List<MeshifyForwardEntity> mesh;
    @JsonProperty(value = "reach")
    String reach;

    public MeshifyForwardTransaction() {

    }

    /**
     * Constructor for creating a MeshifyForwardTransaction with sender and mesh entities.
     *
     * @param sender The sender of the transaction.
     * @param mesh   List of MeshifyForwardEntity objects representing the mesh entities.
     */
    public MeshifyForwardTransaction(String sender, List<MeshifyForwardEntity> mesh) {
        this.sender = sender;
        this.mesh = mesh;
    }

    /**
     * Parcelable constructor for creating a MeshifyForwardTransaction from a Parcel.
     *
     * @param in The Parcel containing the serialized MeshifyForwardTransaction.
     */
    protected MeshifyForwardTransaction(Parcel in) {
        this.sender = in.readString();
        this.mesh = in.createTypedArrayList(MeshifyForwardEntity.CREATOR);
        this.reach = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sender);
        dest.writeTypedList(this.mesh);
        dest.writeString(this.reach);
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    @JsonProperty(value = "sender")
    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty(value = "mesh")
    public List<MeshifyForwardEntity> getMesh() {
        return this.mesh;
    }

    public void setMesh(List<MeshifyForwardEntity> mesh) {
        this.mesh = mesh;
    }

    @JsonProperty(value = "reach")
    public String getReach() {
        return this.reach;
    }

    public void setReach(String reach) {
        this.reach = reach;
    }
}
