package com.manetdroid.meshify2.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Represents content for Meshify entities, containing payload data and an identifier.
 */
public class MeshifyContent implements Parcelable {

    /**
     * Parcelable creator for generating instances of MeshifyContent from a Parcel.
     */
    public static final Creator<MeshifyContent> CREATOR = new Creator<MeshifyContent>() {
        @Override
        public MeshifyContent createFromParcel(Parcel in) {
            return new MeshifyContent(in);
        }

        @Override
        public MeshifyContent[] newArray(int size) {
            return new MeshifyContent[size];
        }
    };
    // Payload data stored as a HashMap with String keys and Object values
    private final HashMap<String, Object> payload;
    // Unique identifier for the MeshifyContent
    private String id;

    /**
     * Constructor for creating a MeshifyContent instance with payload and an identifier.
     *
     * @param payload Payload data stored in a HashMap
     * @param id      Unique identifier for the MeshifyContent
     */
    public MeshifyContent(HashMap<String, Object> payload, String id) {
        this.payload = payload;
        this.id = id;
    }

    /**
     * Parcelable constructor used for deserialization from a Parcel.
     *
     * @param in Parcel containing serialized MeshifyContent data
     */
    protected MeshifyContent(Parcel in) {
        id = in.readString();
        // Read payload as a HashMap from the Parcel
        payload = in.readHashMap(getClass().getClassLoader());
    }

    /**
     * Provides a description of the contents (required for Parcelable).
     *
     * @return Always returns 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes MeshifyContent data to a Parcel for serialization.
     *
     * @param dest  Parcel to which the data should be written
     * @param flags Additional flags (not used)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        // Write payload as a HashMap to the Parcel
        dest.writeMap(payload);
    }

    /**
     * Getter for retrieving the payload data.
     *
     * @return HashMap containing payload data
     */
    public HashMap<String, Object> getPayload() {
        return payload;
    }

    /**
     * Getter for retrieving the unique identifier.
     *
     * @return Unique identifier for the MeshifyContent
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for updating the unique identifier.
     *
     * @param id New unique identifier for the MeshifyContent
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Generates a JSON representation of the MeshifyContent using Gson.
     *
     * @return JSON string representing the MeshifyContent
     */
    public String toString() {
        return new Gson().toJson(this);
    }
}