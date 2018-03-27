package com.rokin.mobile.analytics;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Hashtable;

import com.rokin.mobile.analytics.constant.AppConstant;
import com.rokin.mobile.analytics.data.MLocation;
import com.rokin.mobile.analytics.db.AnalyticsLogDBManager;
import com.rokin.mobile.analytics.db.AnalyticsTableOpenHelper;
import com.rokin.mobile.analytics.location.FusedLocationManager;
import com.rokin.mobile.analytics.main.AnalyticsLogProcessor;
import com.rokin.mobile.analytics.network.RequestHandler;
import com.rokin.mobile.analytics.utils.DateTimeUtil;
import com.rokin.mobile.analytics.utils.DeviceInfo;
import com.rokin.mobile.analytics.utils.FileLogger;
import com.rokin.mobile.analytics.utils.Logger;
import com.rokin.mobile.analytics.utils.Utils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * A singleton class for handling all of the Analytics Server APIs from an client application.
 * Other log related information (i.e. GPS info, Trip Summary) also can be found from this class.
 * <br>
 * <br>
 * To access the Analytics API and information user must need to provide the application context and some required configurations.
 *
 * @author Sourav
 * @see Configuration
 * @since version 1.0.0
 */
public class AnalyticsManager {
    private static AnalyticsManager instance;
    private Context context;
    private Configuration configuration;

    private boolean isSDKInitialized = false;
    private String sessionStartUpTimeStamp;

    private AnalyticsLogProcessor logProcessor;

