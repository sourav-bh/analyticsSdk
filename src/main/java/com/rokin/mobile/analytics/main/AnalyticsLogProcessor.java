package com.rokin.mobile.analytics.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.rokin.mobile.analytics.CapturedDataListener;
import com.rokin.mobile.analytics.Configuration;
import com.rokin.mobile.analytics.data.MLocation;
import com.rokin.mobile.analytics.network.RequestHandler;
import com.rokin.mobile.analytics.constant.AppConstant;
import com.rokin.mobile.analytics.db.AnalyticsLogDBManager;
import com.rokin.mobile.analytics.db.AnalyticsLogDBManager.AnalyticsLog;
import com.rokin.mobile.analytics.location.FusedLocationManager;
import com.rokin.mobile.analytics.location.MLocationUpdateListener;
import com.rokin.mobile.analytics.location.MLocationManager;
import com.rokin.mobile.analytics.utils.DateTimeUtil;
import com.rokin.mobile.analytics.utils.DeviceInfo;
import com.rokin.mobile.analytics.utils.FileLogger;
import com.rokin.mobile.analytics.utils.Logger;
import com.rokin.mobile.analytics.network.Request;
import com.rokin.mobile.analytics.network.Response;
import com.rokin.mobile.analytics.network.ResponseListener;
import com.rokin.mobile.analytics.utils.Utils;
import com.rokin.mobile.analytics.utils.telephony.CellInfo;
import com.rokin.mobile.analytics.utils.telephony.NetworkInfoProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Created by Sourav
 */

public class AnalyticsLogProcessor implements ResponseListener, MLocationUpdateListener {
    private Configuration configuration;
    private Context context;

    private String appSessionId;
    private RequestHandler requestHandler;

    private boolean isUploadProcessing;
    private Handler logUploadHandler = null;
    private Runnable logUploadRunnable = null;

    private MLocationManager locManagerNqmLog = null;
    private MLocationManager locManagerGpsLog = null;

    private int gpsDataLoggingInterval = 0, nqmDataLoggingInterval = 0;
    private long lastTimeGPSDataLogged = 0, lastTimeNQMDataLogged = 0;

    private CapturedDataListener capturedDataListener = null;
    private Location updatedGeolocation = null;

    public AnalyticsLogProcessor(Configuration configuration, Context context, String appSessionId, RequestHandler requestHandler) {
        this.configuration = configuration;
        this.context = context;
        this.appSessionId = appSessionId;
        this.requestHandler = requestHandler;
    }

    private void initializeGPS() {
        if (configuration.getCurrentLocation() != null || updatedGeolocation != null) {
            if (configuration.getCurrentLocation() != null && updatedGeolocation == null) {
                this.updatedGeolocation = configuration.getCurrentLocation();
            }
            updatedGeolocation = configuration.getCurrentLocation();
        } else if (DeviceInfo.isGooglePlayServicesAvailable(context)) {
            FusedLocationManager managerUpdate = FusedLocationManager.getInstance(context);
            if (managerUpdate.getLastLocation() != null) {
                this.updatedGeolocation = managerUpdate.getLastLocation();
            }
        }
    }

