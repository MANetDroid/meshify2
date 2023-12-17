package com.manetdroid.meshify2.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

/**
 * Represents a JSON response.
 */
public class ResponseJson implements Parcelable {

    public static final Creator<ResponseJson> CREATOR = new Creator<ResponseJson>() {
        @Override
        public ResponseJson createFromParcel(Parcel in) {
            return new ResponseJson(in);
        }

        @Override
        public ResponseJson[] newArray(int size) {
            return new ResponseJson[size];
        }
    };
    @JsonProperty(value = "type")
    private int type;
    @JsonProperty(value = "key")
    private String key;
    @JsonProperty(value = "uuid")
    private String uuid;

    /**
     * Default constructor.
     */
    public ResponseJson() {
    }

    protected ResponseJson(Parcel in) {
        type = in.readInt();
        uuid = in.readString();
        key = in.readString();
    }

    /**
     * Create a ResponseJson object with a general type.
     *
     * @param uuid The UUID value.
     * @return A ResponseJson object.
     */
    public static ResponseJson responseTypeGeneral(String uuid) {
        ResponseJson responseJson = new ResponseJson();
        responseJson.setUuid(uuid);
        responseJson.setType(0);
        return responseJson;
    }

    /**
     * Create a ResponseJson object with a key type.
     *
     * @param key The key value.
     * @return A ResponseJson object.
     */
    public static ResponseJson responseTypeKey(String key) {
        ResponseJson responseJson = new ResponseJson();
        responseJson.setType(1);
        responseJson.setKey(key);
        return responseJson;
    }

    @JsonProperty(value = "type")
    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @JsonProperty(value = "uuid")
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonProperty(value = "key")
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Convert the object to its JSON representation.
     *
     * @return A JSON string.
     */
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
        dest.writeInt(this.type);
        dest.writeString(this.uuid);
        dest.writeString(this.key);
    }
}