    private AnalyticsManager(Context context, Configuration configuration, RequestHandler requestHandler) {
        this.configuration = configuration;
        this.context = context;

        if (DeviceInfo.isGooglePlayServicesAvailable(context)) {
            FusedLocationManager.getInstance(context).initialize();
        }
        AnalyticsTableOpenHelper.getInstance(context).open();

        sessionStartUpTimeStamp = DateTimeUtil.getFormattedCurrentTime(context, false);
        this.logProcessor = new AnalyticsLogProcessor(configuration, context, getApplicationSessionId(), requestHandler);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            logProcessor.initializeSessionStuffs();
        }
        isSDKInitialized = true;
    }

    /**
     * This method initiate {@link AnalyticsManager AnalyticsManager}. Please be aware to destroy the Analytics SDK instance once you are leaving your application,
     * preferably on onActivityDestroyed() method.
     *
     * @param context      is the calling application's {@link Context}
     * @param configuration is an object of Configuration class
     * @since version 1.0.0
     */
    @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_PHONE_STATE})
    public static synchronized void initialize(@NonNull Context context, @NonNull Configuration configuration, @Nullable RequestHandler requestHandler) {
        if (instance != null) {
            throw new IllegalStateException("Analytics SDK already initialized in your application");
        }

        instance = new AnalyticsManager(context, configuration, requestHandler);
        if (configuration.isLogFileEnable()) {
            FileLogger.getInstance().createLogFile();
        }
    }

    public static synchronized AnalyticsManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call initialize() before trying to access Analytics SDK");
        }

        return instance;
    }

    /**
     * Return the configuration object, which is currently using by Analytics SDK.
     *
     * @return the existing configuration object.
     * @see Configuration
     * context@since version 1.0.0
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Return the application session ID
     *
     * @return the application session ID as a {@link String String}.
     * @since version 1.0.0
     */
    public String getApplicationSessionId() {
        String appSessionID;
        appSessionID = configuration.getApplicationName() + "_" + configuration.getUserID() + "_" + sessionStartUpTimeStamp;
        return appSessionID;
    }

    /**
     * This is a single shot call for uploading only one GPS location info at Analytics Server.
     * <br>
     * Provide necessary values as a {@link MLocation MLocation} object.
     *
     * @param mLocation an object containing all the necessary values for GPS info.
     * @since version 1.0.0
     */
    public void logLocationData(final MLocation mLocation) {
        logProcessor.logLocationDataInCache(mLocation);
    }

    /**
     * A method for capturing your GPS information in a timely basis and send them to Analytics Server.
     * <br>
     * <br>
     * The uploading procedure might be batch-wise or one at a time.
     * It can be handled by using a configuration parameter in {@link Configuration Configuration}.
     * <br>
     * <br>
     * To stop this continuous upload invoke the {@link #stopLoggingLocationData()} method.
     *
     * @param interval the time gap in milliseconds between two successive data upload.
     * @since version 1.0.0
     */
    @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public void logLocationData(int interval) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Location information logging requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission");
        }
        logProcessor.logLocationData(interval);
    }

    /**
     * Stop uploading GPS information into Analytics Server.
     *
     * @since version 1.0.0
     */
    public void stopLoggingLocationData() {
        logProcessor.stopLoggingLocationData();
    }

    /**
     * This is a single shot call.It uploads single network quality info associated with the provided GPS data.
     * <br>
     * Provide necessary values as a {@link MLocation MLocation} object.
     *
     * @param mLocation an object containing all the necessary values for GPS info.
     * @since version 1.0.0
     */
    public void logNetworkQualityData(final MLocation mLocation) {
        logProcessor.logNetworkQualityDataInCache(mLocation);
    }

    /**
     * A method for capturing your network quality information and also GPS info.
     * It runs in a timely basis for data capturing and sending them to Analytics Server.
     * <br>
     * <br>
     * To stop this continuous upload invoke the {@link #stopLoggingNetworkQuality()} method.
     *
     * @param interval the time gap in milliseconds between two successive data upload.
     * @since version 1.0.0
     */
    @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public void logNetworkQualityData(int interval) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Network quality logging requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission");
        }
        logProcessor.logNetworkQualityMonitor(interval);
    }

    /**
     * Stop capturing GPS info and uploading network quality information.
     *
     * @since version 1.0.0
     */
    public void stopLoggingNetworkQuality() {
        logProcessor.stopLoggingNetworkQuality();
    }

    /**
     * Log the detailed information into Analytics Server when application has faced any problem or technical difficulties.
     *
     * @param errorCode        a {@link String String} value indicating an application specific code
     * @param errorDescription a {@link String String} value describing the error
     * @param reason           a {@link String String} representation of the reason behind the error
     * context@since version 1.0.0
     */
    public void logApplicationBug(String errorCode, String errorDescription, String reason) {
        if (configuration == null || !configuration.isUploadEnable())
            return;

        String errorDesc = errorDescription + "::" + reason;

        StringBuffer value = new StringBuffer();
        value.append(getApplicationSessionId());
        value.append(AppConstant.SEPARATOR);
        value.append(errorCode);
        value.append(AppConstant.SEPARATOR);
        value.append(errorDesc);

        Hashtable<String, String> logAppBugParamList = new Hashtable<String, String>();
        logAppBugParamList.put("value", value.toString());
        logAppBugParamList.put("user_id", configuration.getUserID());

        AnalyticsLogDBManager.getInstance(context).addRequestLog(Utils.getURLEncodedString(logAppBugParamList), getApplicationSessionId(), true);
    }

    /**
     * Log client provided feedback into Analytics Server.
     *
     * @param category    a {@link String String} value indicating the feedback category
     * @param name        the {@link String String} value indicating client's name
     * @param subject     a {@link String String} value for the subject of the feedback
     * @param messageBody the body containing detailed information associated with the feedback
     * @param imei        the IMEI number of the client's phone
     * @param brand       the brand name of the client's phone
     * @param model       the model name/number of the client's phone
     * context@since version 1.0.0
     */
    public void logClientFeedBack(String category, String name, String subject, String messageBody,
                                  String imei, String brand, String model) {
        try {
            if (configuration == null || !configuration.isUploadEnable()) {
                return;
            }

            Hashtable<String, String> paramList = new Hashtable<String, String>();
            paramList.put("app_session_id", getApplicationSessionId());
            paramList.put("category", category);
            paramList.put("name", name);
            paramList.put("msg_subject", subject);
            paramList.put("msg_body", messageBody);
            paramList.put("imei_no", imei);
            paramList.put("brand", brand);
            paramList.put("model", model);
            paramList.put("user_id", configuration.getUserID());

            AnalyticsLogDBManager.getInstance(context).addRequestLog(
                    Utils.getURLEncodedString(paramList), getApplicationSessionId(), true);
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        }
    }

    /**
     * Log an application action details into Analytics Server.
     *
     * @param action     a {@link String String} value representing the action
     * @param result     a {@link String String} value representing the result of an action
     * @param deviceTime the timestamp when the action was performed
     * @param actionType the type of the performed action
     * context@since version 1.0.0
     */
    public void logAppAction(String action, String result, long deviceTime, String actionType) {
        if (configuration == null || !configuration.isUploadEnable())
            return;

        StringBuffer value = new StringBuffer();
        value.append(getApplicationSessionId());
        value.append(AppConstant.SEPARATOR);
        value.append(action);
        value.append(AppConstant.SEPARATOR);
        value.append(result);
        value.append(AppConstant.SEPARATOR);
        value.append(DateTimeUtil.getFormattedTimeWithoutOffset(deviceTime, false));
        value.append(AppConstant.SEPARATOR);
        value.append(actionType);

        Hashtable<String, String> logAppBugParamList = new Hashtable<String, String>();
        logAppBugParamList.put("value", value.toString());
        logAppBugParamList.put("user_id", configuration.getUserID());

        AnalyticsLogDBManager.getInstance(context).addRequestLog(Utils.getURLEncodedString(logAppBugParamList), getApplicationSessionId(), true);
    }

    /**
     * Return indication for Analytics SDK initialization.
     *
     * @return a true if the Analytics SDK already initialized, false otherwise.
     * context@since version 1.0.0
     */
    public boolean isInitialized() {
        return isSDKInitialized;
    }

    /**
     * This method remove all callbacks and also destroy AnalyticsManager instance.
     */
    public void destroy() {
        FusedLocationManager.getInstance(context).stopLocationUpdates();
        logProcessor.destroy();
        FileLogger.getInstance().destroy();

        isSDKInitialized = false;
        instance = null;
    }

    /**
     * This method return an updated Geolocation object.
     *
     * @return the location object.
     * @since version 1.0.0
     */
    public Location getUpdatedGeoLocation() {
        return logProcessor.getUpdatedGeoLocation();
    }

    /**
     * Set a listener object for receiving GPS updates into your class.
     *
     * @param listener is CapturedDataListener. GPS data could be retrieve by using this callback
     * @since version 1.0.0
     */
    public void setCapturedDataListener(CapturedDataListener listener) {
        logProcessor.setCapturedDataListener(listener);
    }

    /**
     * Removes the listener object, hence GPS updates will no longer be available.
     *
     * @since version 1.0.0
     */
    public void removeCapturedDataListener() {
        logProcessor.removeCapturedDataListener();
    }
}
