package com.manetdroid.meshify2.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class MeshifyHandshake implements Parcelable {

    public static final Creator<MeshifyHandshake> CREATOR = new Creator<MeshifyHandshake>() {
        @Override
        public MeshifyHandshake createFromParcel(Parcel in) {
            return new MeshifyHandshake(in);
        }

        @Override
        public MeshifyHandshake[] newArray(int size) {
            return new MeshifyHandshake[size];
        }
    };
    @JsonProperty(value = "rq")
    private Integer rq;  // Request
    @JsonProperty(value = "rp")
    private ResponseJson rp; // Response

    public MeshifyHandshake() {
        // Default constructor required for Parcelable
    }

    /**
     * Constructor for creating MeshifyHandshake.
     *
     * @param rq The request value, can be null.
     * @param rp The response value, can be null.
     */
    public MeshifyHandshake(@Nullable Integer rq, @Nullable ResponseJson rp) {
        // The parameters are marked as Nullable, meaning they can be null.
        // No need for null checks or Objects.requireNonNull.
        this.rq = rq;
        this.rp = rp;
    }

    protected MeshifyHandshake(Parcel in) {
        this.rq = in.readInt();
        this.rp = in.readParcelable(ResponseJson.class.getClassLoader());
    }

    @JsonProperty(value = "rq")
    public Integer getRq() {
        return this.rq;
    }

    public void setRq(Integer rq) {
        this.rq = rq;
    }

    @JsonProperty(value = "rp")
    public ResponseJson getRp() {
        return this.rp;
    }

    public void setRp(ResponseJson rp) {
        this.rp = rp;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rq);
        dest.writeParcelable(this.rp, flags);
    }
}