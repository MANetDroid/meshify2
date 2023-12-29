package com.manetdroid.meshify2.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.manetdroid.meshify2.api.exceptions.MeshifyException;
import com.manetdroid.meshify2.api.profile.DeviceProfile;
import com.manetdroid.meshify2.framework.controllers.MeshifyCore;
import com.manetdroid.meshify2.logs.Log;

import java.util.HashMap;

public class MeshifyClient {
    private static final String TAG = "[Meshify][MeshifyClient]";

    private final String userUuid;

    private final String apiKey;

    private final String publicKey;

    private final String secretKey;

    private final String phone;

    private final DeviceProfile deviceProfile;

    private long ownSequenceNum;

    private MeshifyClient(Builder builder) {
        this.userUuid = builder.userUuid;
        this.apiKey = builder.apiKey;
        this.publicKey = builder.publicKey;
        this.secretKey = builder.secretKey;
        this.phone = builder.phone;
        this.deviceProfile = builder.deviceProfile;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getUserUuid() {
        return this.userUuid;
    }

    public String getPhone() {
        return this.phone;
    }

    /**
     * Method allowing to increment the sequence number
     */
    public long getNextSequenceNumber() {
        if (ownSequenceNum < Constants.MAX_VALID_SEQ_NUM) {
            ++ownSequenceNum;
        } else {
            ownSequenceNum = Constants.MIN_VALID_SEQ_NUM;
        }
        return ownSequenceNum;
    }

    static class Builder {

        private final SharedPreferences sharedPreferences;

        private final SharedPreferences.Editor editor;
        private final DeviceProfile deviceProfile;
        private final long ownSequenceNum;
        private String userUuid;
        private String apiKey;
        private String publicKey;
        private String secretKey;
        private String phone = null;

        Builder(Context context) {
            if (context != null) {
                Context applicationContext = context.getApplicationContext();
                this.sharedPreferences = applicationContext.getSharedPreferences(MeshifyCore.PREFS_NAME, 0);
                this.editor = sharedPreferences.edit();
                this.ownSequenceNum = Constants.FIRST_SEQUENCE_NUMBER;
                this.deviceProfile = new DeviceProfile(applicationContext);
                return;
            }
            throw new IllegalArgumentException("Context can not be null.");
        }

        public Builder setUserUuid(String userUuid) {
            this.userUuid = userUuid;
            return this;
        }

        public Builder generateKeyPair() throws MeshifyException {
            try {
                HashMap<String, String> hashMap = MeshifyRSA.generateKeyPair();
                this.publicKey = hashMap.get("public");
                this.secretKey = hashMap.get("secret");
                Log.d(TAG, "... generating new key pair");
                this.editor.putString(MeshifyCore.PREFS_PUBLIC_KEY, this.publicKey);
                this.editor.putString(MeshifyCore.PREFS_PRIVATE_KEY, this.secretKey);
            } catch (Exception e) {
                throw new MeshifyException(Constants.KEY_PAIR_GENERATION, Constants.ERROR_KEY_PAIR_GENERATION);
            } return this;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder setKeys() {
            this.publicKey = this.sharedPreferences.getString(MeshifyCore.PREFS_PUBLIC_KEY, null);
            this.secretKey = this.sharedPreferences.getString(MeshifyCore.PREFS_PRIVATE_KEY, null);
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public MeshifyClient build() {
            this.editor.putString(MeshifyCore.PREFS_USER_UUID, this.userUuid);
            this.editor.apply();
            return new MeshifyClient(this);
        }
    }

}