    @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_PHONE_STATE})
    public void initializeSessionStuffs() {
        initializeGPS();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            DeviceInfo.profileDevice(context);
        }

        if (AnalyticsLogDBManager.getInstance(context).isLogRemainingForSend()) {
            AnalyticsLogDBManager.getInstance(context).makePreviousUploadable();
        }

        startAnalyticsLogUploader();
    }

    /**
     * Starts a uploader to check and upload pending/uncommitted analytics logs to Analytics server.
     * It checks and uploads analytics logs in every 10 seconds.
     */
    private void startAnalyticsLogUploader() {
        isUploadProcessing = false;
        logUploadHandler = new Handler();
        logUploadRunnable = new Runnable() {
            public void run() {
                if (configuration.isUploadEnable())
                    processPendingAnalyticsLog();

                logUploadHandler.postDelayed(this, configuration.getLogUploadingInterval());
            }
        };
        logUploadHandler.postDelayed(logUploadRunnable, configuration.getLogUploadingInterval());
    }

    /**
     * Upload pending session requests and GPS log to Analytics Server,
     * if there is no upload running.
     */
    private void processPendingAnalyticsLog() {
        if (!isUploadProcessing) {
            uploadAnalyticsLogFromDB();
        }
    }

    /**
     * Fetch 10 analytics log from database and process them for uploading.
     */
    private void uploadAnalyticsLogFromDB() {
        isUploadProcessing = true;
        int maxGPSDataCount = 1;
        if (configuration.isBundleGPSDataUpload()) {
            maxGPSDataCount = 10;
        }

        if (AnalyticsLogDBManager.getInstance(context).isLogRemainingForSend()) {
            // Fetch the logged requests or GPS data from database.
            ArrayList<ArrayMap<String, String>> logList = AnalyticsLogDBManager.getInstance(context).getRemainingLogsToUpload();
            int gpsDataCount = 0;
            String logType = "", logValue = "", gpsDataValueString = "", gpsDataLogIdString = "";
            for (int i = 0; i < logList.size(); i++) {
                ArrayMap<String, String> logItem = logList.get(i);
                logType = logItem.get(AnalyticsLog.COLUMN_LOG_TYPE);
                logValue = logItem.get(AnalyticsLog.COLUMN_LOG_VALUE);

                if (logType != null && logType.equalsIgnoreCase(AnalyticsLogDBManager.LOG_TYPE_REQUEST_LOG)) {
                    break;
                } else if (logType.equalsIgnoreCase(AnalyticsLogDBManager.LOG_TYPE_GPS_DATA_LOG)) {
                    gpsDataCount++;
                    if (i == 0) {
                        gpsDataValueString = logValue;
                        gpsDataLogIdString = logItem.get(AnalyticsLog.COLUMN_ID);
                    } else {
                        gpsDataValueString += "~" + logValue;
                        gpsDataLogIdString += "," + logItem.get(AnalyticsLog.COLUMN_ID);
                    }
                }
            }

            // Send the fetched requests or GPS data to Analytics server.
            if (logType != null) {
                if (logType.equalsIgnoreCase(AnalyticsLogDBManager.LOG_TYPE_REQUEST_LOG)) {
                    if (gpsDataValueString != null && !gpsDataValueString.equalsIgnoreCase("")) {
                        // upload gps log to analytics and check it is succeed or not
                        // If succeeded then remove that gps log from db
                        // add the gpsDataLogIdString as a tag into ServerConnector
                        uploadGPSDataLogs(gpsDataValueString, gpsDataLogIdString);
                    } else if (logValue != null && !logValue.equalsIgnoreCase("")) {
                        // Send request log to analytics and check it is succeed or not
                        // If succeeded then remove that request from db
                        // add the logValue as a tag into ServerConnector

                        Request request = new Request(this, logValue, AppConstant.ANALYTICS_LOG_REQUEST_TYPE);
                        request.setTag(logValue);
                        if (requestHandler != null) {
                            requestHandler.onServerRequest(request);
                        }
                    }
                } else if (logType.equalsIgnoreCase(AnalyticsLogDBManager.LOG_TYPE_GPS_DATA_LOG)
                        && gpsDataValueString != null && !gpsDataValueString.equalsIgnoreCase("") && gpsDataCount >= maxGPSDataCount) {
                    // upload gps log to analytics and check it is succeed or not
                    // If succeeded then remove that gps log from db
                    // add the gpsDataLogIdString as a tag into ServerConnector
                    uploadGPSDataLogs(gpsDataValueString, gpsDataLogIdString);
                }
            }
        }
        isUploadProcessing = false;
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
        gpsDataLoggingInterval = interval;
        lastTimeGPSDataLogged = 0;

        if (locManagerGpsLog == null) {
            locManagerGpsLog = new MLocationManager(context, AnalyticsLogProcessor.this, AppConstant.LOCATION_REQUEST_FOR_GPS_LOGGING);
        }
        locManagerGpsLog.setRequestType(AppConstant.LOCATION_REQUEST_FOR_GPS_LOGGING);
        locManagerGpsLog.updateLocation();
    }

    /**
     * Stop uploading GPS information into Analytics Server.
     *
     * @since version 1.0.0
     */
    public void stopLoggingLocationData() {
        if (locManagerGpsLog != null) {
            locManagerGpsLog.stopListeningForUpdate();
        }
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
    public void logNetworkQualityMonitor(int interval) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Network quality logging requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission");
        }
        nqmDataLoggingInterval = interval;
        lastTimeNQMDataLogged = 0;

        if (locManagerNqmLog == null) {
            locManagerNqmLog = new MLocationManager(context, AnalyticsLogProcessor.this, AppConstant.LOCATION_REQUEST_FOR_NQM_LOGGING);
        }
        locManagerNqmLog.setRequestType(AppConstant.LOCATION_REQUEST_FOR_NQM_LOGGING);
        locManagerNqmLog.updateLocation();
    }

    /**
     * Stop capturing GPS info and uploading network quality information.
     *
     * @since version 1.0.0
     */
    public void stopLoggingNetworkQuality() {
        if (locManagerNqmLog != null) {
            locManagerNqmLog.stopListeningForUpdate();
        }
    }

    /**
     * This will add a entry to database for logLocationData.
     *
     * @param gpsData an object containing all the necessary values for GPS info.
     *                Must pass empty String for <b>LOGBOOK</b> or <b>TRACKING</b> mode
     * @since version 1.0.0
     */
    public void logLocationDataInCache(MLocation gpsData) {
        TelephonyManager telephonyManager = NetworkInfoProvider.getInstance(context).getTelephonyManager();
        boolean isRoaming = false;
        if (telephonyManager != null) {
            isRoaming = telephonyManager.isNetworkRoaming();
        }

        if (gpsData == null || context == null)
            return;

        if (configuration == null || !configuration.isUploadEnable())
            return;

        String networkType = NetworkInfoProvider.getInstance(context).getNetWorkType();

        String gpsLogData = gpsData.getFormattedLogString(appSessionId, NetworkInfoProvider.getInstance(context).getCellInfo(), networkType);
        AnalyticsLogDBManager.getInstance(context).addGPSLog(gpsLogData, appSessionId, true);
    }

    /**
     * Upload GPS log to Analytics Server
     *
     * @param gpsDataLogValues is a String containing the GPS log data
     * @param gpsDataLogIDs    is the IDs associated with GPS logs in database
     */
    private void uploadGPSDataLogs(String gpsDataLogValues, String gpsDataLogIDs) {
        Request request = new Request(this, gpsDataLogValues, AppConstant.ANALYTICS_LOG_GPS_DATA_REQUEST_TYPE);
        request.setTag(gpsDataLogIDs);
        if (requestHandler != null) {
            requestHandler.onServerRequest(request);
        }
    }

    /**
     * This method creates a network quality into entry into database.
     *
     * @param gpsData an object containing all the necessary values for GPS info
     * @since version 1.0.0
     */
    public synchronized void logNetworkQualityDataInCache(MLocation gpsData) {
        try {
            if (configuration == null || !configuration.isUploadEnable()) {
                return;
            }
            CellInfo cellInfo = NetworkInfoProvider.getInstance(context).getCellInfo();
            if (cellInfo.getCellID() == 0)
                return;

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();

            JSONArray valueArray = new JSONArray();

            JSONObject gprs = new JSONObject();
            gprs.put("type", "gprs_Info");
            JSONObject gprsInfodata = new JSONObject();
            gprsInfodata.put("LAC", cellInfo.getLAC());
            gprsInfodata.put("Home MNC", cellInfo.getMNC());
            gprsInfodata.put("IMEI", DeviceInfo.getIMEINumber());
            gprsInfodata.put("Cell ID", cellInfo.getCellID());
            gprsInfodata.put("Home MCC", cellInfo.getMCC());

            JSONArray gprsInfoDataArray = new JSONArray();
            gprsInfoDataArray.put(gprsInfodata);
            gprs.put("data", gprsInfoDataArray);

            valueArray.put(gprs);

            JSONObject gps = new JSONObject();
            gps.put("type", "gps");

            JSONObject gpsDataObj = new JSONObject();
            gpsDataObj.put("verticalAccuracy", gpsData.getAccuracy());
            gpsDataObj.put("timestamp", gpsData.getTime());
            gpsDataObj.put("latitude", gpsData.getLocation().getLatitude());
            gpsDataObj.put("isValid", true);
            gpsDataObj.put("speed", gpsData.getSpeed());
            gpsDataObj.put("coarse", gpsData.getBearing());
            gpsDataObj.put("longitude", gpsData.getLocation().getLongitude());
            gpsDataObj.put("horizontalAccuracy", gpsData.getAccuracy());
            gpsDataObj.put("altitude", gpsData.getAltitude());

            JSONArray gpsDataArray = new JSONArray();
            gpsDataArray.put(gpsDataObj);
            gps.put("data", gpsDataArray);

            valueArray.put(gps);

            JSONObject radioInfoObj = new JSONObject();
            radioInfoObj.put("type", "radio_Info");

            JSONObject radioInfoDataObj = new JSONObject();
            radioInfoDataObj.put("Data service operational", true);
            if (info != null)
                radioInfoDataObj.put("State", info.getState());
            else
                radioInfoDataObj.put("State", "Unknown");

            if (cm.getAllNetworkInfo() != null) {
                radioInfoDataObj.put("Number of networks", cm.getAllNetworkInfo().length);
            } else {
                radioInfoDataObj.put("Number of networks", 0);
            }
            if (info != null)
                radioInfoDataObj.put("Network type", info.getType());
            else
                radioInfoDataObj.put("Network type", "Unknown");

            radioInfoDataObj.put("Network service", 36870);
            radioInfoDataObj.put("Packets sent", TrafficStats.getTotalTxBytes());
            radioInfoDataObj.put("MNC 0", cellInfo.getMNC());
            radioInfoDataObj.put("MCC 0", cellInfo.getMCC());
            radioInfoDataObj.put("Signal strength", cellInfo.getSignalStrength());
            radioInfoDataObj.put("Current network name", tm.getNetworkOperatorName());
            radioInfoDataObj.put("networkName", tm.getNetworkOperatorName());
            radioInfoDataObj.put("Network country code 0", tm.getNetworkCountryIso());

            JSONArray radioInfoArray = new JSONArray();
            radioInfoArray.put(radioInfoDataObj);
            radioInfoObj.put("data", radioInfoArray);
            valueArray.put(radioInfoObj);

            Hashtable<String, String> paramList = new Hashtable<String, String>();
            paramList.put("app_session_id", appSessionId);
            paramList.put("value", valueArray.toString());
            paramList.put("device_time_stamp", DateTimeUtil.getFormattedCurrentTime(context, true));
            paramList.put("user_id", configuration.getUserID()/*GlobalSettings.getInstance().getUserID(*/);
            paramList.put("async_enable", "true");

            AnalyticsLogDBManager.getInstance(context).addRequestLog(Utils.getURLEncodedString(paramList), appSessionId, true);
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        }
    }

    @Override
    public void onLocationUpdated(MLocation locData, int requestType) {
        this.updatedGeolocation = locData.getLocation();
        updatedGeolocation = locData.getLocation();

        if (requestType == AppConstant.LOCATION_REQUEST_FOR_NQM_LOGGING) {
            if ((System.currentTimeMillis() - lastTimeNQMDataLogged) > nqmDataLoggingInterval) {
                lastTimeNQMDataLogged = System.currentTimeMillis();
                logNetworkQualityDataInCache(locData);
            }
        } else if (requestType == AppConstant.LOCATION_REQUEST_FOR_GPS_LOGGING) {
            if (System.currentTimeMillis() - lastTimeGPSDataLogged >= gpsDataLoggingInterval) {
                lastTimeGPSDataLogged = System.currentTimeMillis();
                logLocationDataInCache(locData);
            }
        }

        if (capturedDataListener != null) {
            capturedDataListener.capturedData(locData);
        }
    }

    @Override
    public void onLocationRequestTimedOut(boolean isTimeOut) {
        Toast.makeText(context, "Location services failed to connect!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationStatusChanged(int status) {

    }

    @Override
    public void serverResponse(Response response, int type) {
        String res = "";
        if (response.getData() == null)
            res = "";

        FileLogger.getInstance().writeLog("INFO_ANALYTICS_RESPONSE: " + response.getRequestType(), res);

        if (response.getRequestType() == AppConstant.ANALYTICS_LOG_REQUEST_TYPE) {
            if (response.getStatus()) {
                String responseData = (String) response.getData();
                JSONObject jsonResponse;
                try {
                    jsonResponse = new JSONObject(responseData);
                    if (jsonResponse.optBoolean(AppConstant.SUCCESS, false)) {
                        AnalyticsLogDBManager.getInstance(context).removeLog(AnalyticsLogDBManager.LOG_TYPE_REQUEST_LOG, response.getTag());
                    }
                } catch (JSONException e) {
                }
            }
            isUploadProcessing = false;
        } else if (response.getRequestType() == AppConstant.ANALYTICS_LOG_GPS_DATA_REQUEST_TYPE) {
            if (response.getStatus()) {
                AnalyticsLogDBManager.getInstance(context).removeLog(AnalyticsLogDBManager.LOG_TYPE_GPS_DATA_LOG, response.getTag());
            }
            isUploadProcessing = false;
        }
    }

    private void removeAllLocationCallback() {
        if (locManagerNqmLog != null) {
            locManagerNqmLog.destroy();
        }
        if (locManagerGpsLog != null) {
            locManagerGpsLog.destroy();
        }
    }

    public void destroy() {
        removeAllLocationCallback();

        if (logUploadHandler != null) {
            logUploadHandler.removeCallbacks(logUploadRunnable);
            logUploadHandler = null;
        }
    }

    /**
     * Set a listener object for receiving GPS updates into your class.
     *
     * @param listener is CapturedDataListener. GPS data could be retrieve by using this callback
     * @since version 1.0.0
     */
    public void setCapturedDataListener(CapturedDataListener listener) {
        this.capturedDataListener = listener;
    }

    /**
     * Removes the listener object, hence GPS updates will no longer be available.
     *
     * @since version 2.0.0
     */
    public void removeCapturedDataListener() {
        this.capturedDataListener = null;
    }

    /**
     * This method return an updated Geolocation object.
     *
     * @return the geolocation object.
     * @since version 1.0.0
     */
    public Location getUpdatedGeoLocation() {
        return this.updatedGeolocation;
    }
}
