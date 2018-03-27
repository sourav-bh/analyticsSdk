package com.rokin.mobile.analytics;

import android.location.Location;

import com.rokin.mobile.analytics.utils.Utils;

/**
 * A class for providing the required configurations to use AnalyticsSDK.
 * <br>
 * <br>
 * To use this class user need first build an object of {@link Configuration.ConfigurationBuilder ConfigurationBuilder}.
 *
 * @author Sourav
 * @see AnalyticsManager
 * @since version 1.0.0
 */
public class Configuration {
    private final String applicationName; // required
    private final String appVersion; // required

    private final String userID; // optional
    private final Location currentLocation; // optional
    private final boolean isDebuggable; // optional
    private final boolean isLogFileEnable; // optional
    private final boolean isUploadEnable; // optional
    private final int logUploadingInterval; // optional
    private final boolean isBundleGPSDataUpload; // optional

    private Configuration(ConfigurationBuilder builder) {
        this.applicationName = builder.applicationName;
        this.appVersion = builder.appVersion;
        this.userID = builder.userID;
        this.currentLocation = builder.currentLocation;
        this.isDebuggable = builder.isDebuggable;
        this.isLogFileEnable = builder.isLogFileEnable;
        this.isUploadEnable = builder.isUploadEnable;
        this.logUploadingInterval = builder.logUploadingInterval;
        this.isBundleGPSDataUpload = builder.isBundleGPSDataUpload;
    }

    /**
     * @return the name of the application
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @return the version of the application
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @return the user ID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @return the current location information
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * @return true if debugging is enabled, otherwise false
     */
    public boolean isDebuggable() {
        return isDebuggable;
    }

    /**
     * @return true if it file logging is enabled, otherwise false
     */
    public boolean isLogFileEnable() {
        return isLogFileEnable;
    }

    /**
     * @return true if uploading is enabled, otherwise false
     */
    public boolean isUploadEnable() {
        return isUploadEnable;
    }

    /**
     * @return the log uploading interval time
     */
    public int getLogUploadingInterval() {
        return logUploadingInterval;
    }

    /**
     * @return the is GPSData should upload in bundle or not
     */
    public boolean isBundleGPSDataUpload() {
        return isBundleGPSDataUpload;
    }

    /**
     * Determines if the given configuration object is similar to the existing or not.
     *
     * @param configuration the new configuration object
     * @return true if the configuration objects are similar, otherwise false
     */
    public boolean equals(Configuration configuration) {
        if (this.applicationName.equalsIgnoreCase(configuration.getApplicationName()) &&
            this.appVersion.equalsIgnoreCase(configuration.getAppVersion()) &&
            this.userID.equalsIgnoreCase(configuration.getUserID())) {
            return true;
        }
        return false;
    }

    /**
     * Builder class for creating the configuration object for use of AnalyticsSDK.
     *
     * @author Sourav
     * @see Configuration
     * @since version 2.0.0
     */
    public static class ConfigurationBuilder {
        private final String applicationName;
        private final String appVersion;

        private String userID = null;
        private Location currentLocation = null;
        private boolean isDebuggable = true;
        private boolean isLogFileEnable = true;
        private boolean isUploadEnable = true;
        private int logUploadingInterval = 10;
        private boolean isBundleGPSDataUpload = true;

        /**
         * Constructs a builder object to create a {@link Configuration Configuration} object
         *
         * @param applicationName   the name of the application
         * @param appVersion        the version of the application
         */
        public ConfigurationBuilder(String applicationName, String appVersion) {
            this.applicationName = applicationName;
            this.appVersion = appVersion;
        }

        /**
         * @param userID the user ID to set
         * @return the resulting builder object
         */
        public ConfigurationBuilder setUserID(String userID) {
            this.userID = userID;
            return this;
        }

        /**
         * @param currentLocation the current location information to set
         * @return the resulting builder object
         */
        public ConfigurationBuilder setCurrentLocation(Location currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        /**
         * @param isDebuggable the is debugging enable value to set
         * @return the resulting builder object
         */
        public ConfigurationBuilder setIsDebuggable(boolean isDebuggable) {
            this.isDebuggable = isDebuggable;
            return this;
        }

        /**
         * @param isLogFileEnable the is file logging enable value to set
         * @return the resulting builder object
         */
        public ConfigurationBuilder setLogFileEnable(boolean isLogFileEnable) {
            this.isLogFileEnable = isLogFileEnable;
            return this;
        }

        /**
         * @param isUploadEnable the is uploading enable value to set
         * @return the resulting builder object
         */
        public ConfigurationBuilder setUploadEnable(boolean isUploadEnable) {
            this.isUploadEnable = isUploadEnable;
            return this;
        }

        /**
         * @param logUploadingInterval the log uploading interval time in milliseconds
         * @return the resulting builder object
         * @throws IllegalArgumentException if the interval value is less than 0
         */
        public ConfigurationBuilder setLogUploadingInterval(int logUploadingInterval) {
            if (logUploadingInterval <= 0) {
                throw new IllegalArgumentException("The interval should be greater that 0");
            } else {
                this.logUploadingInterval = logUploadingInterval;
                return this;
            }
        }

        /**
         * @param isBundleGPSDataUpload the value set for is GPS Data uploading as bundle or not
         * @return the resulting builder object
         */
        public ConfigurationBuilder setIsBundleGPSDataUpload(boolean isBundleGPSDataUpload) {
            this.isBundleGPSDataUpload = isBundleGPSDataUpload;
            return this;
        }

        /**
         * Completely build the {@link Configuration Configuration} object with provided values.
         *
         * @return the built configuration object
         * @throws IllegalArgumentException if the application context is NULL
         * @throws IllegalArgumentException if the Analytics Server URL is NULL
         * @throws IllegalArgumentException if the application name is NULL
         * @throws IllegalArgumentException if the application version is NULL
         */
        public Configuration build() {
            Configuration configuration = new Configuration(this);
            if (Utils.isEmptyString(configuration.getApplicationName())) {
                throw new IllegalArgumentException("Application Name is missing");
            } else if (Utils.isEmptyString(configuration.getAppVersion())) {
                throw new IllegalArgumentException("Application Version is missing");
            } else {
                return configuration;
            }
        }
    }
}
