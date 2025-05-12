package com.manetdroid.meshify2.api;

/**
 * Represents the configuration for Meshify.
 */
public class Config {

    private final boolean isEncryption;
    private final boolean isVerified;
    private final boolean neighborDiscovery;
    private Antenna antennaType;
    private final ConfigProfile configProfile;
    private final int maxConnectionRetries;
    private final boolean isAutoConnect;

    private Config(Builder builder) {
        this.isEncryption = builder.isEncryption;
        this.isVerified = builder.isVerified;
        this.neighborDiscovery = builder.neighborDiscovery;
        this.configProfile = builder.configProfile;
        this.antennaType = builder.antennaType;
        this.maxConnectionRetries = builder.maxConnectionRetries;
        this.isAutoConnect = builder.isAutoConnect;
    }

    /**
     * Checks if encryption is enabled.
     */
    public boolean isEncryption() {
        return this.isEncryption;
    }

    /**
     * Checks if verification is enabled.
     */
    public boolean isVerified() {
        return this.isVerified;
    }

    /**
     * Gets the configuration profile.
     */
    public ConfigProfile getConfigProfile() {
        return this.configProfile;
    }

    /**
     * Gets the antenna type.
     */
    public Antenna getAntennaType() {
        return this.antennaType;
    }

    /**
     * Gets the maximum connection retries.
     */
    public int getMaxConnectionRetries() {
        return this.maxConnectionRetries;
    }

    /**
     * Checks if auto-connect is enabled.
     */
    public boolean isAutoConnect() {
        return this.isAutoConnect;
    }

    /**
     * Checks if neighbor discovery is enabled.
     */
    public boolean isNeighborDiscovery() {
        return neighborDiscovery;
    }

    /**
     * Enum representing antenna types.
     */
    public enum Antenna {
        BLUETOOTH,
        BLUETOOTH_LE,
        UNREACHABLE
    }
    public void setAntennaType(Antenna antennaType) {
        this.antennaType = antennaType;
    }
    /**
     * Builder class for constructing Config instances.
     */
    public static final class Builder {

        private boolean isEncryption = false;
        private boolean isVerified = false;
        private boolean neighborDiscovery = false;
        private ConfigProfile configProfile = ConfigProfile.Default;
        private Antenna antennaType = Antenna.BLUETOOTH_LE;
        private int maxConnectionRetries = 10;
        private boolean isAutoConnect = true;

        /**
         * Builds the Config instance.
         */
        public Config build() {
            return new Config(this);
        }

        /**
         * Sets the encryption flag.
         */
        public Builder setEncryption(boolean encryption) {
            this.isEncryption = encryption;
            return this;
        }

        /**
         * Sets the verification flag.
         */
        public Builder setVerified(boolean verified) {
            this.isVerified = verified;
            return this;
        }

        /**
         * Sets the neighbor discovery flag.
         */
        public Builder setNeighborDiscovery(boolean neighborDiscovery) {
            this.neighborDiscovery = neighborDiscovery;
            return this;
        }

        /**
         * Sets the configuration profile.
         */
        public Builder setConfigProfile(ConfigProfile configProfile) {
            this.configProfile = configProfile;
            return this;
        }

        /**
         * Sets the antenna type
         */
        public Builder setAntennaType(Antenna antennaType) {
            this.antennaType = antennaType;
            return this;
        }

        /**
         * Sets the max Connection Retries.
         */
        public Builder setmaxConnectionRetries(int maxConnectionRetries) {
            this.maxConnectionRetries = maxConnectionRetries;
            return this;
        }

        /**
         * Sets the Auto Connection.
         */
        public Builder setAutoConnect(boolean isAutoConnect) {
            this.isAutoConnect = isAutoConnect;
            return this;
        }
    }
}

